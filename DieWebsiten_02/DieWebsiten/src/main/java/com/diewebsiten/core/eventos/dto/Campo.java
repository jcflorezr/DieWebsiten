package com.diewebsiten.core.eventos.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

public class Campo {

	private String columnName;
	private String grupoValidacion;
	private String formaIngreso;
	private String valorPorDefecto;
	private List<Validacion> validaciones;
	private boolean poseeValidaciones;
	
	private static final String TIPO = "tipo";
    private static final String VALIDACION = "validacion";
	
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public String getGrupoValidacion() {
		return grupoValidacion;
	}
	
	public void setGrupoValidacion(String grupoValidacion) {
		this.grupoValidacion = grupoValidacion;
	}
	
	public String getFormaIngreso() {
		return formaIngreso;
	}
	
	public void setFormaIngreso(String formaIngreso) {
		this.formaIngreso = formaIngreso;
	}
	
	public String getValorPorDefecto() {
		return valorPorDefecto;
	}
	
	public void setValorPorDefecto(String valorPorDefecto) {
		this.valorPorDefecto = valorPorDefecto;
	}

	public List<Validacion> getValidaciones() {
		/*
    	 * Copia defensiva del campo 'new Campo().validaciones'
    	 */
		return new ArrayList<>(validaciones);
	}

	public void setValidaciones(List<JsonObject> validacionesCampo) {
		this.validaciones = new ArrayList<>();
		for (JsonObject validacionesObject : validacionesCampo) {
			Validacion validacion = new Validacion();
			validacion.setTipo(validacionesObject.get(TIPO).getAsString());
			validacion.setValidacion(validacionesObject.get(VALIDACION).getAsString());
			this.validaciones.add(validacion);
		}
		if (!this.validaciones.isEmpty()) {
    		this.poseeValidaciones = true; // El campo sí posee validaciones
    	}
	}

	public boolean poseeValidaciones() {
		return poseeValidaciones;
	}
	
}