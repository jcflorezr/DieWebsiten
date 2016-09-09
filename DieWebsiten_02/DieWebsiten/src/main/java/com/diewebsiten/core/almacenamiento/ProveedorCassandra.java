package com.diewebsiten.core.almacenamiento;

import static com.diewebsiten.core.almacenamiento.dto.Sentencia.TiposResultado;

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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.diewebsiten.core.util.Transformaciones.agruparValores;
import static com.diewebsiten.core.util.Transformaciones.ponerObjeto;
import static java.util.stream.Collectors.*;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    
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

		String nombreTransaccion = transaccion.getNombre();
		String sentenciaCQL = transaccion.getSentencia();
    	Object[] parametros = Optional.ofNullable(transaccion.getParametrosTransaccion()).orElse(new Object[]{});
    	TiposResultado tipoResultado = transaccion.getTipoResultado();

    	if (isBlank(nombreTransaccion)) {
    		throw new ExcepcionGenerica("El nombre de la sentencia no puede venir vacío.");
    	}

    	try {

    		// ESTE LLAMADO PODRIA SER MEDIANTE UNA FABRICA????
    		SentenciaCassandra sentencia = new SentenciaCassandra().obtenerSentencia(sesion, sentenciaCQL, nombreTransaccion);

    		if (parametros.length != sentencia.getNumeroParametrosSentencia()) {
    			throw new ExcepcionGenerica("La sentencia necesita " + sentencia.getNumeroParametrosSentencia() + " parámetros para ser ejecutada.");
    		}

    		ResultSet resultadoEjecucion = isEmpty(parametros) ? obtenerResultSet.apply(sentenciaCQL)
															   : obtenerResultSetParametros.apply(sentencia, parametros);

    		if (resultadoEjecucion.isExhausted()) {
    			if (TiposResultado.PLANO.equals(tipoResultado)) return MAPPER.createArrayNode();
    			else return MAPPER.createObjectNode();
    		}

			return new Estructura(resultadoEjecucion, sentencia).obtenerResultado(tipoResultado);

		} catch (Throwable e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? Arrays.asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }

    private class Estructura {
    	
    	private ObjectNode coleccionActual, resultado = MAPPER.createObjectNode();
    	private ResultSet resultadoEjecucion;
    	private SentenciaCassandra sentencia;

		private Estructura(ResultSet resultadoEjecucion, SentenciaCassandra sentencia) {
			this.resultadoEjecucion = resultadoEjecucion;
			sentencia.enriquecerSentencia(sesion, resultadoEjecucion, sentencia);
			this.sentencia = sentencia;
		}

		private Supplier<Stream<Map<String, Object>>> transformarResultadoEjecucion() {
			Supplier<Stream<Row>> filasStream = () -> StreamSupport.stream(resultadoEjecucion.spliterator(), false);
			Function<Row, Stream<Definition>> columnasStream = (fila) -> StreamSupport.stream(fila.getColumnDefinitions().spliterator(), false);
			return () -> filasStream.get().parallel().map(fila -> columnasStream.apply(fila)
										  			 .collect(toMap(Definition::getName, columna -> obtenerValorColumnaActual(fila, columna))));
		}

		private JsonNode obtenerResultado(TiposResultado tipoResultado) {
			switch (tipoResultado) {
				case PLANO: return obtenerResultadoPlano();
				case JERARQUÍA: return obtenerResultadoConJerarquia(false);
				case JERARQUÍA_CON_NOMBRES_DE_COLUMNAS: return obtenerResultadoConJerarquia(true);
				default: throw new ExcepcionGenerica("El tipo de resultado: '" + tipoResultado + "', no es válido.");
			}
		}

        private ArrayNode obtenerResultadoPlano() {
			List<Map<String, Object>> resultSet = transformarResultadoEjecucion().get().collect(toList());
			return MAPPER.convertValue(resultSet, ArrayNode.class);
        }

    	private ObjectNode obtenerResultadoConJerarquia (boolean incluirNombresColumnasPrimarias) {
			Supplier<Stream<Map<String, Object>>> resultSet = transformarResultadoEjecucion();
    		coleccionActual = resultado;
			resultSet.get().parallel().forEach(fila -> {
				sentencia.getColumnasIntermedias().get()
						.forEach(columnaIntermedia -> {
							if (incluirNombresColumnasPrimarias) coleccionActual = ponerObjeto.apply(coleccionActual, columnaIntermedia);
							coleccionActual = ponerObjeto.apply(coleccionActual, fila.get(columnaIntermedia).toString());
						});
				sentencia.getColumnasRegulares().get()
						.forEach(columnaRegular -> agruparValores(coleccionActual, columnaRegular, MAPPER.valueToTree(fila.get(columnaRegular))));
				coleccionActual = resultado;
			});
    		return resultado;
    	}
    	
    	private Object obtenerValorColumnaActual(Row fila, Definition columnaActual) {
			return Optional.ofNullable(fila.getBytesUnsafe(columnaActual.getName()))
    					   .map(buffer -> new CodecRegistry().codecFor(columnaActual.getType()).deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED))
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
    		String tipoColumna = new CodecRegistry().codecFor(columnaActual.getType()).getJavaType().getRawType().getSimpleName();
    		if (columnaActual.getType().isCollection()) {
    			return "List".equals(tipoColumna) ? new ArrayList<String>() : new HashMap<>();
    		} else {    							
    			if ("String".equals(tipoColumna))
    				return "";
    			else if ("Integer,Long,Float,Double,BigDecimal,BigInteger".indexOf(tipoColumna) > -1)
    				return 0;
    			else
    				return null;
    		}
    	}
    	
    }
    
}
