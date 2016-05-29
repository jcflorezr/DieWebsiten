package com.diewebsiten.core.almacenamiento;

public class DetallesSentencias {
	
	private String sentencia;
	private String nombreSentencia;
	private Object[] parametrosSentencia;
	
	
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
