package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.exceptions.DriverException;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.JsonUtils;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.diewebsiten.core.util.Transformaciones.jsonToMap;
import static com.diewebsiten.core.util.Transformaciones.objectToList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Cluster.class)
@PowerMockIgnore("javax.management.*")
public class ProveedorCassandraTest {

    private static final String RUTA_RAIZ_ALMACENAMIENTO = "com/diewebsiten/core/almacenamiento/";

    @Mock
    private Cluster cluster;
    @Mock
    private Session sesion;
    @Mock
    private PreparedStatement sentenciaPreparada;
    @Mock
    private ResultSet resultadoEjecucion;
    @Mock
    private ResultSet resultadoEjecucionVacio;

    private ProveedorCassandra proveedorCassandra;
    private Object[] parametros;
    private JsonUtils jsonUtils = new JsonUtils();


    @Before
    public void setUp() throws Exception {
        Constructor<ProveedorCassandra> proveedorCassandraConstructor = ProveedorCassandra.class.getDeclaredConstructor(new Class[0]);
        proveedorCassandraConstructor.setAccessible(true);
        proveedorCassandra = proveedorCassandraConstructor.newInstance();
        mockearClusterYSesion();
    }

    @Test
    public void ejecutarTransaccion() throws Exception {
        String sentencia = "SELECT transaccion, filtrossentencia, orden FROM diewebsiten.eventos WHERE sitioweb = ?;";
        parametros = new Object[]{"localhost"};

        mockearSentenciaPreparada(countMatches(sentencia, "?"));
        mockearResultSet();

        String rutaArchivoDatosColumnas = RUTA_RAIZ_ALMACENAMIENTO + "datosColumnasSentenciaConFiltros.json";
        JsonNode datosColumnasJson = obtenerJsonDesdeArchivo(rutaArchivoDatosColumnas);
        List<DatosColumnas> datosColumnasList = objectToList(datosColumnasJson, DatosColumnas.class);

        String rutaArchivoResultadoEsperado = RUTA_RAIZ_ALMACENAMIENTO + "resultadoEsperadoSentenciaConFiltros.json";
        ArrayNode resultadosEsperados = (ArrayNode) obtenerJsonDesdeArchivo(rutaArchivoResultadoEsperado);

        for (int i = 0; i < datosColumnasList.size(); i++) {
            DatosColumnas datoColumna = datosColumnasList.get(i);
            mockearFila(datoColumna.getNombre(), datoColumna.getTipo(), datoColumna.getValor());
            Map<String, Object> registroEsperado = jsonToMap(resultadosEsperados.get(i), String.class, Object.class);
            Map<String, Object> registroActual = proveedorCassandra.ejecutarTransaccion(sentencia, parametros).get().collect(toList()).get(0);
            compararResultados(registroEsperado.get(datoColumna.getNombre()), registroActual.get(datoColumna.getNombre()));
        }

        proveedorCassandra.desconectar();

    }

    @Test
    public void ejecutarTransaccionConResultadoVacio() throws Exception {
        String sentencia = "SELECT transaccion, filtrossentencia, orden FROM diewebsiten.eventos;";
        parametros = new Object[]{};
        mockearSentenciaPreparada(countMatches(sentencia, "?"));
        mockearResultSetVacio();
        List<Map<String, Object>> resultadoTransaccion = proveedorCassandra.ejecutarTransaccion(sentencia, parametros).get().collect(toList());
        assertEquals(new ArrayList<>(), resultadoTransaccion);
    }

    @Test(expected = ExcepcionGenerica.class)
    public void debeArrojarErrorDeConexionALaBaseDeDatos() {
        doThrow(new DriverException("any message...")).when(cluster).connect();
        proveedorCassandra.conectar();
    }

    @Test(expected = ExcepcionGenerica.class)
    public void debeArrojarErrorDeParametrosInconsistentes() throws Exception {
        String sentencia = "SELECT transaccion, filtrossentencia, orden FROM diewebsiten.eventos;";
        parametros = new Object[]{"localhost"};

        mockearSentenciaPreparada(countMatches(sentencia, "?"));
        mockearResultSet();
        proveedorCassandra.ejecutarTransaccion(sentencia, parametros);
    }

    private void mockearClusterYSesion() {
        // Cluster
        mockStatic(Cluster.class);
        Cluster.Builder builder = mock(Cluster.Builder.class);
        when(Cluster.builder()).thenReturn(builder);
        when(builder.addContactPoint(anyString())).thenReturn(builder);
        when(builder.withPort(anyInt())).thenReturn(builder);
        when(builder.build()).thenReturn(cluster);
        // Sesión
        when(cluster.connect()).thenReturn(sesion);
        // Conectar
        proveedorCassandra.conectar();
    }

    private void compararResultados(Object resultadoEsperado, Object resultadoActual) {
        if (resultadoEsperado instanceof Number) {
            int resultadoActualInt = (resultadoActual instanceof BigInteger) ? ((BigInteger) resultadoActual).intValue()
                                                                             : (int) resultadoActual;
            assertEquals(resultadoEsperado, resultadoActualInt);
        } else {
            assertEquals(resultadoEsperado, resultadoActual);
        }
    }

    private void mockearSentenciaPreparada(int numFiltrosSentencia) {
        when(sesion.prepare(anyString())).thenReturn(sentenciaPreparada);
        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        when(sentenciaPreparada.getVariables()).thenReturn(columnDefinitions);
        when(columnDefinitions.size()).thenReturn(numFiltrosSentencia);
    }

    private void mockearResultSet() throws Exception {
        BoundStatement boundStatement = mock(BoundStatement.class);
        when(sentenciaPreparada.bind(parametros)).thenReturn(boundStatement);
        when(sesion.execute(boundStatement)).thenReturn(resultadoEjecucion);
    }

    private void mockearResultSetVacio() throws Exception {
        when(sesion.execute(sentenciaPreparada.getQueryString())).thenReturn(resultadoEjecucionVacio);
        when(resultadoEjecucionVacio.isExhausted()).thenReturn(true);
    }

    private void mockearFila(String nombre, String tipo, String valor) throws Exception {
        Row fila = mock(Row.class);
        List<Row> resultadoEjecucionList = new ArrayList<>();
        resultadoEjecucionList.add(fila);
        when(resultadoEjecucion.all()).thenReturn(resultadoEjecucionList);

        ColumnDefinitions tiposDeDatosColumnas = mockearTiposDeDatosColumnas(nombre, tipo);
        when(fila.getColumnDefinitions()).thenReturn(tiposDeDatosColumnas);

        ByteBuffer valoresColumnas = mockearValoresColumnas(valor);
        when(fila.getBytesUnsafe(anyString())).thenReturn(valoresColumnas);
    }

    private ColumnDefinitions mockearTiposDeDatosColumnas(String nombre, String tipo) throws Exception {
        Constructor<DataType.NativeType> nativeTypeConstructor = prepararConstructor(DataType.NativeType.class);
        DataType.NativeType type = nativeTypeConstructor.newInstance(DataType.Name.valueOf(tipo), null);

        Constructor<Definition> definitionConstructor = prepararConstructor(Definition.class);
        Definition def = definitionConstructor.newInstance(null, null, nombre, type);

        Constructor<ColumnDefinitions> columnDefinitionsConstructor = prepararConstructor(ColumnDefinitions.class);
        return columnDefinitionsConstructor.newInstance(new Definition[]{def}, null);
    }

    private ByteBuffer mockearValoresColumnas(String valor) throws Exception {
        byte[] bytes = valor == null ? new byte[]{} : valor.getBytes();
        return (ByteBuffer) ByteBuffer.allocate(512).put(bytes).flip();
    }

    private <T> Constructor<T> prepararConstructor(Class<T> claseConstructor) {
        Constructor<T> constructor = (Constructor<T>) claseConstructor.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor;
    }

    private JsonNode obtenerJsonDesdeArchivo(String nombreArchivo) {
        return jsonUtils.obtenerJsonDesdeArchivo(nombreArchivo);
    }

    private static class DatosColumnas {

        private String nombre;
        private String tipo;
        private String valor;

        public DatosColumnas(){}

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }

}