package com.diewebsiten.core.almacenamiento.dto.sentencias;

import java.util.HashMap;
import java.util.Map;

public abstract class SentenciasFactory {

    private static Map<Integer, Sentencia> sentencias = new HashMap<>();

    // Sentencias por defecto para los diferentes motores de bases de datos
    static {

    }

    public static Sentencia obtenerSentenciaExistente(String queryString) {
        return sentencias.get(queryString.hashCode());
    }

    public static void guardarNuevaSentencia(Sentencia sentenciaPreparada) {
        int idSentencia = sentenciaPreparada.getQueryString().hashCode();
        sentencias.put(idSentencia, sentenciaPreparada);
    }

    protected abstract Sentencia crearSentencia();

}
