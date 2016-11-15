package com.diewebsiten.core.eventos.dto.transaccion;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.diewebsiten.core.util.Transformaciones.removerCaracteres;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public abstract class TransaccionColumnar extends Transaccion {

    protected String baseDeDatos;
    protected String tabla;
    protected List<String> columnasPrimarias;
    protected List<String> columnasRegulares;

    private List<String> columnasQuery;

    public TransaccionColumnar(){
        columnasPrimarias = new ArrayList<>();
        columnasRegulares = new ArrayList<>();
    }

    protected TransaccionColumnar(String sentencia, String motorAlmacenamiento, Object... parametros) {
        super(sentencia, motorAlmacenamiento, parametros);
    }

    public abstract List<String> getColumnasPrimarias();

    public abstract List<String> getColumnasRegulares();

    public abstract void complementarSentencia();

    public String getBaseDeDatos() {
        return baseDeDatos;
    }

    protected void setBaseDeDatos() {
        baseDeDatos = obtenerDatoDesdeSentencia(true);
    }

    public String getTabla() {
        return tabla;
    }

    protected void setTabla() {
        tabla = obtenerDatoDesdeSentencia(false);
    }

    protected void setFiltrosSentencia() {
        String sentencia = substringAfter(getSentencia(), Separadores.WHERE);
        if (isBlank(sentencia)) return;
        if (endsWith(sentencia, Separadores.PUNTO_Y_COMA)) sentencia = chop(sentencia);
        String[] filtros = sentencia.split(Separadores.AND);
        List<String> filtrosList = new ArrayList<>();
        for(String filtro : filtros) filtrosList.add(removerCaracteres(filtro, Separadores.IGUAL, Separadores.PARAM).trim());
        setFiltrosSentencia(filtrosList);
    }

    protected Stream<String> getColumnasQuery() {
        return columnasQuery.stream();
    }

    protected void setColumnasQuery() {
        columnasQuery = asList(substringBetween(getSentencia(), SPACE, Separadores.FROM).split(Separadores.COMA)).stream()
                .map(columna -> columna.trim())
                .collect(toList());
    }

    private String obtenerDatoDesdeSentencia(boolean obtenerNombreBaseDeDatos) {
        String dato = contains(getSentencia(), Separadores.WHERE)
                ? substringBetween(getSentencia(), Separadores.FROM, Separadores.WHERE)
                : contains(getSentencia(), Separadores.PUNTO_Y_COMA)
                ? substringBetween(getSentencia(), Separadores.FROM, Separadores.PUNTO_Y_COMA)
                : substringAfter(getSentencia(), Separadores.FROM);
        return (obtenerNombreBaseDeDatos ? substringBefore(dato, Separadores.PUNTO)
                                         : substringAfter(dato, Separadores.PUNTO)).trim();
    }

    protected boolean queryContieneUnicaColumna() {
        return columnasQuery.size() == 1;
    }

    protected boolean queryContieneSoloColumnasPrimarias(List<String> columnasPrimarias) {
        int numColumnasPrimarias = columnasPrimarias.size();
        return numColumnasPrimarias > 0 && columnasQuery.size() == numColumnasPrimarias;
    }

    protected void convertirUltimaColumnaARegular(List<String> columnasPrimarias) {
        columnasPrimarias.remove(columnasPrimarias.size() - 1);
    }

    public ObjectNode enJerarquia() {
        return new Transacciones(this).enJerarquia();
    }

    public ObjectNode enJerarquiaConNombres() {
        return new Transacciones(this).enJerarquiaConNombres();
    }

    private interface Separadores {
        String FROM = "FROM";
        String WHERE = "WHERE";
        String AND = "AND";
        String COMA = ",";
        String PUNTO_Y_COMA = ";";
        String PUNTO = ".";
        String PARAM = "?";
        String IGUAL = "=";
    }

}
