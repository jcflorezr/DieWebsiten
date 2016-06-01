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
	private DetallesSentencia detallesSentencia;
	
	
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
	
	public void setColumnasConsultaSentenciaCql(List<String> columnasConsultaSentenciaCql) {
		this.columnasConsultaSentenciaCql = columnasConsultaSentenciaCql;
	}
	
	public List<String> getColumnasIntermediasSentenciaCql() {
		return columnasIntermediasSentenciaCql;
	}
	
	public void setColumnasIntermediasSentenciaCql(List<String> columnasIntermediasSentenciaCql) {
		this.columnasIntermediasSentenciaCql = columnasIntermediasSentenciaCql;
	}

	public DetallesSentencia getDetallesSentencia() {
		if (detallesSentencia != null) {
			return new DetallesSentencia(detallesSentencia);
		} else {
			return new DetallesSentencia();
		}
	}

	public void setDetallesSentencia(String sentencia, String nombreSentencia, Object[] parametros) {
		this.detallesSentencia.setSentencia(sentencia);
		this.detallesSentencia.setNombreSentencia(nombreSentencia);
    	this.detallesSentencia.setParametrosSentencia(parametros);
	}
	
}
