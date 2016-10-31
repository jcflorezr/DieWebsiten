package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Cassandra extends Sentencia {

	private PreparedStatement sentenciaPreparada;
	private String keyspaceName;
	private String columnfamilyName;
	private List<String> columnasIntermedias;
	private List<String> columnasRegulares;

	Cassandra() {
		columnasIntermedias = new ArrayList<>();
		columnasRegulares = new ArrayList<>();
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
		this.columnasIntermedias = columnasIntermedias;
	}

	public Supplier<Stream<String>> getColumnasRegulares() {
		return () -> columnasRegulares.stream();
	}

	void setColumnasRegulares(List<String> columnasRegulares) {
		this.columnasRegulares = columnasRegulares;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cassandra cassandra = (Cassandra) o;

		if (keyspaceName != null ? !keyspaceName.equals(cassandra.keyspaceName) : cassandra.keyspaceName != null)
			return false;
		if (columnfamilyName != null ? !columnfamilyName.equals(cassandra.columnfamilyName) : cassandra.columnfamilyName != null)
			return false;
		if (columnasIntermedias != null ? !columnasIntermedias.equals(cassandra.columnasIntermedias) : cassandra.columnasIntermedias != null)
			return false;
		return columnasRegulares != null ? columnasRegulares.equals(cassandra.columnasRegulares) : cassandra.columnasRegulares == null;

	}

	@Override
	public int hashCode() {
		int result = keyspaceName != null ? keyspaceName.hashCode() : 0;
		result = 31 * result + (columnfamilyName != null ? columnfamilyName.hashCode() : 0);
		result = 31 * result + (columnasIntermedias != null ? columnasIntermedias.hashCode() : 0);
		result = 31 * result + (columnasRegulares != null ? columnasRegulares.hashCode() : 0);
		return result;
	}
}
