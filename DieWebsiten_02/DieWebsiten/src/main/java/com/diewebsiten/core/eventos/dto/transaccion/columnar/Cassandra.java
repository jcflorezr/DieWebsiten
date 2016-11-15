package com.diewebsiten.core.eventos.dto.transaccion.columnar;

import com.diewebsiten.core.eventos.dto.transaccion.TransaccionColumnar;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.diewebsiten.core.eventos.dto.transaccion.Transacciones.nuevaTransaccionCassandra;
import static com.diewebsiten.core.util.Transformaciones.stringToList;
import static java.util.stream.Collectors.toList;

public class Cassandra extends TransaccionColumnar {

    private static final String LLAVES_PRIMARIAS = "SELECT key_aliases, column_aliases FROM system.schema_columnfamilies WHERE keyspace_name = ? AND columnfamily_name = ?;";
    private static final String KEY_ALIASES = "key_aliases";
    private static final String COLUMN_ALIASES = "column_aliases";

    public Cassandra() {}

    public Cassandra(String sentencia, Object... parametros) {
        super(sentencia, "CASSANDRA", parametros);
    }

    @Override
    public void complementarSentencia() {
        setBaseDeDatos();
        setTabla();
        setFiltrosSentencia();
        setColumnasQuery();
        if (queryContieneUnicaColumna())
            columnasRegulares = getColumnasQuery().collect(toList());
        else {
            setColumnasPrimarias();
            setColumnasRegulares();
        }
    }

    @Override
    public List<String> getColumnasPrimarias() {
        return Collections.unmodifiableList(columnasPrimarias);
    }

    void setColumnasPrimarias() {
        ObjectNode llavesPrimarias = nuevaTransaccionCassandra(LLAVES_PRIMARIAS, getBaseDeDatos(), getTabla()).conUnicaFila();
        List<String> columnasPrimarias =
                Stream.of(stringToList(llavesPrimarias.get(KEY_ALIASES).asText(), String.class),
                          stringToList(llavesPrimarias.get(COLUMN_ALIASES).asText(), String.class))
                        .flatMap(List::stream)
                        .filter(llavePrimaria -> getFiltrosSentencia().stream().noneMatch(parametro -> llavePrimaria.equals(parametro)))
                        .map(columnaPrimaria -> getColumnasQuery().filter(columna -> columna.equals(columnaPrimaria)).findFirst().get())
                        .collect(toList());
        if (queryContieneSoloColumnasPrimarias(columnasPrimarias)) convertirUltimaColumnaARegular(columnasPrimarias);
        this.columnasPrimarias = columnasPrimarias;
    }

    @Override
    public List<String> getColumnasRegulares() {
        return Collections.unmodifiableList(columnasRegulares);
    }

    void setColumnasRegulares() {
        this.columnasRegulares = getColumnasQuery()
                                    .filter(columna -> getColumnasPrimarias().stream().
                                            noneMatch(columnaIntermedia -> columnaIntermedia.equals(columna)))
                                    .collect(toList());
    }
}
