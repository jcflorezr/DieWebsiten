package com.diewebsiten.core.almacenamiento.cassandra;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.almacenamiento.ProveedorAlmacenamiento;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;
import com.google.common.base.Throwables;
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

    private static ProveedorCassandra proveedorCassandra;
	private static Cluster cluster;
    private static Session sesion;
    private Map<String, PreparedStatement> sentenciasPreparadas;
    private static Object obj = new Object();
    
    private ProveedorCassandra() {
    	sentenciasPreparadas = new HashMap<>();
    }
    
    /*
     * Unica instancia de la clase ProveedorCassandra.
     */
    public static ProveedorCassandra getInstance(boolean iniciar) {
    	if (iniciar) {
	    	if (proveedorCassandra == null) {
	    		synchronized(obj) {
	    			if (proveedorCassandra == null) {	    				
	    				proveedorCassandra = new ProveedorCassandra();
	    				proveedorCassandra.conectar();
	    			}
	    		}
	    	}
    	} else {
    		proveedorCassandra.desconectar();
    	}
    	return proveedorCassandra;
    }

    /**
     * Establecer una conexión con el motor de base de datos.
     * 
     * 1. Establecer los parámetros de conexión al motor de base de datos.
     * 2. Crear una sesión de conexión a la base de datos.
     */
    private void conectar() {
        cluster = Cluster.builder().addContactPoint(Constantes.CASSANDRA_URL.getString()).withPort(Constantes.CASSANDRA_PORT.getInt()).build();
        sesion = cluster.connect();
    }
    
    /**
     * Cerrar la conexión con el motor de base de datos.
     */
    private void desconectar() {
    	if (cluster != null) {
    		cluster.close();
    	}
    }
    
    /**
     * Tener sentencias preparadas con el fin de reusarlas
     * @param sentenciaCQL
     * @param nombreSentencia
     * @return
     */
    private PreparedStatement obtenerSentenciaPreparada(String sentenciaCQL, String nombreSentencia) throws Exception {
    	try {			
    		PreparedStatement sentenciaPreparada = sentenciasPreparadas.get(nombreSentencia);
    		if (sentenciaPreparada == null) {
    			synchronized (obj) {
    				if (sentenciasPreparadas.get(nombreSentencia) == null) {
    					sentenciaPreparada = sesion.prepare(sentenciaCQL);
    					sentenciasPreparadas.put(nombreSentencia, sentenciaPreparada);
    				}
    			}
    		}
        	return sentenciaPreparada;
		} catch (Exception e) {
			throw new ExcepcionGenerica("Error al preparar la nueva sentencia CQL. Nombre: '" + nombreSentencia + "'. Sentencia: " + sentenciaCQL + ". Mensaje original --> " + e.getMessage());
		}
    }
    
    /**
     * 
     */
    @Override
    public List<JsonObject> ejecutarTransaccion(String sentenciaCQL, String nombreSentencia, Object[] parametros) throws Exception {
    	try {    		
	    	if (parametros == null || parametros.length == 0) {
	    		return obtenerResultado(sesion.execute(sentenciaCQL));
	    	} else if (isBlank(nombreSentencia)) {
	    		throw new ExcepcionGenerica("Si la sentencia sí tiene parámetros, el nombre de la sentencia no puede venir vacío.");
	    	}
	    	PreparedStatement sentenciaPreparada = obtenerSentenciaPreparada(sentenciaCQL, nombreSentencia);
			return obtenerResultado(sesion.execute(sentenciaPreparada.bind(parametros)));
		} catch (Exception e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? Arrays.asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }
    
    /**
     * 
     * @param sentenciaCQL
     * @param nombreSentencia
     * @param parametros
     * @param resultadoFinal
     * @throws ExceptionGenerica
     */
    public void ejecutarTransaccion(String sentenciaCQL, String nombreSentencia, Object[] parametros, List<String> columnasJerarquia, JsonObject resultadoFinal) throws Exception {
    	try {    		
	    	if (parametros == null || parametros.length == 0) {
	    		obtenerResultadoConJerarquia(sesion.execute(sentenciaCQL), columnasJerarquia, resultadoFinal);
	    	} else if (isBlank(nombreSentencia)) {
	    		throw new ExcepcionGenerica("Si la sentencia sí tiene parámetros, el nombre de la sentencia no puede venir vacío.");
	    	}
	    	PreparedStatement sentenciaPreparada = obtenerSentenciaPreparada(sentenciaCQL, nombreSentencia);
	    	ResultSet resultadoEjecucion = sesion.execute(sentenciaPreparada.bind(parametros)); 
			obtenerResultadoConJerarquia(resultadoEjecucion, columnasJerarquia, resultadoFinal);
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
    private List<JsonObject> obtenerResultado(ResultSet resultadoEjecucion) {
    	
    	Iterator<Row> iterador = resultadoEjecucion.iterator();
		List<ColumnDefinitions.Definition> columnasResultadoEjecucion = resultadoEjecucion.getColumnDefinitions().asList();
		List<JsonObject> resultado = new ArrayList<>();
		
		while (iterador.hasNext()) {

			Map<String, Object> filaMap = new HashMap<>();
			Row fila = iterador.next();
			
			for (ColumnDefinitions.Definition columnaActual : columnasResultadoEjecucion) {
				
				String nombreColumnaActual = columnaActual.getName();
				ByteBuffer bf = fila.getBytesUnsafe(nombreColumnaActual);
				
				Object valorColumnaActual;
				if (bf != null) {
        			valorColumnaActual = columnaActual.getType().deserialize((bf), ProtocolVersion.NEWEST_SUPPORTED);
        		} else {
        			valorColumnaActual = obtenerValorVacio(bf, columnaActual);
        		}
				
				filaMap.put(nombreColumnaActual, valorColumnaActual);
				
			}
			
			resultado.add(new JsonParser().parse(new Gson().toJson(filaMap)).getAsJsonObject());
			
		}
		
		return resultado;
		
    }
    
    
    /**
     * 
     * @param resultadoEjecucion
     * @param parametrosTransaccion
     * @param nombresParametrosTransaccion
     * @param columnasJerarquia
     * @param resultadoFinal
     * @throws Exception
     */
    private void obtenerResultadoConJerarquia (ResultSet resultadoEjecucion, List<String> columnasJerarquia, JsonObject resultadoFinal) throws Exception {
		
		if (resultadoFinal == null) {
			throw new ExcepcionGenerica("El JsonObject debe estar inicializado");
		}
		
		// EL ORDEN DE LA LISTA DE JERARQUÍA YA VIENE ESTABLECIDO DESDE 
		// LA BASE DE DATOS (TABLA EVENTOS) SEGÚN EL ORDEN EN QUE SE CREARON EN LA TABLA
		
		JsonObject coleccionColumnasJerarquia = resultadoFinal;
		List<ColumnDefinitions.Definition> columnasResultadoEjecucion = resultadoEjecucion.getColumnDefinitions().asList();
		Iterator<Row> iterador = resultadoEjecucion.iterator();
		
		while (iterador.hasNext()) {
			
			JsonObject coleccionColumnaActual = null;
			Row fila = iterador.next();
			int i = 0;
			
            for (ColumnDefinitions.Definition columnaActual : columnasResultadoEjecucion) {
            	
            	String nombreColumnaActual = columnaActual.getName();
            	ByteBuffer bf = fila.getBytesUnsafe(nombreColumnaActual);
            	
            	Object valorColumnaActual;
            	if (bf != null) {
        			valorColumnaActual = columnaActual.getType().deserialize((bf), ProtocolVersion.NEWEST_SUPPORTED);
        		} else {
        			valorColumnaActual = obtenerValorVacio(bf, columnaActual);
        		}
            	
            	if (columnaActualEsDeJerarquia(nombreColumnaActual, columnasJerarquia, i)) {
            		coleccionColumnaActual = coleccionColumnasJerarquia.getAsJsonObject(nombreColumnaActual);
            		if (coleccionColumnaActual != null) {
            			coleccionColumnaActual = coleccionColumnaActual.getAsJsonObject((String) valorColumnaActual);
            			if (coleccionColumnaActual == null) {
            				coleccionColumnaActual = coleccionColumnasJerarquia.getAsJsonObject(nombreColumnaActual);
            				coleccionColumnaActual.add((String) valorColumnaActual, new JsonObject());
            				coleccionColumnaActual = coleccionColumnaActual.getAsJsonObject((String) valorColumnaActual);
                		}
            		} else {
            			coleccionColumnasJerarquia.add(nombreColumnaActual, new JsonObject());
            			coleccionColumnaActual = coleccionColumnasJerarquia.getAsJsonObject(nombreColumnaActual);
            			coleccionColumnaActual.add((String) valorColumnaActual, new JsonObject());
            			coleccionColumnaActual = coleccionColumnaActual.getAsJsonObject((String) valorColumnaActual);
            		}
            		i++;
            	} else {
            		JsonObject coleccionNivelActual = null;
            		
        			if (null != coleccionColumnaActual) { // La columna regular es hija de la última columna de la lista de jerarquía
        				coleccionNivelActual = coleccionColumnaActual;
        			} else if (null != coleccionColumnasJerarquia) { // La columna regular es hermana de una columna de la lista de jerarquía
        				coleccionNivelActual = coleccionColumnasJerarquia;
        			}
            			
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
    
    /**
     * Si el valor de la columna actual es nulo se guardará como valor un "valor vacío"
     * dependiendo del tipo de variable al que pertenezca la columna actual
     * 
     * @param bf
     * @param columnaActual
     * @return
     */
    private Object obtenerValorVacio(ByteBuffer bf, ColumnDefinitions.Definition columnaActual) {
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
    
}
