package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.SentenciaColumnar;
import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.cassandra.Cassandra;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.diewebsiten.core.almacenamiento.ResultadoTransaccion.TiposResultado;
import static com.diewebsiten.core.almacenamiento.ResultadoTransaccion.TiposResultado.*;
import static com.diewebsiten.core.util.Transformaciones.stringToList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ResultadoTransaccionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    List<Map<String, Object>> resultSetList = new ArrayList<>();
    SentenciaColumnar sentencia;

    @Before
    public void setUp() throws Exception {
        inicializarResultSetList();
        inicializarSentencia();
    }

    @Test
    public void obtenerResultadoPlano() throws Exception {
        JsonNode resultadoPlano = obtenerResultado(PLANO);
        assertEquals(stringToList(resultadoPlano.toString(), Map.class), resultSetList);
    }

    @Test
    public void obtenerResultadoEnJerarquia() throws IOException {
        JsonNode resultadoEnJerarquia = obtenerResultado(JERARQUÍA);
        URL rutaArchivo = getClass().getClassLoader().getResource("resultadoTransaccionEnJerarquia.json");
        File archivoResultadoEsperado = new File(rutaArchivo.getPath());
        JsonNode resultadoEsperado = MAPPER.readValue(archivoResultadoEsperado, JsonNode.class);
        assertEquals(resultadoEsperado, resultadoEnJerarquia);
    }

    @Test
    public void obtenerResultadoEnJerarquiaConNombres() throws IOException {
        JsonNode resultadoEnJerarquiaConNombres = obtenerResultado(JERARQUÍA_CON_NOMBRES_DE_COLUMNAS);
        URL rutaArchivo = getClass().getClassLoader().getResource("resultadoTransaccionEnJerarquiaConNombres.json");
        File archivoResultadoEsperado = new File(rutaArchivo.getPath());
        JsonNode resultadoEsperado = MAPPER.readValue(archivoResultadoEsperado, JsonNode.class);
        assertEquals(resultadoEsperado, resultadoEnJerarquiaConNombres);
    }

    private JsonNode obtenerResultado(TiposResultado tipoResultado) {
        return new ResultadoTransaccion(resultSetList.stream(), sentencia, tipoResultado).obtenerResultado();
    }

    private void inicializarResultSetList() {
        Map<String, Object> fila = new HashMap<>();
        fila.put("sitioweb", "localhost");
        fila.put("pagina", "eventos");
        fila.put("evento", "ConsultarInfoTabla");
        fila.put("transaccion", "consultarInfoTabla");
        fila.put("motoralmacenamiento", "CASSANDRA");
        fila.put("descripcion", "Breve descripción");
        resultSetList.add(fila);
        fila = new HashMap<>();
        fila.put("sitioweb", "localhost");
        fila.put("pagina", "eventos");
        fila.put("evento", "ConsultarInfoTabla");
        fila.put("transaccion", "consultarValidacionesColumnas");
        fila.put("motoralmacenamiento", "CASSANDRA");
        fila.put("descripcion", "Breve descripción");
        resultSetList.add(fila);
        fila = new HashMap<>();
        fila.put("sitioweb", "localhost");
        fila.put("pagina", "eventos");
        fila.put("evento", "ConsultarInfoTabla");
        fila.put("transaccion", "consultarTiposColumnas");
        fila.put("motoralmacenamiento", "CASSANDRA");
        fila.put("descripcion", "Breve descripción");
    }

    private void inicializarSentencia() throws Exception {
        Constructor<Cassandra> cassandra = Cassandra.class.getDeclaredConstructor(new Class[0]);
        cassandra.setAccessible(true);
        sentencia = cassandra.newInstance();
        Field columnas = SentenciaColumnar.class.getDeclaredField("columnasPrimarias");
        columnas.setAccessible(true);
        columnas.set(sentencia, asList("sitioweb", "pagina", "evento", "transaccion"));
        columnas = SentenciaColumnar.class.getDeclaredField("columnasRegulares");
        columnas.setAccessible(true);
        columnas.set(sentencia, asList("motoralmacenamiento", "descripcion"));
    }

}