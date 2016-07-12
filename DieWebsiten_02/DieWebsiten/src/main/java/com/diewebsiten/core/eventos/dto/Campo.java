package com.diewebsiten.core.eventos.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
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
	
    Campo(){}
	
    @JsonProperty("column_name")
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	@JsonProperty("grupovalidacion")
	public String getGrupoValidacion() {
		return grupoValidacion;
	}
	
	public void setGrupoValidacion(String grupoValidacion) {
		this.grupoValidacion = grupoValidacion;
	}
	
	@JsonProperty("formaingreso")
	public String getFormaIngreso() {
		return formaIngreso;
	}
	
	public void setFormaIngreso(String formaIngreso) {
		this.formaIngreso = formaIngreso;
	}
	
	@JsonProperty("valorpordefecto")
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

	public void setValidaciones(JsonNode validacionesCampo) {
		this.validaciones = new ArrayList<>();
		for (JsonNode validacionObject : validacionesCampo) {
			Validacion validacion = new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).convertValue(validacionObject, Validacion.class);
//			JsonObject validacionActual = validacionObject.getAsJsonObject();
//			validacion.setTipo(validacionActual.get(TIPO).getAsString());
//			validacion.setValidacion(validacionActual.get(VALIDACION).getAsString());
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