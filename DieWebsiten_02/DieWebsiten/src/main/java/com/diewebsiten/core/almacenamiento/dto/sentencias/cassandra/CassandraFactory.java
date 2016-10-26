package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactory;

public class CassandraFactory extends SentenciasFactory {

    private String queryString;
    private boolean sentenciaSimple;

    private static Object obj = new Object();

    CassandraFactory() {}

    public CassandraFactory(String queryString, boolean sentenciaSimple) {
        this.queryString = queryString;
        this.sentenciaSimple = sentenciaSimple;
    }

    @Override
    public Sentencia crearSentencia() {
        Sentencia sentencia = obtenerSentenciaExistente(queryString);
        if (sentencia == null) {
            synchronized (obj) {
                sentencia =  obtenerSentenciaExistente(queryString);
                if (sentencia == null) {
                    sentencia = new Cassandra(queryString, sentenciaSimple);
                    guardarNuevaSentencia(sentencia);
                }
            }
        }
        return sentencia;
    }

    static Cassandra obtenerSentenciaCreada(String queryString) {
        return (Cassandra) obtenerSentenciaExistente(queryString);
    }

}
