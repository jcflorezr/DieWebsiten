package com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.SentenciaColumnar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cassandra extends SentenciaColumnar {

	private PreparedStatement sentenciaPreparada;
	private String keyspaceName;
	private String columnfamilyName;

	Cassandra() {
		columnasPrimarias = new ArrayList<>();
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

	@Override
	public List<String> getColumnasPrimarias() {
		return Collections.unmodifiableList(columnasPrimarias);
	}

	void setColumnasIntermedias(List<String> columnasIntermedias) {
		this.columnasPrimarias = columnasIntermedias;
	}

	@Override
	public List<String> getColumnasRegulares() {
		return Collections.unmodifiableList(columnasRegulares);
	}

	void setColumnasRegulares(List<String> columnasRegulares) {
		this.columnasRegulares = columnasRegulares;
	}

}
