package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactory;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.almacenamiento.ProveedorCassandra.prepararSentencia;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.*;

public class CassandraFactory extends SentenciasFactory {

    private static final String FROM = "FROM";
    private static final String WHERE = "WHERE";
    private static final String COMA = ",";
    private static final String PUNTO_Y_COMA = ";";
    private static final String PUNTO = ".";
    private static final String KEY_ALIASES = "key_aliases";
    private static final String COLUMN_ALIASES = "column_aliases";

    private String queryString;
    private boolean sentenciaSimple;
    private Cassandra cassandra;

    // Las siguientes propiedades solo se usa para extraer la informacion
    // de la sentencia en caso de que 'sentenciaPreparada' no la contenga
    private List<String> columnasQuery;

    private static Object obj = new Object();

    CassandraFactory() {}

    public CassandraFactory(String queryString, boolean sentenciaSimple) {
        this.queryString = queryString;
        this.sentenciaSimple = sentenciaSimple;
        cassandra = new Cassandra();
    }

    @Override
    public Sentencia crearSentencia() {
        Sentencia sentencia = obtenerSentenciaExistente(queryString);
        if (sentencia == null) {
            synchronized (obj) {
                sentencia =  obtenerSentenciaExistente(queryString);
                if (sentencia == null) {
                    prepararSentencia();
                    if (!sentenciaSimple) complementarSentencia();
                    guardarNuevaSentencia(sentencia);
                }
            }
        }
        return sentencia;
    }

    private void prepararSentencia() {
        cassandra = new Cassandra();
        cassandra.setSentenciaPreparada(prepararSentencia.apply(queryString));
        cassandra.setQueryString(queryString);
    }

    private void complementarSentencia() {
        guardarNombreBaseDeDatos();
        guardarNombreTabla();
        guardarParametros();
        guardarColumnasQuery();
        if (queryContieneUnicaColumna()) cassandra.setColumnasRegulares(columnasQuery);
        else {
            setColumnasIntermedias();
            setColumnasRegulares();
        }
    }

    private void guardarNombreBaseDeDatos() {
        String keyspaceName = firstNonNull(cassandra.getSentenciaPreparada().getQueryKeyspace(),
                obtenerDatoDesdeSentencia(true, FROM, WHERE, PUNTO_Y_COMA, PUNTO));
        cassandra.setKeyspaceName(keyspaceName);
    }

    private void guardarNombreTabla() {
        String columnfamilyName = cassandra.getSentenciaPreparada().getVariables().size() > 0
                                    ? cassandra.getSentenciaPreparada().getVariables().getTable(0)
                                    : obtenerDatoDesdeSentencia(false, FROM, WHERE, PUNTO_Y_COMA, PUNTO);
        cassandra.setColumnfamilyName(columnfamilyName);
    }

    private void guardarParametros() {
        Spliterator<Definition> parametrosSpliterator = cassandra.getSentenciaPreparada().getVariables().spliterator();
        List<String> parametrosSentencia = stream(parametrosSpliterator, false)
                                            .map(Definition::getName)
                                            .collect(toList());
        cassandra.setParametrosSentencia(parametrosSentencia);
    }

    private void guardarColumnasQuery() {
        columnasQuery = asList(substringBetween(queryString, SPACE, FROM).split(COMA)).stream()
                                    .map(columna -> columna.trim())
                                    .collect(toList());
    }

    private Supplier<Stream<String>> getColumnasQuery() {
        return () -> columnasQuery.stream();
    }






    private String obtenerDatoDesdeSentencia(boolean paraKeySpaceName, String... separadores) {
        String dato = contains(queryString, separadores[1])
                ? substringBetween(queryString, separadores[0], separadores[1])
                : contains(queryString, separadores[2])
                ? substringBetween(queryString, separadores[0], separadores[2])
                : substringAfter(queryString, separadores[0]);
        return (paraKeySpaceName ? substringBefore(dato, separadores[3]) : substringAfter(dato, separadores[3])).trim();
    }


    public boolean queryContieneUnicaColumna() {
        return columnasQuery.size() == 1;
    }


    // ======== HELPERS ========= //

    private boolean contieneSoloColumnasIntermedias() {
        return getNumeroColumnasIntermedias() > 0 && columnasQuery.size() == getNumeroColumnasIntermedias();
    }

    private int getNumeroColumnasIntermedias() {
        return columnasIntermedias.size();
    }

    static Cassandra obtenerSentenciaCreada(String queryString) {
        return (Cassandra) obtenerSentenciaExistente(queryString);
    }

}
