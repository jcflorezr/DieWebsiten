package com.diewebsiten.core.almacenamiento;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletSecurityElement;

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
	private Cluster cluster;
    private Session sesion;
    private Map<String, PreparedStatement> sentenciasPreparadas;

    private ProveedorCassandra() {
    	conectar();
    	prepararSentencias();
    }
    
    /*
     * Unica instancia de la clase ProveedorCassandra.
     */
    public static synchronized ProveedorCassandra getInstance() {
    	if (proveedorCassandra == null) {
    		proveedorCassandra = new ProveedorCassandra();
    	}
    	return proveedorCassandra;	
    }

    /**
     * Establecer una conexión con el motor de base de datos.
     * 
     * 1. Establecer los parámetros de conexión al motor de base de datos.
     * 2. Crear una sesión de conexión a la base de datos.
     * 
     * @return 
     * @throws java.lang.Exception
     */
    @Override
    void conectar() {
        cluster = Cluster.builder().addContactPoint(Constantes.CASSANDRA_URL.getString()).withPort(Constantes.CASSANDRA_PORT.getInt()).build();
        this.sesion = cluster.connect();
    }
    
    /**
     * Cerrar la conexión con el motor de base de datos.
     */
    @Override
    void desconectar() {
        cluster.close();
    }
    
    private void prepararSentencias() {
    	synchronized (ProveedorCassandra.class) {
    		if (null == sentenciasPreparadas) {
    			sentenciasPreparadas = new HashMap<String, PreparedStatement>();
    			sentenciasPreparadas.put(Constantes.NMBR_SNT_TRANSACCIONES.getString(), getSesion().prepare(Constantes.SNT_TRANSACCIONES.getString()));
    			sentenciasPreparadas.put(Constantes.NMBR_SNT_VALIDACIONES_EVENTO.getString(), getSesion().prepare(Constantes.SNT_VALIDACIONES_EVENTO.getString()));
    		}
    	}
    }
    
    /**
     * 
     * Método generico para ejecutar una sentencia CQL (Cassandra Query Language)
     * de tipo consulta.
     * 
     * @param nombreSentencia nombre de la sentencia que se va a extraer para su ejecución
     * @param parametros filtros de búsqueda con que se ejecutará la sentencia
     * @return Conjunto de datos (DataSet)
     * @throws ExcepcionGenerica en caso de que el nombre de la sentencia no coincida con 
     * 							 ninguna de las sentencias existentes
     */
    @Override
    public List<?> consultar(String nombreSentencia, Object... parametros) throws ExcepcionGenerica {
    	PreparedStatement sentenciaPreparada = getSentenciasPreparadas().get(nombreSentencia);
    	if (sentenciaPreparada == null) {
    		throw new ExcepcionGenerica("No se puede ejecutar la sentencia '" + nombreSentencia + "' porque no existe.");
    	} else if (parametros.length == 0) {
    		return getSesion().execute(sentenciaPreparada.getQueryString()).all();
    	} else {
    		return getSesion().execute(sentenciaPreparada.bind(parametros)).all();
    	}
    }
    
    public List<?> consultar(boolean metadata, String nombreSentencia, Object... parametros) throws ExcepcionGenerica {
    	PreparedStatement sentenciaPreparada = getSentenciasPreparadas().get(nombreSentencia);
    	if (sentenciaPreparada == null) {
    		throw new ExcepcionGenerica("No se puede ejecutar la sentencia '" + nombreSentencia + "' porque no existe.");
    	} else if (parametros.length == 0) {
    		return getSesion().execute(sentenciaPreparada.getQueryString()).all();
    	} else {
    		return getSesion().execute(sentenciaPreparada.bind(parametros)).all();
    	}
    }
    
    private List<Row> retornarResultSet () {
    	
    }
    
    private List<Column.>
    
    // =============================
    // ==== Getters and Setters ====
    // =============================
    

    public Map<String, PreparedStatement> getSentenciasPreparadas() {
        return sentenciasPreparadas;
    }
    
    public void agregarSentenciaPreparada(String nombreSentencia, String sentencia) {
    	PreparedStatement sentenciasPreparadas = getSentenciasPreparadas().get(nombreSentencia);
        if (null == sentenciasPreparadas) {
            getSentenciasPreparadas().put(nombreSentencia, getSesion().prepare(sentencia));
        }
    }
    
    private Session getSesion() {
    	return this.sesion;
    }

}
