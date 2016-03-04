package com.diewebsiten.core.almacenamiento;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
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
    	prepararSentenciasIniciales();
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
    
    
    /**
     * Preparar las sentencias CQL que se ejecutan en todos los eventos con el fin de prepararlas una sola vez por cada evento.
     */
    private void prepararSentenciasIniciales() {
    	synchronized (ProveedorCassandra.class) {
    		if (null == sentenciasPreparadas) {
    			sentenciasPreparadas = new HashMap<String, PreparedStatement>();
    			sentenciasPreparadas.put(Constantes.NMBR_SNT_TRANSACCIONES.getString(), getSesion().prepare(Constantes.SNT_TRANSACCIONES.getString()));
    			sentenciasPreparadas.put(Constantes.NMBR_SNT_VALIDACIONES_EVENTO.getString(), getSesion().prepare(Constantes.SNT_VALIDACIONES_EVENTO.getString()));
    		}
    	}
    }
    
    /**
     * Método generico para ejecutar una sentencia CQL (Cassandra Query Language)
     * de tipo consulta sin filtros.
     * 
     * @param sentencia sentencia CQL a ejecutar
     * @return Conjunto de datos (DataSet).
     */
    @Override
    public List<Row> consultar(String sentencia) {
    	return retornarResultSet(null, sentencia);
    }
    
    /**
     * Método generico para ejecutar una sentencia CQL (Cassandra Query Language)
     * de tipo consulta sin filtros. Aparte de eso llena el parámetro @metadata
     * con los nombres de las columnas que componen la consulta.
     * 
     * @param metadata lista a poblar con los nombres de las columnas de la consulta
     * @param sentencia sentencia CQL a ejecutar
     * @return Conjunto de datos (DataSet) y Metadata de la consulta (como parñametro de salida).
     */
    public List<Row> consultar(List<ColumnDefinitions.Definition> metadata, String sentencia) {
    	return retornarResultSet(metadata, sentencia);
    }
    
    /**
     * Método generico para ejecutar una sentencia CQL (Cassandra Query Language)
     * de tipo consulta con filtros.
     * 
     * @param nombreSentencia nombre de la sentencia que se va a extraer para su ejecución
     * @param parametros filtros de búsqueda con que se ejecutará la sentencia
     * @return Conjunto de datos (DataSet)
     * @throws ExcepcionGenerica en caso de que el nombre de la sentencia no coincida con 
     * 							 ninguna de las sentencias existentes
     */
    @Override
    public List<Row> consultar(String nombreSentencia, Object... parametros) throws ExcepcionGenerica {
    	return retornarResultSet(null, nombreSentencia, parametros);
    }
    
    /**
     * Método generico para ejecutar una sentencia CQL (Cassandra Query Language)
     * de tipo consulta con filtros. Aparte de eso llena el parámetro @metadata
     * con los nombres de las columnas que componen la consulta.
     * 
     * @param metadata lista a poblar con los nombres de las columnas de la consulta
     * @param nombreSentencia nombre de la sentencia que se va a extraer para su ejecución
     * @param parametros filtros de búsqueda con que se ejecutará la sentencia
     * @return Conjunto de datos (DataSet) y Metadata de la consulta (como parñametro de salida).
     * @throws ExcepcionGenerica en caso de que el nombre de la sentencia no coincida con 
     * 							 ninguna de las sentencias existentes
     */
    public List<Row> consultar(List<ColumnDefinitions.Definition> metadata, String nombreSentencia, Object... parametros) throws ExcepcionGenerica {
    	return retornarResultSet(metadata, nombreSentencia, parametros);
    }
    
    /**
     * Las sentencias sin filtros no serán preparadas para su ejecución, serán ejecutadas directamente.
     * @param metadata
     * @param nombreSentencia
     * @return
     */
    private List<Row> retornarResultSet (List<ColumnDefinitions.Definition> metadata, String nombreSentencia) {
    	ResultSet resultSet = getSesion().execute(nombreSentencia);
    	if (metadata != null) {
    		metadata = resultSet.getColumnDefinitions().asList();
    	}
    	return resultSet.all();
    }   
    
    /**
     * Las sentencias con filtros edben ser preparadas antes de su ejecución, no serán ejecutadas directamente.
     * @param metadata
     * @param nombreSentencia
     * @param parametros
     * @return
     * @throws ExcepcionGenerica
     */
    private List<Row> retornarResultSet (List<ColumnDefinitions.Definition> metadata, String nombreSentencia, Object... parametros) throws ExcepcionGenerica {
    	PreparedStatement sentenciaPreparada = getSentenciasPreparadas().get(nombreSentencia);
    	ResultSet resultSet;
    	if (sentenciaPreparada == null) {
    		throw new ExcepcionGenerica("No se puede ejecutar la sentencia '" + nombreSentencia + "' porque no existe.");
    	} else {
    		resultSet = getSesion().execute(sentenciaPreparada.bind(parametros));
    	}
    	if (metadata != null) {
    		metadata = resultSet.getColumnDefinitions().asList();
    	}
    	return resultSet.all();
    }
    
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
