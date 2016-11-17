package com.diewebsiten.core.eventos.dto.transaccion;

import com.diewebsiten.core.almacenamiento.Proveedores;
import com.diewebsiten.core.eventos.dto.transaccion.columnar.Cassandra;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.eventos.dto.transaccion.Transacciones.TIPOS_TRANSACCIONES;
import static com.diewebsiten.core.eventos.dto.transaccion.Transacciones.nuevaTransaccionCassandra;
import static com.diewebsiten.core.util.Transformaciones.jsonToMap;
import static com.diewebsiten.core.util.Transformaciones.jsonToObject;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Proveedores.class})
public class TransaccionesTest {

    private Map<String, String> transaccionesDesdeLaBaseDeDatos = new HashMap<>();
    private static final String SENTENCIA = "SELECT pagina, evento, transaccion, motoralmacenamiento, sentencia, tiporesultado " +
                                            "FROM diewebsiten.eventos " +
                                            "WHERE sitioweb = ?;";

    private static final Object PARAMETROS = new Object[]{"localhost"};
    private static final String MOTOR_ALMACENAMIENTO = "motoralmacenamiento";
    private static final String LLAVES_PRIMARIAS = "LP";
    private static final String RESULTADO_TRANSACCION = "RT";
    private static final String RESULTADO_ESPERADO = "RE";

    private static final String RUTA_RAIZ_EVENTOS = "com/diewebsiten/core/eventos/";
    private static final String RUTA_TRANSACCION = "dto/transaccion/";
    private static final String RUTA_TRANSACCION_SIN_FILTROS = RUTA_RAIZ_EVENTOS + RUTA_TRANSACCION + "transacciones_desde_base_de_datos/transaccion_sin_filtros/";
    private static final String RUTA_TRANSACCION_SOLO_COLUMNAS_PRIMARIAS = RUTA_RAIZ_EVENTOS + RUTA_TRANSACCION + "transacciones_desde_base_de_datos/transaccion_solo_columnas_primarias/";

    private OngoingStubbing<Supplier<Stream<Map<String, Object>>>> ejecutarTransaccionStubbing;
    private Cassandra transaccionCassandra;

    @Before
    public void setUp() {
        transaccionCassandra = nuevaTransaccionCassandra(SENTENCIA, PARAMETROS);
        mockStatic(Proveedores.class);
        ejecutarTransaccionStubbing = when(Proveedores.ejecutarTransaccion(anyString(), anyString(), anyObject()));
        inicializarTransaccionesDesdeLaBaseDeDatos();
    }

    @Test
    public void transaccionCassandraConResultadoPlano() {
        ejecutarTransaccionStubbing.thenReturn(crearResultSetPrueba("com/diewebsiten/core/eventos/dto/transaccion/transaccionesEventoResultSet.json"));
        JsonNode resultadoEsperado = obtenerResultadoDesdeArchivo("com/diewebsiten/core/eventos/dto/transaccion/transaccionesEventoResultSet.json");
        JsonNode resultadoActual = transaccionCassandra.plana();
        assertEquals(resultadoEsperado, resultadoActual);
    }

    @Test
    public void transaccionCassandraConResultadoEnJerarquia() {
        ejecutarTransaccionStubbing.thenReturn(crearResultSetPrueba("com/diewebsiten/core/eventos/dto/transaccion/llavesPrimariasResultSet.json"),
                                               crearResultSetPrueba("com/diewebsiten/core/eventos/dto/transaccion/transaccionesEventoResultSet.json"));
        JsonNode resultadoEsperado = obtenerResultadoDesdeArchivo("com/diewebsiten/core/eventos/dto/transaccion/resultadoTransaccionesEventoEnJerarquia.json");
        JsonNode resultadoActual = transaccionCassandra.enJerarquia();
        assertEquals(resultadoEsperado, resultadoActual);
    }

    @Test
    public void transaccionCassandraConResultadoEnJerarquiaConNombres() {
        ejecutarTransaccionStubbing.thenReturn(crearResultSetPrueba("com/diewebsiten/core/eventos/dto/transaccion/llavesPrimariasResultSet.json"),
                                               crearResultSetPrueba("com/diewebsiten/core/eventos/dto/transaccion/transaccionesEventoResultSet.json"));
        JsonNode resultadoEsperado = obtenerResultadoDesdeArchivo("com/diewebsiten/core/eventos/dto/transaccion/resultadoTransaccionesEventoEnJerarquiaConNombres.json");
        JsonNode resultadoActual = transaccionCassandra.enJerarquiaConNombres();
        assertEquals(resultadoEsperado, resultadoActual);
    }

    @Test
    public void transaccionConSoloColumnasPrimarias() {
        List<Transaccion> transacciones = obtenerTransaccionesDesdeLaBaseDeDatos("com/diewebsiten/core/eventos/dto/transaccion/transacciones_desde_base_de_datos/transaccionSoloColumnasPrimariasResultSet.json");
        transacciones.forEach(transaccion -> {
            JsonNode resultadoActual = obtenerResultado(transaccion);
            JsonNode resultadoEsperado = obtenerResultadoDesdeArchivo(obtenerNombreArchivoTransaccion(transaccion.getNombre(), RESULTADO_ESPERADO));
            assertEquals(resultadoEsperado, resultadoActual);
        });
    }

    @Test
    public void transaccionSinFiltros() {
        List<Transaccion> transacciones = obtenerTransaccionesDesdeLaBaseDeDatos("com/diewebsiten/core/eventos/dto/transaccion/transacciones_desde_base_de_datos/transaccionSinFiltrosResultSet.json");
        transacciones.forEach(transaccion -> {
            JsonNode resultadoActual = obtenerResultadoTransaccionSinFiltros(transaccion);
            JsonNode resultadoEsperado = obtenerResultadoDesdeArchivo(obtenerNombreArchivoTransaccion(transaccion.getNombre(), RESULTADO_ESPERADO));
            assertEquals(resultadoEsperado, resultadoActual);
        });
    }

    @Test(expected = ExcepcionGenerica.class)
    public void transaccionConTipoResultadoInvalido() {
        List<Transaccion> transacciones = obtenerTransaccionesDesdeLaBaseDeDatos("com/diewebsiten/core/eventos/dto/transaccion/transacciones_desde_base_de_datos/transaccionConTipoResultadoInvalidoResultSet.json");
        transacciones.forEach(transaccion -> obtenerResultadoTransaccionSinFiltros(transaccion));
    }

    @Test(expected = ExcepcionGenerica.class)
    public void transaccionConMotorAlmacenamientoInvalido() {
        List<Transaccion> transacciones = obtenerTransaccionesDesdeLaBaseDeDatos("com/diewebsiten/core/eventos/dto/transaccion/transacciones_desde_base_de_datos/transaccionConMotorAlmacenamientoInvalidoResultSet.json");
        transacciones.forEach(transaccion -> obtenerResultadoTransaccionSinFiltros(transaccion));
    }

    private JsonNode obtenerResultado(Transaccion transaccion) {
        String nombreArchivoLlavesPrimarias = obtenerNombreArchivoTransaccion(transaccion.getNombre(), LLAVES_PRIMARIAS);
        String nombreArchivoResultadoTransaccion = obtenerNombreArchivoTransaccion(transaccion.getNombre(), RESULTADO_TRANSACCION);
        ejecutarTransaccionStubbing.thenReturn(crearResultSetPrueba(nombreArchivoLlavesPrimarias),
                                               crearResultSetPrueba(nombreArchivoResultadoTransaccion));
        return new Transacciones(transaccion).obtenerResultado();
    }

    private JsonNode obtenerResultadoTransaccionSinFiltros(Transaccion transaccion) {
        String nombreArchivoResultadoTransaccion = obtenerNombreArchivoTransaccion(transaccion.getNombre(), RESULTADO_TRANSACCION);
        ejecutarTransaccionStubbing.thenReturn(crearResultSetPrueba(nombreArchivoResultadoTransaccion));
        return new Transacciones(transaccion).obtenerResultado();
    }


    private Supplier<Stream<Map<String,Object>>> crearResultSetPrueba(String nombreArchivo) {
        JsonNode resultSetPrueba = obtenerResultadoDesdeArchivo(nombreArchivo);
        List<Map<String, Object>> resultSetPruebaList = new ArrayList<>();
        resultSetPrueba.forEach(objeto -> resultSetPruebaList.add(jsonToMap(objeto, String.class, Object.class)));
        return () -> resultSetPruebaList.stream();
    }

    private JsonNode obtenerResultadoDesdeArchivo(String nombreArchivo) {
        String ruta = "";
        try {
            ruta = getClass().getClassLoader().getResource(nombreArchivo).getPath();
            File archivo = new File(ruta);
            return jsonToObject(archivo, JsonNode.class);
        } catch (Exception e) {
            throw new ExcepcionGenerica("Error al procesar el archivo '" + nombreArchivo + "' en la ruta " + ruta + ". MOTIVO: " + e.getMessage());
        }
    }

    private List<Transaccion> obtenerTransaccionesDesdeLaBaseDeDatos(String nombreArchivo) {
        JsonNode transaccionesJson = obtenerResultadoDesdeArchivo(nombreArchivo);
        List<Transaccion> transacciones = new ArrayList<>();
        transaccionesJson.forEach(transaccionJson -> {
            String nombreMotorAlmacenamiento = transaccionJson.get(MOTOR_ALMACENAMIENTO).asText();
            Class<Transaccion> transaccionClass = TIPOS_TRANSACCIONES.getOrDefault(nombreMotorAlmacenamiento, Transaccion.class);
            Transaccion transaccion = jsonToObject(transaccionJson, transaccionClass);
            transacciones.add(transaccion);
        });
        return transacciones;
    }

    private void inicializarTransaccionesDesdeLaBaseDeDatos() {
        transaccionesDesdeLaBaseDeDatos.put(LLAVES_PRIMARIAS + "consultarTiposTransacciones", RUTA_TRANSACCION_SIN_FILTROS + "llavesPrimariasTransaccionSinFiltros.json");
        transaccionesDesdeLaBaseDeDatos.put(LLAVES_PRIMARIAS + "consultarTiposTransaccionesSinPuntoYComa", RUTA_TRANSACCION_SIN_FILTROS + "llavesPrimariasTransaccionSinFiltros.json");
        transaccionesDesdeLaBaseDeDatos.put(LLAVES_PRIMARIAS + "sentenciaGrupoValidaciones", RUTA_TRANSACCION_SOLO_COLUMNAS_PRIMARIAS + "llavesPrimariasTransaccionSoloColumnasPrimarias.json");
        transaccionesDesdeLaBaseDeDatos.put(RESULTADO_TRANSACCION + "consultarTiposTransacciones", RUTA_TRANSACCION_SIN_FILTROS + "resultadoTransaccionSinFiltros.json");
        transaccionesDesdeLaBaseDeDatos.put(RESULTADO_TRANSACCION + "consultarTiposTransaccionesSinPuntoYComa", RUTA_TRANSACCION_SIN_FILTROS + "resultadoTransaccionSinFiltros.json");
        transaccionesDesdeLaBaseDeDatos.put(RESULTADO_TRANSACCION + "sentenciaGrupoValidaciones", RUTA_TRANSACCION_SOLO_COLUMNAS_PRIMARIAS + "resultadoTransaccionSoloColumnasPrimarias.json");
        transaccionesDesdeLaBaseDeDatos.put(RESULTADO_ESPERADO + "consultarTiposTransacciones", RUTA_TRANSACCION_SIN_FILTROS + "resultadoEsperadoEnJerarquia.json");
        transaccionesDesdeLaBaseDeDatos.put(RESULTADO_ESPERADO + "consultarTiposTransaccionesSinPuntoYComa", RUTA_TRANSACCION_SIN_FILTROS + "resultadoEsperadoPlano.json");
        transaccionesDesdeLaBaseDeDatos.put(RESULTADO_ESPERADO + "sentenciaGrupoValidaciones", RUTA_TRANSACCION_SOLO_COLUMNAS_PRIMARIAS + "resultadoEsperadoEnJerarquiaConNombres.json");
    }
    
    private String obtenerNombreArchivoTransaccion(String nombreTransaccion, String tipoArchivo) {
        return transaccionesDesdeLaBaseDeDatos.get(tipoArchivo + nombreTransaccion);
    }

}