package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.SentenciaColumnar;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.diewebsiten.core.util.Transformaciones.agruparValores;
import static com.diewebsiten.core.util.Transformaciones.ponerObjeto;
import static java.util.stream.Collectors.toList;

public class ResultadoTransaccion {

    private Stream<Map<String, Object>> resultSet;
    private SentenciaColumnar sentencia;
    private TiposResultado tipoResultado;
    private List<String> columnasPrimarias;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private ObjectNode coleccionActual, resultado = MAPPER.createObjectNode();

    public ResultadoTransaccion(Stream<Map<String, Object>> resultSet, SentenciaColumnar sentencia, TiposResultado tipoResultado) {
        this.resultSet = resultSet;
        this.sentencia = sentencia;
        this.tipoResultado = tipoResultado;
        this.columnasPrimarias = sentencia.getColumnasIntermedias();
    }

    public JsonNode obtenerResultado() {
        switch (tipoResultado) {
            case PLANO: return obtenerResultadoPlano();
            case JERARQUÍA: return obtenerResultadoConJerarquia(false);
            case JERARQUÍA_CON_NOMBRES_DE_COLUMNAS: return obtenerResultadoConJerarquia(true);
            default: throw new ExcepcionGenerica("El tipo de resultado: '" + tipoResultado + "', no es válido.");
        }
    }

    private ArrayNode obtenerResultadoPlano() {
        List<Map<String, Object>> resultSetList = resultSet.collect(toList());
        return MAPPER.convertValue(resultSetList, ArrayNode.class);
    }

    private ObjectNode obtenerResultadoConJerarquia (boolean incluirNombresColumnasPrimarias) {
        coleccionActual = resultado;
        resultSet.forEach(fila -> {
            categorizarColumnasPrimarias(fila, incluirNombresColumnasPrimarias);
            categorizarColumnasRegulares(fila);
        });
        return resultado;
    }

    private void categorizarColumnasPrimarias(Map<String, Object> fila, boolean incluirNombresColumnasPrimarias) {
        sentencia.getColumnasIntermedias().stream()
            .forEach(columnaIntermedia -> {
                if (incluirNombresColumnasPrimarias) coleccionActual = ponerObjeto.apply(coleccionActual, columnaIntermedia);
                coleccionActual = ponerObjeto.apply(coleccionActual, fila.get(columnaIntermedia).toString());
            });
    }

    private void categorizarColumnasRegulares(Map<String, Object> fila) {
        sentencia.getColumnasRegulares().stream()
                .forEach(columnaRegular -> agruparValores(coleccionActual, columnaRegular, MAPPER.valueToTree(fila.get(columnaRegular))));
        coleccionActual = resultado;
    }

    public static ArrayNode arrayNodeVacio() {
        return MAPPER.createArrayNode();
    }

    public static ObjectNode objectNodeVacio() {
        return MAPPER.createObjectNode();
    }

    public static TiposResultado obtenerTipoResultado(String tipoResultado) {
        try {
            return TiposResultado.valueOf(tipoResultado);
        } catch (RuntimeException e) {
            throw new ExcepcionGenerica("'" + tipoResultado + "' no es un tipo de resultado válido.");
        }
    }

    public enum TiposResultado {
        PLANO, JERARQUÍA, JERARQUÍA_CON_NOMBRES_DE_COLUMNAS
    }

}
