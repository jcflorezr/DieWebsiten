package com.diewebsiten.core.almacenamiento;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;

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
	    	}
    	} else {
    		proveedorCassandra.desconectar();
    	}
    	return proveedorCassandra;	
    }

    private ProveedorCassandra() {
    	conectar();
    	prepararSentenciasIniciales();
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
        cluster.close();
    }
    
    /**
     * HAY QUE ENCONTRAR LA FORMA DE QUE ESTE METODO EJECUTE TODO TIPO DE SENTENCIA.. NO SOLO LAS DE CONSULTA
     * @return
     */
    private List<Row> ejecutarSentenciaTipoConsulta(Object sentencia, Object[] parametros) throws ExcepcionGenerica {
    	try {
    		if (null == parametros) {
    			return sesion.execute((String)sentencia).all();
    		} else {
    			return sesion.execute(((PreparedStatement) sentencia).bind(parametros)).all();
    		}
		} catch (Exception e) {
			String sentenciaError = sentencia instanceof String ? sentencia.toString() : ((PreparedStatement)sentencia).getQueryString();
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaError + "'. Parámetros: " + Arrays.asList(parametros).toString() + ". Mensaje original --> " + e.getMessage());
		}
    }
    
    /**
     * 
     */
    @Override
    public List<Row> consultar(String sentenciaCQL) throws ExcepcionGenerica {
    	return ejecutarSentenciaTipoConsulta(sentenciaCQL, null);
    }
    
    /**
     * Método generico para ejecutar una sentencia CQL (Cassandra Query Language)
     * de tipo consulta. Las sentencias con filtros deben ser preparadas 
     * antes de su ejecución, no serán ejecutadas directamente.
     * 
     * @param nombreSentencia nombre de la sentencia que se va a extraer para su ejecución
     * @param parametros filtros de búsqueda con que se ejecutará la sentencia
     * @return Conjunto de datos (DataSet)
     * @throws ExcepcionGenerica en caso de que el nombre de la sentencia no coincida con 
     * 							 ninguna de las sentencias existentes
     */
    @Override
    public List<Row> consultar(String nombreSentencia, Object[] parametros) throws ExcepcionGenerica {
    	PreparedStatement sentenciaPreparada = sentenciasPreparadas.get(nombreSentencia);
    	if (sentenciaPreparada == null) {
    		throw new ExcepcionGenerica("No se puede ejecutar la sentencia '" + nombreSentencia + "' porque no existe.");
    	} else {
    		return ejecutarSentenciaTipoConsulta(sentenciaPreparada, parametros);
    	}
    }
    
    /**
     * 
     */
    @Override
    public List<Row> consultar(String sentenciaCQL, String nombreSentencia, Object[] parametros) throws ExcepcionGenerica {
    	if (null == parametros) {
    		return ejecutarSentenciaTipoConsulta(sentenciaCQL, null);
    	}
    	PreparedStatement sentenciaPreparada = sentenciasPreparadas.get(nombreSentencia);
    	if (null == sentenciaPreparada) {
    		sentenciaPreparada = agregarSentenciaPreparada(sentenciaCQL, nombreSentencia);
    	}
    	return ejecutarSentenciaTipoConsulta(sentenciaPreparada, parametros);
    }
    
    /**
     * Preparar las sentencias CQL que se ejecutan en todos los eventos con el fin de prepararlas una sola vez por cada evento.
     */
    private void prepararSentenciasIniciales() {
		if (null == sentenciasPreparadas) {
			sentenciasPreparadas = new HashMap<String, PreparedStatement>();
			sentenciasPreparadas.put(Constantes.NMBR_SNT_TRANSACCIONES.getString(), sesion.prepare(Constantes.SNT_TRANSACCIONES.getString()));
			sentenciasPreparadas.put(Constantes.NMBR_SNT_VALIDACIONES_EVENTO.getString(), sesion.prepare(Constantes.SNT_VALIDACIONES_EVENTO.getString()));
		}
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
    private synchronized PreparedStatement agregarSentenciaPreparada(String sentenciaCQL, String nombreSentencia) throws ExcepcionGenerica {
    	try {			
    		PreparedStatement sentenciaPreparada = sesion.prepare(sentenciaCQL);
    		sentenciasPreparadas.put(nombreSentencia, sentenciaPreparada);
        	return sentenciaPreparada;
		} catch (Exception e) {
			throw new ExcepcionGenerica("Error al preparar la nueva sentencia CQL. Nombre: '" + nombreSentencia + "'. Sentencia: " + sentenciaCQL + ". Mensaje original --> " + e.getMessage());
		}
    }

}
