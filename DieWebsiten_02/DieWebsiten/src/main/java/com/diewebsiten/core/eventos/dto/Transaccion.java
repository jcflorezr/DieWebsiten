package com.diewebsiten.core.eventos.dto;

import com.diewebsiten.core.almacenamiento.AlmacenamientoFabrica.MotoresAlmacenamiento;
import com.diewebsiten.core.almacenamiento.dto.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.Sentencia.TiposResultado;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;


public class Transaccion {

	private String nombre;
	private MotoresAlmacenamiento motorAlmacenamiento;
	private String sentencia;
	private List<String> filtrosSentencia;
	private TiposResultado tipoResultado;
	private Object[] parametrosTransaccion;

	public Transaccion() {}

	public Transaccion(String sentencia, String nombre, TiposResultado tipoResultado, Object... parametrosTransaccion) {
		this.nombre = nombre;
		this.parametrosTransaccion = parametrosTransaccion;
		this.sentencia = sentencia;
		this.tipoResultado = tipoResultado;
		this.motorAlmacenamiento = MotoresAlmacenamiento.CASSANDRA;
	}

	@JsonProperty("transaccion")
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@JsonProperty("motoralmacenamiento")
	public MotoresAlmacenamiento getMotorAlmacenamiento() {
		return motorAlmacenamiento;
	}

	public void setMotorAlmacenamiento(MotoresAlmacenamiento motorAlmacenamiento) {
		this.motorAlmacenamiento = motorAlmacenamiento;
	}

	public String getSentencia() {
		return sentencia;
	}

	public void setSentencia(String sentencia) {
		this.sentencia = sentencia;
	}

	@JsonProperty("filtrossentencia")
	public List<String> getFiltrosSentencia() {
		return filtrosSentencia;
	}

	public void setFiltrosSentencia(List<String> filtrosSentencia) {
		this.filtrosSentencia = filtrosSentencia;
	}

	@JsonProperty("tiporesultado")
	public Sentencia.TiposResultado getTipoResultado() {
		return tipoResultado;
	}

	public void setTipoResultado(Sentencia.TiposResultado tipoResultado) {
		this.tipoResultado = tipoResultado;
	}

	public Object[] getParametrosTransaccion() {
		return parametrosTransaccion;
	}

	public void setParametrosTransaccion(Object[] parametrosTransaccion) {
		this.parametrosTransaccion = parametrosTransaccion;
	}


	@Override
	public String toString() {
		return "Transaccion{" + "hashCode=" + this.hashCode() + "," +
				"nombre='" + nombre + '\'' +
				", motorAlmacenamiento=" + motorAlmacenamiento +
				", sentencia='" + sentencia + '\'' +
				", filtrosSentencia=" + filtrosSentencia +
				", tipoResultado=" + tipoResultado +
				", parametrosTransaccion=" + Arrays.toString(parametrosTransaccion) +
				'}';
	}
}
