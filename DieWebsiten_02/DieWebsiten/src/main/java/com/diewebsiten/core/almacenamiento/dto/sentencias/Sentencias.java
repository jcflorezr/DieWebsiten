package com.diewebsiten.core.almacenamiento.dto.sentencias;

public class Sentencias {

    public static Sentencia obtenerSentencia(SentenciasFactory sentencia) {
        return sentencia.crearSentencia();
    }

}
