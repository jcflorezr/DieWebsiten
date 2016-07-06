package com.diewebsiten.core.almacenamiento;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.almacenamiento.dto.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.SentenciaCassandra;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Administrar las conexiones y transacciones que se realizan al
 * motor de base de datos Cassandra.
 *
 * @author Juan Camilo Flórez Román (www.diewebsiten.com)
 */
public class ProveedorCassandra extends ProveedorAlmacenamiento {

    private static volatile Conexion proveedorCassandra;
    private static volatile boolean iniciar = true;

    private static Cluster cluster;
    private static Session sesion;
    private Map<String, Sentencia> sentenciasPreparadas;
    private static Object obj = new Object();
    
    private static final String CASSANDRA_URL = "localhost";
    private static final int CASSANDRA_PORT = 9042;
    private static final Gson gson = new Gson();
    
    
    private ProveedorCassandra() {
    	sentenciasPreparadas = new HashMap<>();
    	iniciar = false;
    }
    
    /*
     * Unica instancia de la clase ProveedorCassandra.
     */
    static Conexion getInstance() {
    	if (iniciar) {
	    	if (proveedorCassandra == null) {
	    		synchronized(obj) {
	    			if (proveedorCassandra == null) {
						try {
							proveedorCassandra = new Conexion();
							proveedorCassandra.setProveedorAlmacenamiento(new ProveedorCassandra());
							proveedorCassandra.getProveedorAlmacenamiento().get().conectar();
							proveedorCassandra.setConexionExitosa(true);
						} catch (Exception e) {
							proveedorCassandra.setProveedorAlmacenamiento(null);
							proveedorCassandra.setErrorConexion(e);
						}
	    			}
	    		}
	    	}
    	} else {
    		proveedorCassandra.getProveedorAlmacenamiento().get().desconectar();
    	}
    	return proveedorCassandra;
    }

    /**
     * Establecer una conexión con el motor de base de datos.
     * 
     * 1. Establecer los parámetros de conexión al motor de base de datos.
     * 2. Crear una sesión de conexión a la base de datos.
     */
	@Override
	void conectar() {
        cluster = Cluster.builder().addContactPoint(CASSANDRA_URL).withPort(CASSANDRA_PORT).build();
        sesion = cluster.connect();
    }
    
    /**
     * Cerrar la conexión con el motor de base de datos.
     */
	@Override
	void desconectar() {
    	if (cluster != null) {
    		cluster.close();
    	}
    }
    
    /**
     * Tener sentencias preparadas con el fin de reusarlas
     * @param sentenciaCQL
     * @param nombreTransaccion
     * @return
     */
    private SentenciaCassandra obtenerSentencia(String sentenciaCQL, String nombreTransaccion) throws Exception {
    	try {
    		SentenciaCassandra sentencia = (SentenciaCassandra) sentenciasPreparadas.get(nombreTransaccion);
    		if (sentencia == null) {
    			synchronized (obj) {
    				sentencia = (SentenciaCassandra) sentenciasPreparadas.get(nombreTransaccion);
    				if (sentencia == null) {
    					PreparedStatement datosSentencia = sesion.prepare(sentenciaCQL);
    					ColumnDefinitions parametrosSentencia = datosSentencia.getVariables();
    					String keyspaceName, columnfamilyName;
    					if (parametrosSentencia.size() > 0) {    						
    						keyspaceName = parametrosSentencia.getKeyspace(0); columnfamilyName = parametrosSentencia.getTable(0);
    					} else {
    						String query = datosSentencia.getQueryString();
    						String[] s = substringBetween(query, "FROM", ";").trim().split("\\.");
    						try {								
    							keyspaceName = s[0];
    							columnfamilyName = s[1];
							} catch (ArrayIndexOutOfBoundsException e) {
								throw new ExcepcionGenerica("La sentencia '" + query + "' no está bien formada.");
							}
    					}
    					sentencia = new SentenciaCassandra()
    					.setSentenciaPreparada(datosSentencia)
    					.setKeyspaceName(keyspaceName)
    					.setColumnfamilyName(columnfamilyName)
    					.setLlavesPrimarias(obtenerLlavesPrimarias(keyspaceName, columnfamilyName))
    					.setNumeroParametros(parametrosSentencia.size());
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
    
    /**
     * 
     */
    @Override
    public JsonElement ejecutarTransaccion(Transaccion transaccion) throws Exception {

    	String sentenciaCQL = transaccion.getSentencia();
    	String nombreTransaccion = transaccion.getNombre();
    	Object[] parametros = transaccion.getParametrosTransaccion();
    	boolean necesitaResultadoEnJerarquia = transaccion.isResultadoEnJerarquia();
    	
    	if (isBlank(nombreTransaccion)) {
    		throw new ExcepcionGenerica("El nombre de la sentencia no puede venir vacío.");
    	}
    	
    	try {	
    		SentenciaCassandra sentencia = obtenerSentencia(sentenciaCQL, nombreTransaccion);
    		
    		if (parametros.length != sentencia.getNumeroParametros()) {
    			throw new ExcepcionGenerica("La sentencia necesita " + sentencia.getNumeroParametros() + " parámetros para ser ejecutada.");
    		}
    		
    		ResultSet resultadoEjecucion = isEmpty(parametros) ? sesion.execute(sentenciaCQL) : sesion.execute(sentencia.getSentenciaPreparada().bind(parametros));
    		
    		if (resultadoEjecucion.isExhausted()) {
    			if (necesitaResultadoEnJerarquia) return new JsonObject();
    			else return new JsonArray();
    		}
    		
    		if (necesitaResultadoEnJerarquia) {
    			return obtenerResultadoConJerarquia(resultadoEjecucion, sentencia.getLlavesPrimarias());
    		} else {
    			return obtenerResultado(resultadoEjecucion);
    		}
    		 
		} catch (Exception e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? Arrays.asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }
    
    /**
     * 
     * @param resultadoEjecucion
     * @return
     */
    private JsonArray obtenerResultado(ResultSet resultadoEjecucion) {
    	
		Stream<Row> filas = StreamSupport.stream(resultadoEjecucion.spliterator(), false);
		List<ColumnDefinitions.Definition> columnas = resultadoEjecucion.getColumnDefinitions().asList();
		JsonArray resultado = new JsonArray();
		
		filas.forEach(fila -> {
				JsonObject obj = new JsonObject(); 
				columnas.forEach(columna -> obj.add(columna.getName(), gson.toJsonTree(obtenerValorColumnaActual(fila, columna))));
				resultado.add(obj);
				}
		);
		
		return resultado;
		
    }
    
    
    /**
     * 
     * @param resultadoEjecucion
     * @param columnasJerarquia
     * @param resultadoFinal
     * @throws Exception
     */
    private JsonElement obtenerResultadoConJerarquia (ResultSet resultadoEjecucion, List<String> llavesPrimarias) throws Exception {
		
    	JsonElement resultado = new JsonObject();
		
		// EL ORDEN DE LA LISTA DE JERARQUÍA YA VIENE ESTABLECIDO DESDE 
		// LA BASE DE DATOS (TABLA EVENTOS) SEGÚN EL ORDEN EN QUE SE CREARON EN LA TABLA
		
		JsonObject coleccionColumnasJerarquia = resultado.getAsJsonObject();
		List<ColumnDefinitions.Definition> columnasResultadoEjecucion = resultadoEjecucion.getColumnDefinitions().asList();
		List<String> columnasJerarquia = columnasResultadoEjecucion.size() > 1 ? obtenerColumnasIntermedias(llavesPrimarias, resultadoEjecucion.one().getColumnDefinitions()) : new ArrayList<>();
		Iterator<Row> iterador = resultadoEjecucion.iterator();
		
		while (iterador.hasNext()) {
			
			JsonObject coleccionColumnaActual = null;
			Row fila = iterador.next();
			int i = 0;
			
            for (ColumnDefinitions.Definition columnaActual : columnasResultadoEjecucion) {
            	
            	String nombreColumnaActual = columnaActual.getName();
				Object valorColumnaActual = obtenerValorColumnaActual(fila, columnaActual);
            	
            	if (columnaActualEsDeJerarquia(nombreColumnaActual, columnasJerarquia, i)) {
            		coleccionColumnaActual = coleccionColumnasJerarquia.getAsJsonObject(nombreColumnaActual);
            		if (coleccionColumnaActual != null) {
            			coleccionColumnaActual = coleccionColumnaActual.getAsJsonObject(valorColumnaActual.toString());
            			if (coleccionColumnaActual == null) {
							coleccionColumnaActual = obtenerColeccionColumnaActual(coleccionColumnasJerarquia, nombreColumnaActual, valorColumnaActual.toString());
                		}
            		} else {
            			coleccionColumnasJerarquia.add(nombreColumnaActual, new JsonObject());
            			coleccionColumnaActual = obtenerColeccionColumnaActual(coleccionColumnasJerarquia, nombreColumnaActual, valorColumnaActual.toString());
            		}
            		i++;
            	} else {
            		JsonObject coleccionNivelActual = null != coleccionColumnaActual ? coleccionColumnaActual : coleccionColumnasJerarquia;
            		JsonElement valorColumnaRegular = coleccionNivelActual.get(nombreColumnaActual);
            			
            		// Verificar si esta columna ya tiene un valor.
            		// Si es así entonces se creará un JsonArray con todos los valores correspondientes a esta columna.												    	   
            		if (valorColumnaRegular == null) {
            			coleccionNivelActual.addProperty(nombreColumnaActual, valorColumnaActual.toString());
            		} else {
            			if (valorColumnaRegular.isJsonArray()) {
            				valorColumnaRegular.getAsJsonArray().add(new JsonPrimitive(valorColumnaActual.toString()));
            			} else {
            				coleccionNivelActual.add(nombreColumnaActual, new JsonArray());
            				JsonArray valoresColumnaRegular = coleccionNivelActual.get(nombreColumnaActual).getAsJsonArray();
            				valoresColumnaRegular.add(valorColumnaRegular);
            				valoresColumnaRegular.add(new JsonPrimitive(valorColumnaActual.toString()));
            			}
            		}
            	}
            	
            }
            
        }
		
		return resultado;
		
	}
    
    private boolean columnaActualEsDeJerarquia(String nombreColumnaActual, List<String> columnasJerarquia, int i) throws ExcepcionGenerica {
    	if (i >= columnasJerarquia.size()) { // la columna actual no es de jerarquía
    		return false;
    	}    	
    	String columnaJerarquia = columnasJerarquia.get(i);
    	if (nombreColumnaActual.equals(columnaJerarquia)) {
    		return true;
    	} else {
    		throw new ExcepcionGenerica("La columna actual '" + nombreColumnaActual + "'  (que es de jerarquia) no concuerda con la posición actual (" + columnaJerarquia + ") de la lista de columnas de jerarquía. " + columnasJerarquia.toString());
    	}
    }

	private JsonObject obtenerColeccionColumnaActual(JsonObject coleccionColumnasJerarquia, String nombreColumnaActual, String valorColumnaActual) {
		JsonObject coleccionColumnaActual = coleccionColumnasJerarquia.getAsJsonObject(nombreColumnaActual);
		coleccionColumnaActual.add(valorColumnaActual.toString(), new JsonObject());
		return coleccionColumnaActual.getAsJsonObject(valorColumnaActual.toString());
	}

	private Object obtenerValorColumnaActual(Row fila, ColumnDefinitions.Definition columnaActual) {
		ByteBuffer bf = fila.getBytesUnsafe(columnaActual.getName());
		if (bf != null) {
			return columnaActual.getType().deserialize((bf), ProtocolVersion.NEWEST_SUPPORTED);
		} else {
			return obtenerValorVacio(columnaActual);
		}
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
			return "List".equals(tipoColumna) ? new JsonArray() : new JsonObject();
		} else {    							
			if ("String".equals(tipoColumna))
				return "";
			else if ("Integer,Long,Float,Double,BigDecimal,BigInteger".indexOf(tipoColumna) > -1)
				return 0;
			else
				return null;
		}
    }
    
    private List<String> obtenerLlavesPrimarias(String keyspaceName, String columnFamilyName) throws ExcepcionGenerica {
		
    	if (isBlank(keyspaceName) || isBlank(columnFamilyName)) {
			throw new ExcepcionGenerica("Para obtener las llaves primarias de la sentencia actual ninguno de estos valores puede ser nulo. Keyspace Name: " + keyspaceName + ". ColumnFamily Name: " + columnFamilyName);
		}
    	
		String sentenciaLlavesPrimarias = "SELECT key_aliases, column_aliases FROM system.schema_columnfamilies "
				+ "WHERE keyspace_name = '" + keyspaceName + "' and columnfamily_name = '" + columnFamilyName + "';";
		Row llavesPrimarias = sesion.execute(sentenciaLlavesPrimarias).one();

		Type listStringType = new TypeToken<List<String>>(){private static final long serialVersionUID = 1L;}.getType();
		Gson gson = new Gson();
		
		List<String> listaLlavesPrimarias = new ArrayList<>();
		listaLlavesPrimarias.addAll(gson.fromJson(llavesPrimarias.getString("key_aliases"), listStringType));
		listaLlavesPrimarias.addAll(gson.fromJson(llavesPrimarias.getString("column_aliases"), listStringType));
		
		return listaLlavesPrimarias;
		
    }
    
    private List<String> obtenerColumnasIntermedias(List<String> llavesPrimarias, ColumnDefinitions metadataResultadoEjecucion) {
    	List<String> columnasIntermedias = new ArrayList<>();
    	int i = 0;
    	for (String llavePrimaria : llavesPrimarias) {
    		if (llavePrimaria.equals(metadataResultadoEjecucion.getName(i))) {
    			columnasIntermedias.add(llavePrimaria);
    			i++;
    		}
    	}
    	return columnasIntermedias;
    }
    
}
