package com.diewebsiten.core.almacenamiento.dto.sentencias;

import java.util.HashMap;
import java.util.Map;

public abstract class SentenciasFactory {

    private static Map<Integer, Sentencia> sentencias = new HashMap<>();

    protected static Sentencia obtenerSentenciaExistente(String queryString) {
        return sentencias.get(queryString.hashCode());
    }

    protected static void guardarNuevaSentencia(Sentencia sentencia) {
        int idSentencia = sentencia.getQueryString().hashCode();
        sentencias.put(idSentencia, sentencia);
    }

    protected abstract Sentencia crearSentencia();

}
