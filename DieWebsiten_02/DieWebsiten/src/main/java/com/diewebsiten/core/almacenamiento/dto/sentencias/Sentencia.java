package com.diewebsiten.core.almacenamiento.dto.sentencias;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class Sentencia {

    private String nombre;
    private String queryString;
    private Stream<String> parametrosSentencia;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Supplier<Stream<String>> getParametrosSentencia() {
        return () -> parametrosSentencia;
    }

    public void setParametrosSentencia(Stream<String> parametrosSentencia) {
        this.parametrosSentencia = parametrosSentencia;
    }

    public int numParametrosSentencia() {
        return (int) this.parametrosSentencia.count();
    }

    public enum TiposResultado {
        PLANO, JERARQUÍA, JERARQUÍA_CON_NOMBRES_DE_COLUMNAS
    }

}
