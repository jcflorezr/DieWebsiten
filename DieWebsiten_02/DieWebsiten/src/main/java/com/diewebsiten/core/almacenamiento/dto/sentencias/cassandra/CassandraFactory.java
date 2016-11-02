package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.almacenamiento.ProveedorCassandra.obtenerResultSetParametros;
import static com.diewebsiten.core.almacenamiento.ProveedorCassandra.prepararSentencia;
import static com.diewebsiten.core.almacenamiento.util.Sentencias.LLAVES_PRIMARIAS;
import static com.diewebsiten.core.util.Transformaciones.stringToList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
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
    private Separadores separadores;

    // Las siguientes propiedades solo se usa para extraer la informacion
    // de la sentencia en caso de que 'sentenciaPreparada' no la contenga
    private List<String> columnasQuery;

    private static Object obj = new Object();

    public CassandraFactory(String queryString, boolean sentenciaSimple) {
        this.queryString = queryString;
        this.sentenciaSimple = sentenciaSimple;
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
                    guardarNuevaSentencia(cassandra);
                    sentencia = cassandra;
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
        guardarColumnasFiltro();
        guardarColumnasQuery();
        if (queryContieneUnicaColumna()) cassandra.setColumnasRegulares(columnasQuery);
        else {
            guardarColumnasIntermedias();
            guardarColumnasRegulares();
        }
    }

    private void guardarNombreBaseDeDatos() {
        String keyspaceName = firstNonNull(cassandra.getSentenciaPreparada().getQueryKeyspace(),
                obtenerDatoDesdeSentencia(true));
        cassandra.setKeyspaceName(keyspaceName);
    }

    private void guardarNombreTabla() {
        String columnfamilyName = cassandra.getSentenciaPreparada().getVariables().size() > 0
                                    ? cassandra.getSentenciaPreparada().getVariables().getTable(0)
                                    : obtenerDatoDesdeSentencia(false);
        cassandra.setColumnfamilyName(columnfamilyName);
    }

    private void guardarColumnasFiltro() {
        List<Definition> filtrosSpliterator = cassandra.getSentenciaPreparada().getVariables().asList();
        List<String> filtrosSentencia = filtrosSpliterator.stream()
                                            .map(Definition::getName)
                                            .collect(toList());
        cassandra.setFiltrosSentencia(filtrosSentencia);
    }

    private void guardarColumnasQuery() {
        columnasQuery = asList(substringBetween(queryString, SPACE, FROM).split(COMA)).stream()
                                    .map(columna -> columna.trim())
                                    .collect(toList());
    }

    private Supplier<Stream<String>> getColumnasQuery() {
        return () -> columnasQuery.stream();
    }

    private void guardarColumnasIntermedias() {
        Cassandra sentenciaLlavesPrimarias = (Cassandra) obtenerSentenciaExistente(LLAVES_PRIMARIAS.sentencia());
        Object[] parametrosLlavesPrimarias = {cassandra.getKeyspaceName(), cassandra.getColumnfamilyName()};
        Row llavesPrimarias = obtenerResultSetParametros.apply(sentenciaLlavesPrimarias, parametrosLlavesPrimarias).one();
        List<String> columnasIntermedias =
                Stream.of(stringToList(llavesPrimarias.getString(KEY_ALIASES), String.class),
                          stringToList(llavesPrimarias.getString(COLUMN_ALIASES), String.class))
                        .flatMap(List::stream)
                        .filter(llavePrimaria -> cassandra.getFiltrosSentencia().get().noneMatch(parametro -> llavePrimaria.equals(parametro)))
                        .map(columnaIntermedia -> getColumnasQuery().get().filter(columna -> columna.equals(columnaIntermedia)).findFirst().get())
                        .collect(toList());
        if (queryContieneSoloColumnasIntermedias(columnasIntermedias)) convertirUltimaColumnaARegular(columnasIntermedias);
        cassandra.setColumnasIntermedias(columnasIntermedias);
    }

    private void guardarColumnasRegulares() {
        List<String> columnasRegulares =
            getColumnasQuery().get()
                .filter(columna -> cassandra.getColumnasIntermedias().get().
                        noneMatch(columnaIntermedia -> columnaIntermedia.equals(columna)))
                .collect(toList());
        cassandra.setColumnasRegulares(columnasRegulares);
    }

    private String obtenerDatoDesdeSentencia(boolean paraKeySpaceName) {
        separadores = new Separadores();
        String dato = contains(queryString, separadores.getWhere())
                ? substringBetween(queryString, separadores.getFrom(), separadores.getWhere())
                : contains(queryString, separadores.getPuntoYComa())
                ? substringBetween(queryString, separadores.getFrom(), separadores.getPuntoYComa())
                : substringAfter(queryString, separadores.getFrom());
        return (paraKeySpaceName ? substringBefore(dato, separadores.getPunto()) : substringAfter(dato, separadores.getPunto()).trim());
    }

    private boolean queryContieneUnicaColumna() {
        return columnasQuery.size() == 1;
    }

    private boolean queryContieneSoloColumnasIntermedias(List<String> columnasIntermedias) {
        int numColumnasIntermedias = columnasIntermedias.size();
        return numColumnasIntermedias > 0 && columnasQuery.size() == numColumnasIntermedias;
    }

    private void convertirUltimaColumnaARegular(List<String> columnasIntermedias) {
        columnasIntermedias.remove(columnasIntermedias.size() - 1);
    }

    private class Separadores {

        private final String from = "FROM";
        private final String where = "WHERE";
        private final String coma = ",";
        private final String puntoYComa = ";";
        private final String punto = ".";

        public String getFrom() {
            return from;
        }

        public String getWhere() {
            return where;
        }

        public String getComa() {
            return coma;
        }

        public String getPuntoYComa() {
            return puntoYComa;
        }

        public String getPunto() {
            return punto;
        }
    }

}
