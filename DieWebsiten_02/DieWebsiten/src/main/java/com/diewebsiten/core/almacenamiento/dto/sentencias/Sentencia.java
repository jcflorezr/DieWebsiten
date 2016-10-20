package com.diewebsiten.core.almacenamiento.dto.sentencias;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.PreparedStatement;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class Sentencia {

    private String nombre;
    private String queryString;
    private List<String> parametrosSentencia;

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
        return () -> parametrosSentencia.stream();
    }

    public void setParametrosSentencia(PreparedStatement sentenciaPreparada) {
        Spliterator<Definition> parametrosSpliterator = sentenciaPreparada.getVariables().spliterator();
        this.parametrosSentencia = stream(parametrosSpliterator, false).map(Definition::getName).collect(toList());
    }

    public int numParametrosSentencia() {
        return parametrosSentencia.size();
    }

    public enum TiposResultado {
        PLANO, JERARQUÍA, JERARQUÍA_CON_NOMBRES_DE_COLUMNAS
    }

}
