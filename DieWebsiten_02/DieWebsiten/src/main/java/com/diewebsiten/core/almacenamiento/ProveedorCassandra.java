package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.almacenamiento.dto.SentenciaCassandra;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.diewebsiten.core.util.Transformaciones.agruparValores;
import static com.diewebsiten.core.util.Transformaciones.ponerObjeto;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Administrar las conexiones y transacciones que se realizan al
 * motor de base de datos Cassandra.
 *
 * @author Juan Camilo Flórez Román (www.diewebsiten.com)
 */
public class ProveedorCassandra extends ProveedorAlmacenamiento {

    private static volatile Conexion proveedorCassandra;

    private static Optional<Cluster> cluster;
    private static Session sesion;
    private static Object obj = new Object();

    private Function<String, ResultSet> obtenerResultSet = (sentencia) -> sesion.execute(sentencia);
    private BiFunction<SentenciaCassandra, Object[], ResultSet> obtenerResultSetParametros = (sentencia, parametros) -> sesion.execute(sentencia.getSentenciaPreparada().bind(parametros));

    private static final String CASSANDRA_URL = "localhost";
    private static final int CASSANDRA_PORT = 9042;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    
    private ProveedorCassandra() {
    	conectar();
    }
    
    /*
     * Unica instancia de la clase ProveedorCassandra.
     */
    static Conexion inicializar() {
    	
    	if (proveedorCassandra == null) {
    		synchronized(obj) {
    			if (proveedorCassandra == null) {
					try {
						proveedorCassandra = new Conexion().setProveedorAlmacenamiento(new ProveedorCassandra());
					} catch (Exception e) {
						proveedorCassandra = new Conexion().setErrorConexion(e);
					}
    			}
    		}
    	}
    	
    	return proveedorCassandra;
    }

    /**
     * Establecer una conexión con el motor de base de datos.
     * 
     * 1. Establecer los parámetros de conexión al motor de base de datos.
     * 2. Crear una sesión de conexión a la base de datos.
     */
	private static void conectar() {
		// USAR UN TRY-WITH-RESOURCES AQUI PARA VER SI SÍ FUNCIONA
		cluster = Optional.ofNullable(Cluster.builder().addContactPoint(CASSANDRA_URL).withPort(CASSANDRA_PORT).build());
		sesion = cluster.get().connect();
    }
    
    /**
     * Cerrar la conexión con el motor de base de datos.
     */
	@Override
	void desconectar() {
		cluster.ifPresent(Cluster::close);
    }
	
	/**
     * 
     */
    @Override
    public JsonNode ejecutarTransaccion(Transaccion transaccion) throws ExcepcionGenerica {

    	String sentenciaCQL = transaccion.getSentencia();
    	String nombreTransaccion = transaccion.getNombre();
    	Object[] parametros = Optional.ofNullable(transaccion.getParametrosTransaccion()).orElse(new Object[]{});
    	boolean necesitaResultadoEnJerarquia = transaccion.isResultadoEnJerarquia();

    	if (isBlank(nombreTransaccion)) {
    		throw new ExcepcionGenerica("El nombre de la sentencia no puede venir vacío.");
    	}

    	try {

    		// ESTE LLAMADO PODRIA SER MEDIANTE UNA FABRICA????
    		SentenciaCassandra sentencia = SentenciaCassandra.obtenerSentencia(sesion, sentenciaCQL, nombreTransaccion);

    		if (parametros.length != sentencia.getNumeroParametrosSentencia()) {
    			throw new ExcepcionGenerica("La sentencia necesita " + sentencia.getNumeroParametrosSentencia() + " parámetros para ser ejecutada.");
    		}

    		ResultSet resultadoEjecucion = isEmpty(parametros) ? obtenerResultSet.apply(sentenciaCQL) :  obtenerResultSetParametros.apply(sentencia, parametros);

    		if (resultadoEjecucion.isExhausted()) {
    			if (necesitaResultadoEnJerarquia) return mapper.createObjectNode();
    			else return mapper.createArrayNode();
    		}

			Supplier<Stream<Definition>> columnasResultado = () -> StreamSupport.stream(resultadoEjecucion.getColumnDefinitions().spliterator(), false);
    		if (necesitaResultadoEnJerarquia) {
    			SentenciaCassandra.enriquecerSentencia(sesion, columnasResultado, sentencia);
    			return new Estructura(resultadoEjecucion, sentencia, columnasResultado).obtenerResultadoConJerarquia();
    		} else {
    			return new Estructura(resultadoEjecucion, columnasResultado).obtenerResultado();
    		}

		} catch (Throwable e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? Arrays.asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }

    private class Estructura {
    	
    	private ObjectNode coleccionActual, resultado = mapper.createObjectNode();
    	private ResultSet resultadoEjecucion;

    	private SentenciaCassandra sentencia;
    	private Supplier<Stream<Definition>> columnasResultado;

    	private Estructura(ResultSet resultadoEjecucion, Supplier<Stream<Definition>> columnasResultado) {
			this.resultadoEjecucion = resultadoEjecucion;
			this.columnasResultado = columnasResultado;
		}

		private Estructura(ResultSet resultadoEjecucion, SentenciaCassandra sentencia, Supplier<Stream<Definition>> columnasResultado) {
			this(resultadoEjecucion, columnasResultado);
			this.sentencia = sentencia;



		}

    	private ObjectNode obtenerResultadoConJerarquia () {

    		Stream<Row> filas = StreamSupport.stream(resultadoEjecucion.spliterator(), false);
			Supplier<Stream<Row>> sFilas = () -> filas;
    		coleccionActual = resultado;

			System.out.println(
			sFilas.get()
				.map(fila -> StreamSupport.stream(fila.getColumnDefinitions().spliterator(), false)
										  .collect(Collectors.toMap(Definition::getName, columna -> obtenerValorColumnaActual(fila, columna))))
					.peek(System.out::println)
				.collect(Collectors.toList())
			);

    		filas.forEach(fila -> {
    			sentencia.getColumnasIntermedias().get()
						.forEach(columnaIntermedia -> {
							coleccionActual = ponerObjeto.apply(coleccionActual, columnaIntermedia.getName());
							coleccionActual = ponerObjeto.apply(coleccionActual, obtenerValorColumnaActual(fila, columnaIntermedia).toString());
						});
    			sentencia.getColumnasRegulares().get()
						.forEach(columnaRegular -> agruparValores(coleccionActual, columnaRegular.getName(), mapper.valueToTree(obtenerValorColumnaActual(fila, columnaRegular))));
    			coleccionActual = resultado;
    		});

    		return resultado;
    		
    	}
    	
    	private Object obtenerValorColumnaActual(Row fila, Definition columnaActual) {
    		return Optional.ofNullable(fila.getBytesUnsafe(columnaActual.getName()))
    					   .map(buffer -> columnaActual.getType().deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED))
    					   .orElse(obtenerValorVacio(columnaActual));
    	}
    	
    	/**
    	 * Si el valor de la columna actual es nulo se guardará como valor un "valor vacío"
    	 * dependiendo del tipo de variable al que pertenezca la columna actual
    	 *
    	 * @param columnaActual
    	 * @return
    	 */
    	private Object obtenerValorVacio(ColumnDefinitions.Definition columnaActual) {
    		String tipoColumna = columnaActual.getType().asJavaClass().getSimpleName();
    		if (columnaActual.getType().isCollection()) {
    			return "List".equals(tipoColumna) ? new ArrayList<>() : new HashMap<>();
    		} else {    							
    			if ("String".equals(tipoColumna))
    				return "";
    			else if ("Integer,Long,Float,Double,BigDecimal,BigInteger".indexOf(tipoColumna) > -1)
    				return 0;
    			else
    				return null;
    		}
    	}

    	/**
         *
         * @return
         */
        private ArrayNode obtenerResultado() {

    		Stream<Row> filas = StreamSupport.stream(resultadoEjecucion.spliterator(), false);
    		ArrayNode resultado = mapper.createArrayNode();

    		filas.forEach(fila -> {
    				ObjectNode obj = mapper.createObjectNode();
    				columnasResultado.get().forEach(columna -> obj.set(columna.getName(), mapper.valueToTree(obtenerValorColumnaActual(fila, columna))));
    				resultado.add(obj);
    				}
    		);

    		return resultado;

        }
    	
    }
    
}
