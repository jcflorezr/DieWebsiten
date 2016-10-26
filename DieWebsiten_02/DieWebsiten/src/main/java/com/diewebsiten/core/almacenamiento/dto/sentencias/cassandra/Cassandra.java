package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.almacenamiento.ProveedorCassandra.obtenerResultSetParametros;
import static com.diewebsiten.core.almacenamiento.ProveedorCassandra.prepararSentencia;
import static com.diewebsiten.core.almacenamiento.util.Sentencias.LLAVES_PRIMARIAS;
import static com.diewebsiten.core.util.Transformaciones.stringToList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.*;

public class Cassandra extends Sentencia {

	private static final String FROM = "FROM";
	private static final String WHERE = "WHERE";
	private static final String COMA = ",";
	private static final String PUNTO_Y_COMA = ";";
	private static final String PUNTO = ".";
	private static final String KEY_ALIASES = "key_aliases";
	private static final String COLUMN_ALIASES = "column_aliases";

	private PreparedStatement sentenciaPreparada;
	private String keyspaceName;
	private String columnfamilyName;
	private List<String> columnasIntermedias = new ArrayList<>();
	private List<String> columnasRegulares = new ArrayList<>();

	// Las siguientes propiedades solo se usa para extraer la informacion
	// de la sentencia en caso de que 'sentenciaPreparada' no la contenga
	private List<String> columnasResultado;

	Cassandra(String queryString, boolean sentenciaSimple) {
		this.sentenciaPreparada = prepararSentencia.apply(queryString);
		super.setQueryString(sentenciaPreparada.getQueryString().trim());
		if (!sentenciaSimple) complementarSentencia();
	}

	private void complementarSentencia() {
		setKeyspaceName();
		setColumnfamilyName();
		super.setParametrosSentencia(sentenciaPreparada);
		setColumnasResultado();
		if (unicaColumnaResultado()) columnasRegulares = columnasResultado;
		else {
			setColumnasIntermedias();
			setColumnasRegulares();
		}
	}

	// ====== GETTERS AND SETTERS ====== //

	public PreparedStatement getSentenciaPreparada() {
		return sentenciaPreparada;
	}

	public String getKeyspaceName() {
		return keyspaceName;
	}

	private void setKeyspaceName() {
		keyspaceName = sentenciaPreparada.getQueryKeyspace();
		if (isBlank(keyspaceName)) keyspaceName = obtenerDatoDesdeSentencia(true, FROM, WHERE, PUNTO_Y_COMA, PUNTO);
	}

	public String getColumnfamilyName() {
		return columnfamilyName;
	}

	private void setColumnfamilyName() {
		columnfamilyName = sentenciaPreparada.getVariables().size() > 0
										? sentenciaPreparada.getVariables().getTable(0)
										: obtenerDatoDesdeSentencia(false, FROM, WHERE, PUNTO_Y_COMA, PUNTO);
	}

	public Supplier<Stream<String>> getColumnasIntermedias() {
		return () -> columnasIntermedias.stream();
	}

	private void setColumnasIntermedias() {
		Cassandra sentenciaLlavesPrimarias = new CassandraFactory().obtenerSentenciaCreada(LLAVES_PRIMARIAS.sentencia());
		Row llavesPrimarias = obtenerResultSetParametros.apply(sentenciaLlavesPrimarias, new Object[]{getKeyspaceName(), getColumnfamilyName()}).one();
		columnasIntermedias =
		Stream.of(stringToList(llavesPrimarias.getString(KEY_ALIASES), String.class),
				stringToList(llavesPrimarias.getString(COLUMN_ALIASES), String.class))
				.flatMap(List::stream)
				.filter(llavePrimaria -> getParametrosSentencia().get().noneMatch(parametro -> llavePrimaria.equals(parametro)))
				.map(columnaIntermedia -> getColumnasResultado().get().filter(columna -> columna.equals(columnaIntermedia)).findFirst().get())
				.collect(toList());
		if (contieneSoloColumnasIntermedias()) columnasIntermedias.remove(getNumeroColumnasIntermedias() - 1);
	}


	public Supplier<Stream<String>> getColumnasRegulares() {
		return () -> columnasRegulares.stream();
	}

	private void setColumnasRegulares() {
		getColumnasResultado().get()
				.filter(columna -> getColumnasIntermedias().get().
									noneMatch(columnaIntermedia -> columnaIntermedia.equals(columna)))
				.collect(toList());
	}

	// ======== HELPERS ========= //

	private boolean contieneSoloColumnasIntermedias() {
		return getNumeroColumnasIntermedias() > 0 && columnasResultado.size() == getNumeroColumnasIntermedias();
	}

	private int getNumeroColumnasIntermedias() {
		return columnasIntermedias.size();
	}

	public Supplier<Stream<String>> getColumnasResultado() {
		return () -> columnasResultado.stream();
	}

	public boolean unicaColumnaResultado() {
		return columnasResultado.size() == 1;
	}

	private void setColumnasResultado() {
		columnasResultado = asList(substringBetween(super.getQueryString(), SPACE, FROM).split(COMA))
							.stream()
							.map(columna -> columna.trim())
							.collect(toList());
	}

	private String obtenerDatoDesdeSentencia(boolean paraKeySpaceName, String... separadores) {
		String sentenciaCQL = super.getQueryString();
		String dato = contains(sentenciaCQL, separadores[1])
							? substringBetween(sentenciaCQL, separadores[0], separadores[1])
							: contains(sentenciaCQL, separadores[2])
									? substringBetween(sentenciaCQL, separadores[0], separadores[2])
									: substringAfter(sentenciaCQL, separadores[0]);
		return (paraKeySpaceName ? substringBefore(dato, separadores[3]) : substringAfter(dato, separadores[3])).trim();
	}

}
