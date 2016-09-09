package com.diewebsiten.core.almacenamiento.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Sentencia {

    private static Map<String, Sentencia> sentenciasPreparadas = new HashMap<>();

    public static Optional<Sentencia> getSentenciaPreparada(String nombreSentencia) {
        if (sentenciasPreparadas.isEmpty()) return Optional.empty();
        else return Optional.ofNullable(sentenciasPreparadas.get(nombreSentencia));
    }

    public static void setSentenciaPreparada(String nombreSentencia, Sentencia sentenciaPreparada) {
        sentenciasPreparadas.put(nombreSentencia, sentenciaPreparada);
    }

    public enum TiposResultado {
        PLANO, JERARQUÍA, JERARQUÍA_CON_NOMBRES_DE_COLUMNAS
    }

}
