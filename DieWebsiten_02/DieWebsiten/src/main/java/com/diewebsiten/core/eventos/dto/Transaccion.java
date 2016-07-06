package com.diewebsiten.core.eventos.dto;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.diewebsiten.core.almacenamiento.AlmacenamientoFabrica.MotoresAlmacenamiento;
import com.diewebsiten.core.eventos.util.Constantes;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class Transaccion {

	private String nombre;
	private MotoresAlmacenamiento motorAlmacenamiento;
	private String sentencia;
	private Object[] parametrosTransaccion;
	private List<String> filtrosSentencia;
	private String nombreEvento;
	private boolean resultadoEnJerarquia;
	
	
	public static Transaccion obtenerDatosTransaccion(JsonObject datosTransaccion) throws Exception {
    	return retornarDatosTransaccion(datosTransaccion, null);
    }
    
    public static Transaccion obtenerDatosTransaccion(JsonObject datosTransaccion, String nombreEvento) throws Exception {
    	return retornarDatosTransaccion(datosTransaccion, nombreEvento);
    }
	
	private static Transaccion retornarDatosTransaccion(JsonObject datosTransaccion, String nombreEvento) throws Exception {
		String nombreTransaccion = datosTransaccion.get(Constantes.Transacciones.NOMBRE_TRANSACCION.get()).getAsString();
		String sentencia = datosTransaccion.get(Constantes.Transacciones.SENTENCIA.get()).getAsString();
		JsonElement esResultadoEnJerarquia = datosTransaccion.get(Constantes.Transacciones.RESULTADO_EN_JERARQUIA.get());
		
		if (isBlank(nombreTransaccion) || isBlank(sentencia) || esResultadoEnJerarquia == null) {
			throw new Exception("Ninguno de estos dos valores puede ser nulo: Nombre Transaccion: " + nombreTransaccion + ", Sentencia: " + sentencia + ", Es Resultado En Jerarquía: " + esResultadoEnJerarquia);
		}
		
		Type arrayObjectType = new TypeToken<Object[]>(){private static final long serialVersionUID = 1L;}.getType();
		Type listStringType = new TypeToken<List<String>>(){private static final long serialVersionUID = 1L;}.getType();
		Gson gson = new Gson();
		
		JsonElement parametrosTransaccion = datosTransaccion.get(Constantes.Transacciones.PARAMETROS_TRANSACCION.get());
		JsonElement filtrosSentencia = datosTransaccion.get(Constantes.Transacciones.FILTROS_SENTENCIA.get());

		Transaccion transaccion = new Transaccion()
		.setNombre(nombreTransaccion)
		.setSentencia(sentencia)
		.setMotorAlmacenamiento(MotoresAlmacenamiento.valueOf(datosTransaccion.get(Constantes.Transacciones.MOTOR_ALMACENAMIENTO.get()).getAsString()))
		.setParametrosTransaccion((null != parametrosTransaccion) ? gson.fromJson(parametrosTransaccion, arrayObjectType) : null)
		.setFiltrosSentencia((null != filtrosSentencia) ? gson.fromJson(filtrosSentencia, listStringType) : null)
		.setNombreEvento(nombreEvento)
		.setResultadoEnJerarquia(esResultadoEnJerarquia.getAsBoolean());
		
		return transaccion;
	}
	
	public static Transaccion obtenerDatosTransaccionEventos(String sentencia, String nombre, Object[] parametros) {
		return new Transaccion()
		.setSentencia(sentencia)
		.setNombre(nombre)
		.setParametrosTransaccion(parametros)
		.setMotorAlmacenamiento(MotoresAlmacenamiento.CASSANDRA)
		.setResultadoEnJerarquia(false);
	}
	
	
	// =========================== 
	// === Getters and Setters ===
	// =========================== 
	
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

	public List<String> getFiltrosSentencia() {
		return new ArrayList<>(filtrosSentencia);
	}
	
	public Transaccion setFiltrosSentencia(List<String> filtrosSentencia) {
		this.filtrosSentencia = filtrosSentencia;
		return this;
	}

	public String getNombreEvento() {
		return nombreEvento;
	}

	public Transaccion setNombreEvento(String nombreEvento) {
		this.nombreEvento = nombreEvento;
		return this;
	}

	public boolean isResultadoEnJerarquia() {
		return resultadoEnJerarquia;
	}

	public Transaccion setResultadoEnJerarquia(boolean resultadoEnJerarquia) {
		this.resultadoEnJerarquia = resultadoEnJerarquia;
		return this;
	}
	
}
