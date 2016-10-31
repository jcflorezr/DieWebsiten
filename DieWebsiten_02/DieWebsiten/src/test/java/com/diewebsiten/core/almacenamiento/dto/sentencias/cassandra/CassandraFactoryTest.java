package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

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

        boolean sentenciaSimple = false;
        inicializarSentencia(SENTENCIA_COMPLETA, sentenciaSimple);

        mockearSentenciaExistente(null);
        mockearSentenciaPreparada();
        mockearNombreBaseDeDatosYNombreTabla(0, null);
        mockearColumnasFiltro(SENTENCIA_COMPLETA);

        String keyAliases = "[\"sitioweb\"]";
        String columnAliases = "[\"pagina\", \"evento\", \"transaccion\"]";
        mockearColumnasIntermediasYRegulares(keyAliases, columnAliases);

        Cassandra sentenciaCassandra = (Cassandra) cassandraFactory.crearSentencia();
        Cassandra sentenciaCassandraMock = crearObjetoSentenciaCassandra();
        sentenciaCassandraMock.setQueryString(SENTENCIA_COMPLETA);
        sentenciaCassandraMock.setKeyspaceName("diewebsiten");
        sentenciaCassandraMock.setColumnfamilyName("eventos");
        sentenciaCassandraMock.setFiltrosSentencia(asList("sitioweb", "pagina", "eventos"));
        sentenciaCassandraMock.setColumnasIntermedias(asList("transaccion"));
        sentenciaCassandraMock.setColumnasRegulares(asList("motoralmacenamiento", "sentencia", "filtrossentencia", "tiporesultado"));

        assertEquals(sentenciaCassandraMock, sentenciaCassandra);
    }

    // TODO: test con sentencia de unica columna
    // TODO: test con sentencia que solo contenga columnas intermedias
    // TODO: test con sentencia existente

    private void inicializarSentencia(String queryString, boolean sentenciaSimple) {
        cassandraFactory = new CassandraFactory(queryString, sentenciaSimple);
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
        when(ProveedorCassandra.obtenerResultSetParametros.apply(anyObject(), anyObject())).thenReturn(resultSet);
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
        return (substringBefore(dato, separadores[3])).trim();
    }

    private Cassandra crearObjetoSentenciaCassandra() {
        return new Cassandra();
    }

}