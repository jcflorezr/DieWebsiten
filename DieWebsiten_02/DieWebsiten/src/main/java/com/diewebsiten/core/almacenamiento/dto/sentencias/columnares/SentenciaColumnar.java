package com.diewebsiten.core.almacenamiento.dto.sentencias.columnares;

import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;

import java.util.List;

public abstract class SentenciaColumnar extends Sentencia {

    protected List<String> columnasPrimarias;
    protected List<String> columnasRegulares;

    public abstract List<String> getColumnasPrimarias();

    public abstract List<String> getColumnasRegulares();

}
