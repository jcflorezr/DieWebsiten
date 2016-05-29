package com.diewebsiten.core.almacenamiento;

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
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    
    /*
     * Unica instancia de la clase ProveedorCassandra.
     */
    public static synchronized ProveedorCassandra getInstance(boolean iniciar) {
    	if (iniciar) {
	    	if (proveedorCassandra == null) {
	    		proveedorCassandra = new ProveedorCassandra();
	    		proveedorCassandra.conectar();
	    	}
    	} else {
    		proveedorCassandra.desconectar();
    	}
    	return proveedorCassandra;
    }

    private ProveedorCassandra() {
    	sentenciasPreparadas = new HashMap<>();
    }
    

    /**
     * Establecer una conexión con el motor de base de datos.
     * 
     * 1. Establecer los parámetros de conexión al motor de base de datos.
     * 2. Crear una sesión de conexión a la base de datos.
     */
    @Override
    void conectar() {
        cluster = Cluster.builder().addContactPoint(Constantes.CASSANDRA_URL.getString()).withPort(Constantes.CASSANDRA_PORT.getInt()).build();
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
     * HAY QUE ENCONTRAR LA FORMA DE QUE ESTE METODO EJECUTE TODO TIPO DE SENTENCIA.. NO SOLO LAS DE CONSULTA
     * @return
     */
    private List<JsonObject> ejecutarSentenciaTipoConsulta(Object sentencia, Object[] parametros) throws ExcepcionGenerica {
    	try {
    		
    		ResultSet rs;
    		
    		if (null == parametros) {
    			rs = sesion.execute((String)sentencia);
    		} else {
    			rs = sesion.execute(((PreparedStatement) sentencia).bind(parametros));    			
    		}
    		
    		
    		Iterator<Row> it = rs.iterator();
			List<ColumnDefinitions.Definition> cols = rs.getColumnDefinitions().asList();
			List<JsonObject> ljobj = new ArrayList<>();
			JsonObject jobj = new JsonObject();
			Map<String, Object> map = new HashMap<>();
			
			
			
			
			
			
			
			// SACAR ESTE WHILE EN UN METODO APARTE
			
			while (it.hasNext()) {
				Row fila = it.next();
				for (ColumnDefinitions.Definition columnaActual : cols) { 
					ByteBuffer bf = fila.getBytesUnsafe(columnaActual.getName());
					Object valorColumnaActual;
					if (bf != null) {
						valorColumnaActual = columnaActual.getType().deserialize((bf), ProtocolVersion.NEWEST_SUPPORTED);
					} else {
						String tipoColumna = columnaActual.getType().asJavaClass().getSimpleName();
						if (columnaActual.getType().isCollection()) {
							valorColumnaActual = "List".equals(tipoColumna) ? new JsonArray() : new JsonObject();
						} else {    							
							if ("String".equals(tipoColumna)) {
								valorColumnaActual = "";
							} else if ("Integer,Long,Float,Double,BigDecimal,BigInteger".indexOf(tipoColumna) > -1) {
								valorColumnaActual = 0;
							} else {
								valorColumnaActual = null;
							}
						}
					}
					
					map.put(columnaActual.getName(), valorColumnaActual);
					
				}
				jobj = new JsonParser().parse(new Gson().toJson(map)).getAsJsonObject();
				ljobj.add(jobj);
			}
			
			
			
			
			
			
			
			
			
			
			
			
			return ljobj;
    		
    		
		} catch (Exception e) {
			String sentenciaError = sentencia instanceof String ? sentencia.toString() : ((PreparedStatement)sentencia).getQueryString();
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaError + "'. Parámetros: " + Arrays.asList(parametros).toString() + ". Mensaje original --> " + e);
		}
    }
    
    
    
    /**
     * 
     */
    @Override
    public List<JsonObject> consultar(DetallesSentencias detallesSentencia) throws ExcepcionGenerica {
    	
    	String sentenciaCQL = detallesSentencia.getSentencia(); 
    	String nombreSentencia = detallesSentencia.getNombreSentencia();
    	Object[] parametros = detallesSentencia.getParametrosSentencia();
    	
    	if (null == parametros) {
    		return ejecutarSentenciaTipoConsulta(sentenciaCQL, null);
    	}
    	
    	PreparedStatement sentenciaPreparada;
    	synchronized (this) {
			sentenciaPreparada = sentenciasPreparadas.get(nombreSentencia);
			if (null == sentenciaPreparada) {
				sentenciaPreparada = agregarSentenciaPreparada(sentenciaCQL, nombreSentencia);
			}
		}
		return ejecutarSentenciaTipoConsulta(sentenciaPreparada, parametros);
    	
    }
    
    
    
    
    // =============================
    // ==== Getters and Setters ====
    // =============================

    /**
     * Tener sentencias preparadas con el fin de reusarlas
     * @param sentenciaCQL
     * @param nombreSentencia
     * @return
     */
    private PreparedStatement agregarSentenciaPreparada(String sentenciaCQL, String nombreSentencia) throws ExcepcionGenerica {
    	try {			
    		PreparedStatement sentenciaPreparada = sesion.prepare(sentenciaCQL);
    		sentenciasPreparadas.put(nombreSentencia, sentenciaPreparada);
        	return sentenciaPreparada;
		} catch (Exception e) {
			throw new ExcepcionGenerica("Error al preparar la nueva sentencia CQL. Nombre: '" + nombreSentencia + "'. Sentencia: " + sentenciaCQL + ". Mensaje original --> " + e.getMessage());
		}
    }

}
