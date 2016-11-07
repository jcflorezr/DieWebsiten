package com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.cassandra;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.ProveedorCassandra;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactory;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactoryTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SentenciasFactory.class, ProveedorCassandra.class, ColumnDefinitions.class, StreamSupport.class})
public class CassandraFactoryTest {

    private static final String SENTENCIA_COMPLETA = "SELECT transaccion, motoralmacenamiento, sentencia, filtrossentencia, tiporesultado " +
                                                     "FROM diewebsiten.eventos " +
                                                     "WHERE sitioweb = ? AND pagina = ? AND evento = ?;";
    private static final String SENTENCIA_UNICA_COLUMNA = "SELECT keyspaces FROM diewebsiten.sitiosweb WHERE sitioweb = ?;";
    private static final String SENTENCIA_CON_SOLO_COLUMNAS_PRIMARIAS = "SELECT tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupovalidacion = ?;";
    private static final String SENTENCIA_SIN_FILTROS = "SELECT tipo_transaccion FROM diewebsiten.tipos_de_transacciones;";
    private static final String SENTENCIA_LLAVES_PRIMARIAS = "SELECT key_aliases, column_aliases FROM system.schema_columnfamilies WHERE keyspace_name = ? AND columnfamily_name = ?;";
    private static final int CONSTRUCTOR_CLASE_DEFINITION = 0;
    private static final String AND = "AND";
    private static final String FROM = "FROM";
    private static final String WHERE = "WHERE";
    private static final String PARAMETRO = "?";
    private static final String IGUAL = "=";
    private static final String PUNTO_Y_COMA = ";";
    private static final String PUNTO = ".";
    private static final String KEY_ALIASES = "key_aliases";
    private static final String COLUMN_ALIASES = "column_aliases";

    private CassandraFactory cassandraFactory;

    @Mock
    private PreparedStatement sentenciaPreparada;
    @Mock
    private ColumnDefinitions columnDefinitions;
    @Mock
    private Spliterator<Definition> definitionSpliterator;
    @Mock
    private ResultSet resultSet;
    @Mock
    private Row row;
    @Mock
    private Cassandra cassandra;
    @Mock
    private Stream<Definition> definitionStream;

    @Test
    public void crearSentenciaCompleta() throws Exception {
        DatosSentencia datosSentencia = new DatosSentencia()
                .queryString(SENTENCIA_COMPLETA)
                .sentenciaSimple(false)
                .sentenciaRetorno(null)
                .numColumnDefinitions(0)
                .keyAliases("[\"sitioweb\"]")
                .columnAliases("[\"pagina\", \"evento\", \"transaccion\"]")
                .keyspaceName("diewebsiten")
                .columnFamilyName("eventos")
                .filtrosSentencia(asList("sitioweb", "pagina", "evento"))
                .columnasIntermedias(asList("transaccion"))
                .columnasRegulares(asList("motoralmacenamiento", "sentencia", "filtrossentencia", "tiporesultado"));
        probarSentencia(datosSentencia);
    }

    @Test
    public void crearSentenciaConUnicaColumnaQuery() throws Exception {
        DatosSentencia datosSentencia = new DatosSentencia()
                .queryString(SENTENCIA_UNICA_COLUMNA)
                .sentenciaSimple(false)
                .sentenciaRetorno(null)
                .numColumnDefinitions(1)
                .keyAliases("[\"sitioweb\"]")
                .columnAliases(null)
                .keyspaceName("diewebsiten")
                .columnFamilyName("sitiosweb")
                .filtrosSentencia(asList("sitioweb"))
                .columnasIntermedias(new ArrayList<>())
                .columnasRegulares(asList("keyspaces"));
        probarSentencia(datosSentencia);
    }

    @Test
    public void crearSentenciaConSoloColumnasPrimarias() throws Exception {
        DatosSentencia datosSentencia = new DatosSentencia()
                .queryString(SENTENCIA_CON_SOLO_COLUMNAS_PRIMARIAS)
                .sentenciaSimple(false)
                .sentenciaRetorno(null)
                .numColumnDefinitions(1)
                .keyAliases("[\"grupovalidacion\"]")
                .columnAliases("[\"tipo\", \"validacion\"]")
                .keyspaceName("diewebsiten")
                .columnFamilyName("grupos_de_validaciones")
                .filtrosSentencia(asList("grupovalidacion"))
                .columnasIntermedias(asList("tipo"))
                .columnasRegulares(asList("validacion"));
        probarSentencia(datosSentencia);
    }

    @Test
    public void crearSentenciaSinFiltros() throws Exception {
        DatosSentencia datosSentencia = new DatosSentencia()
                .queryString(SENTENCIA_SIN_FILTROS)
                .sentenciaSimple(false)
                .sentenciaRetorno(null)
                .numColumnDefinitions(1)
                .keyAliases("[\"tipo_transaccion\"]")
                .columnAliases(null)
                .keyspaceName("diewebsiten")
                .columnFamilyName("tipos_de_transacciones")
                .filtrosSentencia(new ArrayList<>())
                .columnasIntermedias(new ArrayList<>())
                .columnasRegulares(asList("tipo_transaccion"));
        probarSentencia(datosSentencia);
    }

    @Test
    public void crearSentenciaSinPuntoYComa() throws Exception {
        DatosSentencia datosSentencia = new DatosSentencia()
                .queryString(chop(SENTENCIA_SIN_FILTROS))
                .sentenciaSimple(false)
                .sentenciaRetorno(null)
                .numColumnDefinitions(1)
                .keyAliases("[\"tipo_transaccion\"]")
                .columnAliases(null)
                .keyspaceName("diewebsiten")
                .columnFamilyName("tipos_de_transacciones")
                .filtrosSentencia(new ArrayList<>())
                .columnasIntermedias(new ArrayList<>())
                .columnasRegulares(asList("tipo_transaccion"));
        probarSentencia(datosSentencia);
    }

    @Test
    public void crearSentenciaSimple() throws Exception {
//        "SELECT key_aliases, column_aliases FROM system.schema_columnfamilies WHERE keyspace_name = ? AND columnfamily_name = ?;"
        DatosSentencia datosSentencia = new DatosSentencia()
                .queryString(SENTENCIA_LLAVES_PRIMARIAS)
                .sentenciaSimple(true)
                .sentenciaRetorno(null)
                .numColumnDefinitions(0)
                .keyAliases(null)
                .columnAliases(null)
                .keyspaceName(null)
                .columnFamilyName(null)
                .filtrosSentencia(new ArrayList<>())
                .columnasIntermedias(new ArrayList<>())
                .columnasRegulares(new ArrayList<>());
        probarSentencia(datosSentencia);
    }

    private void probarSentencia(DatosSentencia datosSentencia) throws Exception {

        String queryString = datosSentencia.getQueryString();
        cassandraFactory = new CassandraFactory(queryString, datosSentencia.isSentenciaSimple());

        mockearSentenciaExistente(datosSentencia.getSentenciaRetorno());
        mockearSentenciaPreparada();
        mockearNombreBaseDeDatosYNombreTabla(datosSentencia.getNumColumnDefinitions(), queryString);
        mockearColumnasFiltro(queryString);

        mockearColumnasIntermediasYRegulares(datosSentencia.getKeyAliases(), datosSentencia.getColumnAliases());

        Cassandra sentenciaCassandra = (Cassandra) cassandraFactory.crearSentencia();

        assertEquals(queryString, sentenciaCassandra.getQueryString());
        assertEquals(datosSentencia.getKeyspaceName(), sentenciaCassandra.getKeyspaceName());
        assertEquals(datosSentencia.getColumnFamilyName(), sentenciaCassandra.getColumnfamilyName());
        List<String> filtrosSentencia = sentenciaCassandra.getFiltrosSentencia().get().collect(toList());
        assertEquals(datosSentencia.getFiltrosSentencia(), filtrosSentencia);
        List<String> columnasIntermedias = sentenciaCassandra.getColumnasIntermedias().get().collect(toList());
        assertEquals(datosSentencia.getColumnasIntermedias(), columnasIntermedias);
        List<String> columnasRegulares = sentenciaCassandra.getColumnasRegulares().get().collect(toList());
        assertEquals(datosSentencia.getColumnasRegulares(), columnasRegulares);

    }

    private void mockearSentenciaExistente(Sentencia sentenciaRetorno) {
        new SentenciasFactoryTest().obtenerSentenciaTest().thenReturn(sentenciaRetorno);
    }

    private void mockearSentenciaPreparada() {
        mockStatic(ProveedorCassandra.class);
        when(ProveedorCassandra.prepararSentencia.apply(anyString())).thenReturn(sentenciaPreparada);
    }

    private void mockearNombreBaseDeDatosYNombreTabla(int numColumnDefinitions, String queryString) {
        when(sentenciaPreparada.getVariables()).thenReturn(columnDefinitions);
        if (numColumnDefinitions > 0 && isNotBlank(queryString)) {
            String tabla = obtenerDatoDesdeSentencia(queryString, FROM, WHERE, PUNTO_Y_COMA, PUNTO);
            when(columnDefinitions.getTable(0)).thenReturn(tabla);
        }
        when(columnDefinitions.size()).thenReturn(numColumnDefinitions);
    }

    private void mockearColumnasFiltro(String queryString) throws Exception {
        queryString = removeEnd(substringAfter(queryString, WHERE), PUNTO_Y_COMA);
        Constructor<Definition> constructor = (Constructor<Definition>) Definition.class.getDeclaredConstructors()[CONSTRUCTOR_CLASE_DEFINITION];
        constructor.setAccessible(true);
        List<Definition> defs = new ArrayList<>();
        String[] nombresColumnasFiltro = split(queryString, PARAMETRO);
        for(String nombreColumnaFiltro : nombresColumnasFiltro) {
            nombreColumnaFiltro = remove(remove(nombreColumnaFiltro, AND), IGUAL).trim();
            Definition def = constructor.newInstance(null, null, nombreColumnaFiltro, null);
            defs.add(def);
        }
        when(columnDefinitions.asList()).thenReturn(defs);
    }

    private void mockearColumnasIntermediasYRegulares(String keyAliases, String columnAliases) {
        when(ProveedorCassandra.obtenerResultSet(anyObject(), anyObject())).thenReturn(resultSet);
        when(resultSet.one()).thenReturn(row);
        when(row.getString(KEY_ALIASES)).thenReturn(firstNonNull(keyAliases, new ArrayList<>()).toString());
        when(row.getString(COLUMN_ALIASES)).thenReturn(firstNonNull(columnAliases, new ArrayList<>()).toString());
    }

    private String obtenerDatoDesdeSentencia(String queryString, String... separadores) {
        String dato = contains(queryString, separadores[1])
                ? substringBetween(queryString, separadores[0], separadores[1])
                : contains(queryString, separadores[2])
                ? substringBetween(queryString, separadores[0], separadores[2])
                : substringAfter(queryString, separadores[0]);
        return (substringAfter(dato, separadores[3])).trim();
    }

    private class DatosSentencia {

        private String queryString;
        private boolean sentenciaSimple;
        private Sentencia sentenciaRetorno;
        private int numColumnDefinitions;
        private String keyAliases;
        private String columnAliases;
        private String keyspaceName;
        private String columnFamilyName;
        private List<String> filtrosSentencia;
        private List<String> columnasIntermedias;
        private List<String> columnasRegulares;

        public String getQueryString() {
            return queryString;
        }

        public DatosSentencia queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public boolean isSentenciaSimple() {
            return sentenciaSimple;
        }

        public DatosSentencia sentenciaSimple(boolean sentenciaSimple) {
            this.sentenciaSimple = sentenciaSimple;
            return this;
        }

        public Sentencia getSentenciaRetorno() {
            return sentenciaRetorno;
        }

        public DatosSentencia sentenciaRetorno(Sentencia sentenciaRetorno) {
            this.sentenciaRetorno = sentenciaRetorno;
            return this;
        }

        public int getNumColumnDefinitions() {
            return numColumnDefinitions;
        }

        public DatosSentencia numColumnDefinitions(int numColumnDefinitions) {
            this.numColumnDefinitions = numColumnDefinitions;
            return this;
        }

        public String getKeyAliases() {
            return keyAliases;
        }

        public DatosSentencia keyAliases(String keyAliases) {
            this.keyAliases = keyAliases;
            return this;
        }

        public String getColumnAliases() {
            return columnAliases;
        }

        public DatosSentencia columnAliases(String columnAliases) {
            this.columnAliases = columnAliases;
            return this;
        }

        public String getKeyspaceName() {
            return keyspaceName;
        }

        public DatosSentencia keyspaceName(String keyspaceName) {
            this.keyspaceName = keyspaceName;
            return this;
        }

        public String getColumnFamilyName() {
            return columnFamilyName;
        }

        public DatosSentencia columnFamilyName(String columnFamilyName) {
            this.columnFamilyName = columnFamilyName;
            return this;
        }

        public List<String> getFiltrosSentencia() {
            return filtrosSentencia;
        }

        public DatosSentencia filtrosSentencia(List<String> filtrosSentencia) {
            this.filtrosSentencia = filtrosSentencia;
            return this;
        }

        public List<String> getColumnasIntermedias() {
            return columnasIntermedias;
        }

        public DatosSentencia columnasIntermedias(List<String> columnasIntermedias) {
            this.columnasIntermedias = columnasIntermedias;
            return this;
        }

        public List<String> getColumnasRegulares() {
            return columnasRegulares;
        }

        public DatosSentencia columnasRegulares(List<String> columnasRegulares) {
            this.columnasRegulares = columnasRegulares;
            return this;
        }
    }

}