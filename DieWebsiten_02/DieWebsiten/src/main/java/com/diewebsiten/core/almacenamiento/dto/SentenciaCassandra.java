package com.diewebsiten.core.almacenamiento.dto;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.PreparedStatement;

public class SentenciaCassandra extends Sentencia {
	
	private String tipoTransaccion;
	private String keyspaceName;
	private String columnfamilyName;
	private List<String> llavesPrimarias;
	private PreparedStatement sentenciaPreparada;
	private int numeroParametros;
	
	public String getTipoTransaccion() {
		return tipoTransaccion;
	}
	
	public SentenciaCassandra setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
		return this;
	}
	
	public String getKeyspaceName() {
		return keyspaceName;
	}

	public SentenciaCassandra setKeyspaceName(String keyspaceName) {
		this.keyspaceName = keyspaceName;
		return this;
	}

	public String getColumnfamilyName() {
		return columnfamilyName;
	}
	
	public SentenciaCassandra setColumnfamilyName(String columnfamilyName) {
		this.columnfamilyName = columnfamilyName;
		return this;
	}
	
	public List<String> getLlavesPrimarias() {
		return new ArrayList<>(llavesPrimarias);
	}

	public SentenciaCassandra setLlavesPrimarias(List<String> llavesPrimarias) {
		this.llavesPrimarias = llavesPrimarias;
		return this;
	}

	public PreparedStatement getSentenciaPreparada() {
		return sentenciaPreparada;
	}

	public SentenciaCassandra setSentenciaPreparada(PreparedStatement sentenciaPreparada) {
		this.sentenciaPreparada = sentenciaPreparada;
		return this;
	}

	public int getNumeroParametros() {
		return numeroParametros;
	}

	public SentenciaCassandra setNumeroParametros(int numeroParametros) {
		this.numeroParametros = numeroParametros;
		return this;
	}

}
