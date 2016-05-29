package com.diewebsiten.core.eventos.dto;

import java.util.List;

public class Transaccion {

	private String nombreTransaccion;
	private String tipo;
	private String columnfamilyName;
	private String sentenciaCql;
	private List<String> columnasFiltroSentenciaCql;
	private List<String> columnasConsultaSentenciaCql;
	private List<String> columnasIntermediasSentenciaCql;
	
	
	public String getNombreTransaccion() {
		return nombreTransaccion;
	}
	
	public void setNombreTransaccion(String nombreTransaccion) {
		this.nombreTransaccion = nombreTransaccion;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public String getColumnfamilyName() {
		return columnfamilyName;
	}
	
	public void setColumnfamilyName(String columnfamilyName) {
		this.columnfamilyName = columnfamilyName;
	}
	
	public String getSentenciaCql() {
		return sentenciaCql;
	}
	
	public void setSentenciaCql(String sentenciaCql) {
		this.sentenciaCql = sentenciaCql;
	}
	
	public List<String> getColumnasFiltroSentenciaCql() {
		return columnasFiltroSentenciaCql;
	}
	
	public void setColumnasFiltroSentenciaCql(List<String> filtrosSentenciaCql) {
		this.columnasFiltroSentenciaCql = filtrosSentenciaCql;
	}
	
	public List<String> getColumnasConsultaSentenciaCql() {
		return columnasConsultaSentenciaCql;
	}
	
	public void setColumnasConsultaSentenciaCql(
			List<String> columnasconsultasentenciacql) {
		this.columnasConsultaSentenciaCql = columnasconsultasentenciacql;
	}
	
	public List<String> getColumnasIntermediasSentenciaCql() {
		return columnasIntermediasSentenciaCql;
	}
	
	public void setColumnasIntermediasSentenciaCql(
			List<String> columnasintermediassentenciacql) {
		this.columnasIntermediasSentenciaCql = columnasintermediassentenciacql;
	}
	
}
