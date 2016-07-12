package com.diewebsiten.core.almacenamiento.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.PreparedStatement;

public class SentenciaCassandra extends Sentencia {
	
	private Optional<String> keyspaceName;
	private Optional<String> columnfamilyName;
	private PreparedStatement sentenciaPreparada;
	private List<String> parametrosSentencia;
	private List<Definition> columnasIntermedias;
	private List<Definition> columnasRegulares;
	
	
	public SentenciaCassandra(PreparedStatement sentenciaPreparada, List<String> parametrosSentencia) {
		this.sentenciaPreparada = sentenciaPreparada;
		this.parametrosSentencia = parametrosSentencia;
		this.keyspaceName = Optional.empty();
		this.columnfamilyName = Optional.empty();
		this.columnasIntermedias = new ArrayList<>();
		this.columnasRegulares = new ArrayList<>();
	}

	
	public Optional<String> getKeyspaceName() {
		return keyspaceName;
	}

	public void setKeyspaceName(String keyspaceName) {
		this.keyspaceName = Optional.ofNullable(keyspaceName);
	}
	
	public PreparedStatement getSentenciaPreparada() {
		return sentenciaPreparada;
	}

	public void setSentenciaPreparada(PreparedStatement sentenciaPreparada) {
		this.sentenciaPreparada = sentenciaPreparada;
	}

	public Supplier<Stream<String>> getParametrosSentencia() {
		return () -> parametrosSentencia.stream();
	}

	public int getNumeroParametrosSentencia() {
		return parametrosSentencia.size();
	}

	public Optional<String> getColumnfamilyName() {
		return columnfamilyName;
	}

	public void setColumnfamilyName(String columnfamilyName) {
		this.columnfamilyName = Optional.ofNullable(columnfamilyName);
	}
	
	public Supplier<Stream<Definition>> getColumnasIntermedias() {
		return () -> columnasIntermedias.stream();
	}
	
	public void setColumnasIntermedias(List<Definition> columnasIntermedias) {
		this.columnasIntermedias = Optional.ofNullable(columnasIntermedias).orElse(new ArrayList<>());
	}
	
	public int getNumeroColumnasIntermedias() {
		return columnasIntermedias.size();
	}
	
	public Supplier<Stream<Definition>> getColumnasRegulares() {
		return () -> columnasRegulares.stream();
	}

	public void setColumnasRegulares(List<Definition> columnasRegulares) {
		this.columnasRegulares = Optional.ofNullable(columnasRegulares).orElse(new ArrayList<>());
	}
	
	public int getNumeroColumnasRegulares() {
		return columnasRegulares.size();
	}

}
