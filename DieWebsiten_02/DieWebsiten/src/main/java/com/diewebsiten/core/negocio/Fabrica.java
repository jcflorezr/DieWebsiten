package com.diewebsiten.core.negocio;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.util.Constantes;

/**
 * Esta clase se encarga de hacer conexión con la base de datos
 *
 * @author Juan Camilo Flórez Román (www.diewebsiten.com)
 */
public class Fabrica {

    private static Cluster cluster;

    public Fabrica() {
    }

    /**
     * Método privado que es usado por el método transaccion para comunicarse
     * con la base de datos y extraer o modificar información.
     * @return 
     * @throws java.lang.Exception
     */
    public static Session conectar() {

        // Pasar los parámetros de conexión al motor de base de datos.
        cluster = Cluster.builder().addContactPoint(Constantes.CASSANDRA_URL.getString()).withPort(Constantes.CASSANDRA_PORT.getInt()).build();

        // Crear una sesión de conexión.
        return cluster.connect();

    }// conectar

    /**
     * Cerrar clúster.
     */
    public static void cerrarConexion() {
        cluster.close();
    }

}
