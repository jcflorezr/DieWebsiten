package com.diewebsiten.core.eventos.dto.transaccion;

import java.util.List;

import com.diewebsiten.core.almacenamiento.AlmacenamientoFabrica.MotoresAlmacenamiento;


public class Transaccion {

	private String nombre;
	private MotoresAlmacenamiento motorAlmacenamiento;
	private String sentencia;
	private Object[] parametrosTransaccion;
	private List<String> nombresFiltrosSentencia;
	
	Transaccion(){}
	
	public String getNombre() {
		return nombre;
	}
	
	public Transaccion setNombre(String nombreTransaccion) {
		this.nombre = nombreTransaccion;
		return this;
	}
	
	public MotoresAlmacenamiento getMotorAlmacenamiento() {
		return motorAlmacenamiento;
	}

	public Transaccion setMotorAlmacenamiento(MotoresAlmacenamiento motorAlmacenamiento) {
		this.motorAlmacenamiento = motorAlmacenamiento;
		return this;
	}

	public String getSentencia() {
		return sentencia;
	}
	
	public Transaccion setSentencia(String sentenciaTransaccion) {
		this.sentencia = sentenciaTransaccion;
		return this;
	}

	public Object[] getParametrosTransaccion() {	
		return parametrosTransaccion.clone();
	}

	public Transaccion setParametrosTransaccion(Object[] parametrosTransaccion) {
		this.parametrosTransaccion = parametrosTransaccion;
		return this;
	}

	public List<String> getNombresFiltrosSentencia() {
		return nombresFiltrosSentencia;
	}

	public Transaccion setNombresFiltrosSentencia(List<String> nombresFiltrosSentencia) {
		this.nombresFiltrosSentencia = nombresFiltrosSentencia;
		return this;
	}
	
	public Transaccion obtenerTransaccion(Transaccion transaccion, String nombreEvento) throws Exception {
		return this;
	}
	
}
