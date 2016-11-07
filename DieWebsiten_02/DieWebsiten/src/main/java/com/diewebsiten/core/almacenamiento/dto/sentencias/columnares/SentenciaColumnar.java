package com.diewebsiten.core.almacenamiento.dto.sentencias.columnares;

import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;

import java.util.List;

public abstract class SentenciaColumnar extends Sentencia {

    public abstract List<String> getColumnasIntermedias();

    public abstract List<String> getColumnasRegulares();

}
