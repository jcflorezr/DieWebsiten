package com.diewebsiten.core.eventos.dto.transaccion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;


public class Transaccion {

	private String sentencia;
	private Object[] parametros;
	private String motorAlmacenamiento;
	private String tipoResultado;
	private String nombre;
	private List<String> filtrosSentencia;

	public Transaccion(){}

	protected Transaccion(String sentencia, String motorAlmacenamiento, Object... parametros) {
		this.sentencia = sentencia;
		this.motorAlmacenamiento = motorAlmacenamiento;
		this.parametros = parametros;
	}

	public String getSentencia() {
		return sentencia;
	}

	public void setSentencia(String sentencia) {
		this.sentencia = sentencia;
	}

	public Object[] getParametros() {
		return parametros;
	}

	public void setParametros(Object[] parametros) {
		this.parametros = parametros;
	}

	@JsonProperty("transaccion")
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@JsonProperty("motoralmacenamiento")
	public String getMotorAlmacenamiento() {
		return motorAlmacenamiento;
	}

	public void setMotorAlmacenamiento(String motorAlmacenamiento) {
		this.motorAlmacenamiento = motorAlmacenamiento;
	}

	@JsonProperty("filtrossentencia")
	public List<String> getFiltrosSentencia() {
		return filtrosSentencia;
	}

	public void setFiltrosSentencia(List<String> filtrosSentencia) {
		this.filtrosSentencia = filtrosSentencia;
	}

	@JsonProperty("tiporesultado")
	public String getTipoResultado() {
		return tipoResultado;
	}

	public void setTipoResultado(String tipoResultado) {
		this.tipoResultado = tipoResultado;
	}

	public JsonNode plana() {
		return new Transacciones(this).plano();
	}

	public ObjectNode conUnicaFila() {
		return (ObjectNode) plana().get(0);
	}

	@Override
	public String toString() {
		return "Transaccion{" + "hashCode=" + this.hashCode() +
				", nombre='" + nombre + '\'' +
				", motorAlmacenamiento=" + motorAlmacenamiento +
				", sentencia='" + sentencia + '\'' +
				", filtrosSentencia=" + filtrosSentencia +
				", tipoResultado=" + tipoResultado +
				", parametrosTransaccion=" + Arrays.toString(parametros) +
				'}';
	}
}
