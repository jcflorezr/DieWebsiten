package com.diewebsiten.core.eventos.dto;

public class DetallesSentencia {
	
	private String sentencia;
	private String nombreSentencia;
	private Object[] parametrosSentencia;
	
	public DetallesSentencia() {
	}
	
	public DetallesSentencia(DetallesSentencia detallesSentencia) {
		this.sentencia = detallesSentencia.getSentencia();
		this.nombreSentencia = detallesSentencia.getNombreSentencia();
		this.parametrosSentencia = detallesSentencia.getParametrosSentencia();
	}
	
	public String getSentencia() {
		return sentencia;
	}
	
	public void setSentencia(String sentencia) {
		this.sentencia = sentencia;
	}
	
	public String getNombreSentencia() {
		return nombreSentencia;
	}
	
	public void setNombreSentencia(String nombreSentencia) {
		this.nombreSentencia = nombreSentencia;
	}
	
	public Object[] getParametrosSentencia() {
		return parametrosSentencia;
	}
	
	public void setParametrosSentencia(Object[] parametrosSentencia) {
		this.parametrosSentencia = parametrosSentencia;
	}

}
