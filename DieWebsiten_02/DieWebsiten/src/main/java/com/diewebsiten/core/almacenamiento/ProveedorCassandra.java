package com.diewebsiten.core.almacenamiento;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.almacenamiento.dto.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.SentenciaCassandra;
import com.diewebsiten.core.almacenamiento.util.Sentencias;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
    
    private Map<String, Sentencia> sentenciasPreparadas;

    private Function<String, ResultSet> obtenerResultSet = (sentencia) -> sesion.execute(sentencia);
    private BiFunction<SentenciaCassandra, Object[], ResultSet> obtenerResultSetParametros = (sentencia, parametros) -> sesion.execute(sentencia.getSentenciaPreparada().bind(parametros));
    
    private static final String CASSANDRA_URL = "localhost";
    private static final int CASSANDRA_PORT = 9042;
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private static final Gson gson = new Gson();
    private static final Type listStringType = new TypeToken<List<String>>(){private static final long serialVersionUID = 1L;}.getType();
    private Function<String, List<String>> stringToListString = (stringAConvertir) -> gson.fromJson(stringAConvertir, listStringType);
    
    
    private ProveedorCassandra() {
    	sentenciasPreparadas = new HashMap<>();
    }
    
    /*
     * Unica instancia de la clase ProveedorCassandra.
     */
    static Conexion inicializar() {
    	
    	if (proveedorCassandra == null) {
    		synchronized(obj) {
    			if (proveedorCassandra == null) {
					try {
						proveedorCassandra = new Conexion();
						conectar();
						proveedorCassandra.setProveedorAlmacenamiento(new ProveedorCassandra());
					} catch (Exception e) {
						proveedorCassandra.setErrorConexion(e);
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
        cluster = Optional.of(Cluster.builder().addContactPoint(CASSANDRA_URL).withPort(CASSANDRA_PORT).build());
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
    public JsonNode ejecutarTransaccion(Transaccion transaccion) throws Exception {

    	String sentenciaCQL = transaccion.getSentencia();
    	String nombreTransaccion = transaccion.getNombre();
    	Object[] parametros = Optional.ofNullable(transaccion.getParametrosTransaccion()).orElse(new Object[]{});
    	boolean necesitaResultadoEnJerarquia = transaccion.isResultadoEnJerarquia();
    	
    	if (isBlank(nombreTransaccion)) {
    		throw new ExcepcionGenerica("El nombre de la sentencia no puede venir vacío.");
    	}
    	
    	try {	
    		SentenciaCassandra sentencia = obtenerSentencia(sentenciaCQL, nombreTransaccion);
    		
    		if (parametros.length != sentencia.getNumeroParametrosSentencia()) {
    			throw new ExcepcionGenerica("La sentencia necesita " + sentencia.getNumeroParametrosSentencia() + " parámetros para ser ejecutada.");
    		}
    		
    		ResultSet resultadoEjecucion = isEmpty(parametros) ? obtenerResultSet.apply(sentenciaCQL) :  obtenerResultSetParametros.apply(sentencia, parametros);
    		
    		if (resultadoEjecucion.isExhausted()) {
    			if (necesitaResultadoEnJerarquia) return mapper.createObjectNode();
    			else return mapper.createArrayNode();
    		}
    		
    		if (necesitaResultadoEnJerarquia) {
    			return new Estructura(resultadoEjecucion, sentencia).obtenerResultadoConJerarquia();
    		} else {
    			return new Estructura(resultadoEjecucion).obtenerResultado();
    		}
    		 
		} catch (Throwable e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? Arrays.asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }
    
    /**
     * Tener sentencias preparadas con el fin de reusarlas
     * @param sentenciaCQL
     * @param nombreTransaccion
     * @return
     */
    private SentenciaCassandra obtenerSentencia(String sentenciaCQL, String nombreTransaccion) {
    	try {
    		SentenciaCassandra sentencia = (SentenciaCassandra) sentenciasPreparadas.get(nombreTransaccion);
    		if (sentencia == null) {
    			synchronized (obj) {
    				sentencia = (SentenciaCassandra) sentenciasPreparadas.get(nombreTransaccion);
    				if (sentencia == null) {
    					PreparedStatement sentenciaPreparada = sesion.prepare(sentenciaCQL);
    					List<String> parametrosSentencia = StreamSupport.stream(sentenciaPreparada.getVariables().spliterator(), false).map(Definition::getName).collect(Collectors.toList());
    					sentencia = new SentenciaCassandra(sentenciaPreparada, Optional.ofNullable(parametrosSentencia).orElse(new ArrayList<>()));
    					sentenciasPreparadas.put(nombreTransaccion, sentencia);
    				}
    			}
    		}
        	return sentencia;
		} catch (ClassCastException e) {
			throw new ExcepcionGenerica("La sentencia de la transacción '" + nombreTransaccion + "' no es de tipo Cassandra");
		} catch (Exception e) {
			throw new ExcepcionGenerica("Error al preparar la nueva sentencia CQL que pertenece a la transacción '" + nombreTransaccion + "'. Sentencia: " + sentenciaCQL + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }
    
    private class Estructura {
    	
    	private ObjectNode resultado = mapper.createObjectNode();
    	private ObjectNode coleccionActual;
    	private ResultSet resultadoEjecucion;
    	private boolean noEsUnicaColumna;
    	private SentenciaCassandra sentencia;
    	private Supplier<Stream<Definition>> columnasResultado;
		private Function<String, ObjectNode> ponerObjeto = nombreColumna -> (ObjectNode) Optional.ofNullable(coleccionActual.get(nombreColumna))
				.orElseGet(() -> coleccionActual.putObject(nombreColumna));

    	private Estructura(ResultSet resultadoEjecucion) {
			this.resultadoEjecucion = resultadoEjecucion;
			this.noEsUnicaColumna = resultadoEjecucion.getColumnDefinitions().size() > 1;
			this.columnasResultado = () -> StreamSupport.stream(resultadoEjecucion.getColumnDefinitions().spliterator(), false);
		}

		private Estructura(ResultSet resultadoEjecucion, SentenciaCassandra sentencia) {
			this(resultadoEjecucion);
			this.sentencia = sentencia;
			if (!this.sentencia.getKeyspaceName().isPresent()) this.sentencia.setKeyspaceName(columnasResultado.get().map(Definition::getKeyspace).findAny().get());
			if (!this.sentencia.getColumnfamilyName().isPresent()) this.sentencia.setColumnfamilyName(columnasResultado.get().map(Definition::getTable).findAny().get());
			
			
//			ESTE HAY QUE MODIFICARLO
			if (this.sentencia.getNumeroColumnasIntermedias() == 0 && this.sentencia.getNumeroColumnasRegulares() == 0) categorizarColumnas();
		}

		private void categorizarColumnas() {
			SentenciaCassandra sentenciaLlavesPrimarias = obtenerSentencia(Sentencias.LLAVES_PRIMARIAS.sentencia(), Sentencias.LLAVES_PRIMARIAS.nombre());
			Row llavesPrimarias = obtenerResultSetParametros.apply(sentenciaLlavesPrimarias, new Object[]{sentencia.getKeyspaceName().get(), sentencia.getColumnfamilyName().get()}).one();
			if (noEsUnicaColumna) {
				// Columnas intermedias
				sentencia.setColumnasIntermedias(Stream.of(stringToListString.apply(llavesPrimarias.getString("key_aliases")),
 					   	   								   stringToListString.apply(llavesPrimarias.getString("column_aliases")))
													   .flatMap(List::stream)
													   .filter(llavePrimaria -> sentencia.getParametrosSentencia().get().noneMatch(parametro -> llavePrimaria.equals(parametro)))
													   .map(columnaIntermedia -> columnasResultado.get().limit(1)
															   .filter(columna -> columna.getName().equals(columnaIntermedia))
															   .findAny()
															   .orElse(null)) // ESTO NO PUEDE QUEDAR ASI (CAMBIARLO POR orElseThrow)
													   .collect(Collectors.toList()));
				// Columnas regulares
				sentencia.setColumnasRegulares(
						columnasResultado.get().filter(columna -> sentencia.getColumnasIntermedias().get().noneMatch(columnaIntermedia -> columnaIntermedia.getName().equals(columna.getName())))
											   .collect(Collectors.toList()));
			} else {	
				// Columnas regulares (en caso de que haya una única columna en el resultSet)
				sentencia.setColumnasRegulares(columnasResultado.get().collect(Collectors.toList()));
			}
		}    	

    	private ObjectNode obtenerResultadoConJerarquia () {

    		Stream<Row> filas = StreamSupport.stream(resultadoEjecucion.spliterator(), false);
    		coleccionActual = resultado;

    		filas.forEach(fila -> {
    			sentencia.getColumnasIntermedias().get()
						.forEach(columnaIntermedia -> {
							coleccionActual = ponerObjeto.apply(columnaIntermedia.getName());
							coleccionActual = ponerObjeto.apply(obtenerValorColumnaActual(fila, columnaIntermedia).toString());
						});
    			sentencia.getColumnasRegulares().get()
						.forEach(columnaRegular -> {
							String nombreColumnaActual = columnaRegular.getName();
							JsonNode valorColumnaActual = mapper.valueToTree(obtenerValorColumnaActual(fila, columnaRegular).toString());
							Optional<JsonNode> nombreColumnaRegular = Optional.ofNullable(coleccionActual.get(nombreColumnaActual));
							nombreColumnaRegular.ifPresent(valorColumnaRegular ->
									{if (valorColumnaRegular.isArray()) ((ArrayNode)valorColumnaRegular).add(valorColumnaActual);
									 else coleccionActual.set(nombreColumnaActual, mapper.createArrayNode().add(valorColumnaRegular).add(valorColumnaActual));});
							if (!nombreColumnaRegular.isPresent()) coleccionActual.set(nombreColumnaActual, valorColumnaActual);
						 });
    			coleccionActual = resultado;
    		});

    		return resultado;
    		
    	}
    	
    	private Object obtenerValorColumnaActual(Row fila, Definition columnaActual) {
    		return Optional.ofNullable(fila.getBytesUnsafe(columnaActual.getName()))
    					   .map((buffer) -> columnaActual.getType().deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED))
    					   .orElse(obtenerValorVacio(columnaActual));
    	}
    	
    	/**
    	 * Si el valor de la columna actual es nulo se guardará como valor un "valor vacío"
    	 * dependiendo del tipo de variable al que pertenezca la columna actual
    	 * 
    	 * @param bf
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
         * @param resultadoEjecucion
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
