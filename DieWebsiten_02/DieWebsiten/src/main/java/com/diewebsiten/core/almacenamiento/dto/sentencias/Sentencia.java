package com.diewebsiten.core.almacenamiento.dto.sentencias;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Sentencia {

    private String nombre;
    private String queryString;
    private List<String> filtrosSentencia = new ArrayList<>();

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

    public Supplier<Stream<String>> getFiltrosSentencia() {
        return () -> filtrosSentencia.stream();
    }

    public void setFiltrosSentencia(List<String> parametrosSentencia) {
        this.filtrosSentencia = parametrosSentencia;
    }

    public int numParametrosSentencia() {
        return filtrosSentencia.size();
    }

    public enum TiposResultado {
        PLANO, JERARQUÍA, JERARQUÍA_CON_NOMBRES_DE_COLUMNAS
    }

}
