package com.diewebsiten.core.eventos.dto.transaccion;

import com.diewebsiten.core.almacenamiento.Proveedores;
import com.diewebsiten.core.eventos.dto.transaccion.columnar.Cassandra;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.util.Transformaciones.*;
import static java.util.stream.Collectors.toList;

public class Transacciones {

    public static final Map<String, Class> TIPOS_TRANSACCIONES = new HashMap<>();
    static {
        TIPOS_TRANSACCIONES.put("CASSANDRA", Cassandra.class);
    }
    private static Map<Integer, Transaccion> transacciones = new HashMap<>();

    private Transaccion transaccion;
    private Supplier<Stream<Map<String, Object>>> resultSet;

    private ObjectNode coleccionActual, resultado = newJsonObject();
    private static Object obj = new Object();

    public Transacciones(Transaccion transaccion) {
        this.transaccion = transaccion;
    }

    private static Transaccion obtenerTransaccionExistente(String sentencia) {
        Transaccion transaccionExistente = transacciones.get(sentencia.hashCode());
        if (transaccionExistente == null) {
            synchronized (obj) {
                transaccionExistente = transacciones.get(sentencia.hashCode());
            }
        }
        return transaccionExistente;
    }

    private static void guardarNuevaTransaccion(Transaccion nuevaTransaccion) {
        transacciones.put(nuevaTransaccion.getSentencia().hashCode(), nuevaTransaccion);
    }

    public static Cassandra nuevaTransaccionCassandra(String sentencia, Object... parametros) {
        Cassandra transaccionCassandra = (Cassandra) obtenerTransaccionExistente(sentencia);
        if (transaccionCassandra == null) transaccionCassandra = new Cassandra(sentencia, parametros);
        guardarNuevaTransaccion(transaccionCassandra);
        return transaccionCassandra;
    }

    public JsonNode obtenerResultado() {
        TiposResultado tipoResultado = obtenerTipoResultado(transaccion.getTipoResultado());
        switch (tipoResultado) {
            default: return plano();
            case JERARQUÍA: return enJerarquia();
            case JERARQUÍA_CON_NOMBRES_DE_COLUMNAS: return enJerarquiaConNombres();
        }
    }

    protected JsonNode plano() {
        resultSet = ejecutarTransaccion();
        return listToJsonArray(resultSet.get().collect(toList()));
    }

    protected ObjectNode enJerarquia() {
        prepararTransaccionColumnar();
        resultSet = ejecutarTransaccion();
        return obtenerResultadoEnJerarquia(false);
    }

    protected ObjectNode enJerarquiaConNombres() {
        prepararTransaccionColumnar();
        resultSet = ejecutarTransaccion();
        return obtenerResultadoEnJerarquia(true);
    }

    private Supplier<Stream<Map<String, Object>>> ejecutarTransaccion() {
        return Proveedores.ejecutarTransaccion(transaccion.getMotorAlmacenamiento(),
                                               transaccion.getSentencia(),
                                               transaccion.getParametros());
    }

    private ObjectNode obtenerResultadoEnJerarquia(boolean incluirNombresColumnasPrimarias) {
        coleccionActual = resultado;
        resultSet.get().forEach(fila -> {
            categorizarColumnasPrimarias(fila, incluirNombresColumnasPrimarias);
            categorizarColumnasRegulares(fila);
        });
        return resultado;
    }

    private void categorizarColumnasPrimarias(Map<String, Object> fila, boolean incluirNombresColumnasPrimarias) {
        TransaccionColumnar transaccionColumnar = (TransaccionColumnar) transaccion;
        transaccionColumnar.getColumnasPrimarias().stream()
                .forEach(columnaPrimaria -> {
                    if (incluirNombresColumnasPrimarias) coleccionActual = ponerObjeto(coleccionActual, columnaPrimaria);
                    coleccionActual = ponerObjeto(coleccionActual, fila.get(columnaPrimaria).toString());
                });
    }

    private void categorizarColumnasRegulares(Map<String, Object> fila) {
        TransaccionColumnar transaccionColumnar = (TransaccionColumnar) transaccion;
        transaccionColumnar.getColumnasRegulares().stream()
                .forEach(columnaRegular -> agruparValores(coleccionActual, columnaRegular, objectToValue(fila.get(columnaRegular))));
        coleccionActual = resultado;
    }

    private void prepararTransaccionColumnar() {
        try {
            // TODO 1. que pasa cuando se crea una transaccion dinamicamente desde la base de datos??? ya no seria TransaccionColumnar
            TransaccionColumnar transaccionColumnar = (TransaccionColumnar) transaccion;
            transaccionColumnar.complementarSentencia();
        } catch (ClassCastException e) {
            throw new ExcepcionGenerica("El tipo de transacción no está soportado para obtener un resultado en jerarquía");
        }
    }

    private TiposResultado obtenerTipoResultado(String tipoResultado) {
        try {
            return TiposResultado.valueOf(tipoResultado);
        } catch (RuntimeException e) {
            throw new ExcepcionGenerica("'" + tipoResultado + "' no es un tipo de resultado válido.");
        }
    }

    private enum TiposResultado {
        PLANO, JERARQUÍA, JERARQUÍA_CON_NOMBRES_DE_COLUMNAS
    }

}
