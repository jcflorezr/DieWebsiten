package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.almacenamiento.ProveedorCassandra.obtenerResultSetParametros;
import static com.diewebsiten.core.almacenamiento.util.Sentencias.LLAVES_PRIMARIAS;
import static com.diewebsiten.core.util.Transformaciones.stringToList;
import static java.util.stream.Collectors.toList;

public class Cassandra extends Sentencia {

	private PreparedStatement sentenciaPreparada;
	private String keyspaceName;
	private String columnfamilyName;
	private List<String> columnasIntermedias = new ArrayList<>();
	private List<String> columnasRegulares = new ArrayList<>();

	Cassandra() {
	}

	public PreparedStatement getSentenciaPreparada() {
		return sentenciaPreparada;
	}

	void setSentenciaPreparada(PreparedStatement sentenciaPreparada) {
		this.sentenciaPreparada = sentenciaPreparada;
	}

	public String getKeyspaceName() {
		return keyspaceName;
	}

	void setKeyspaceName(String keyspaceName) {
		this.keyspaceName = keyspaceName;
	}

	public String getColumnfamilyName() {
		return columnfamilyName;
	}

	void setColumnfamilyName(String columnfamilyName) {
		this.columnfamilyName = columnfamilyName;
	}

	public Supplier<Stream<String>> getColumnasIntermedias() {
		return () -> columnasIntermedias.stream();
	}

	void setColumnasIntermedias(List<String> columnasIntermedias) {
		Cassandra sentenciaLlavesPrimarias = new CassandraFactory().obtenerSentenciaCreada(LLAVES_PRIMARIAS.sentencia());
		Row llavesPrimarias = obtenerResultSetParametros.apply(sentenciaLlavesPrimarias, new Object[]{getKeyspaceName(), getColumnfamilyName()}).one();
		columnasIntermedias =
		Stream.of(stringToList(llavesPrimarias.getString(KEY_ALIASES), String.class),
				stringToList(llavesPrimarias.getString(COLUMN_ALIASES), String.class))
				.flatMap(List::stream)
				.filter(llavePrimaria -> getParametrosSentencia().get().noneMatch(parametro -> llavePrimaria.equals(parametro)))
				.map(columnaIntermedia -> getColumnasQuery().get().filter(columna -> columna.equals(columnaIntermedia)).findFirst().get())
				.collect(toList());
		if (contieneSoloColumnasIntermedias()) columnasIntermedias.remove(getNumeroColumnasIntermedias() - 1);
	}


	public Supplier<Stream<String>> getColumnasRegulares() {
		return () -> columnasRegulares.stream();
	}

	void setColumnasRegulares(List<String> columnasRegulares) {
		this.columnasRegulares = columnasRegulares;
	}

	private void setColumnasRegulares() {
		columnasQuery.get()
				.filter(columna -> getColumnasIntermedias().get().
						noneMatch(columnaIntermedia -> columnaIntermedia.equals(columna)))
				.collect(toList());
	}

}
