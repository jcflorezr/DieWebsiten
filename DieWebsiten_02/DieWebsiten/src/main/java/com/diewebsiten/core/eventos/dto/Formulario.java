package com.diewebsiten.core.eventos.dto;

import static com.diewebsiten.core.util.Transformaciones.stringToList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.diewebsiten.core.eventos.util.Mensajes;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Formulario {
	
	private AtomicBoolean validacionExitosa = new AtomicBoolean(true);
	private List<Campo> campos;
	private boolean poseeCampos;
	private JsonObject parametros;
	private boolean poseeParametros;
    private JsonObject parametrosTransformados;
    private boolean poseeParametrosTransformados;


  	public boolean isValidacionExitosa() {
  		return validacionExitosa.get();
  	}

  	public void setValidacionExitosa(boolean validacionExitosa) {
  		if (!validacionExitosa && isValidacionExitosa()) {  			
  			this.validacionExitosa.set(validacionExitosa);
  		}
  	}
    
    // =====================================================
    // ====================== Campos =======================
    // === Guardar los campos que componen el formulario === 
    // =====================================================
    
	public Supplier<Stream<Campo>> getCampos() {
		return () -> campos.stream();
	}

    public void setCampos(JsonNode camposFormularioEvento) {
    	this.campos = stringToList(camposFormularioEvento.toString(), Campo.class);
		if (!this.campos.isEmpty()) this.poseeCampos = true; // El formulario sí posee campos
	}

	public boolean poseeCampos() {
		return poseeCampos;
	}





	private Map<String, GrupoValidacion> columnName;

	private String formaIngreso;
	private String valorPorDefecto;
	private boolean poseeValidaciones;

	@JsonProperty("column_name")
	public Map<String, GrupoValidacion> getColumnName() {
		return columnName;
	}

	public void setColumnName(Map<String, GrupoValidacion> columnName) {
		this.columnName = columnName;
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

//	public List<GrupoValidacion> getValidaciones() {
//		/*
//    	 * Copia defensiva del campo 'new Campo().validaciones'
//    	 */
//		return new ArrayList<>(validaciones);
//	}
//
//	public void setValidaciones(JsonNode validacionesCampo) {
//		this.validaciones = new ArrayList<>();
//		for (JsonNode validacionObject : validacionesCampo) {
//			GrupoValidacion validacion = new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).convertValue(validacionObject, GrupoValidacion.class);
////			JsonObject validacionActual = validacionObject.getAsJsonObject();
////			validacion.setTipo(validacionActual.get(TIPO).getAsString());
////			validacion.setValidacion(validacionActual.get(VALIDACION).getAsString());
//			this.validaciones.add(validacion);
//		}
//		if (!this.validaciones.isEmpty()) {
//    		this.poseeValidaciones = true; // El campo sí posee validaciones
//    	}
//	}
//
//	public boolean poseeValidaciones() {
//		return poseeValidaciones;
//	}







    // =================================================================
    // ========================== Parámetros ===========================
	// === Guardar los parámetros que se recibieron desde el cliente ===
    // =================================================================
    
    public JsonObject getParametros() {
    	/*
    	 * Copia defensiva del campo 'new Formulario().parametros'
    	 */
    	JsonObject copiaDeParametros = new JsonObject();
    	for (Map.Entry<String, JsonElement> copiaDeParametro : parametros.entrySet()) {
    		copiaDeParametros.addProperty(copiaDeParametro.getKey(), copiaDeParametro.getValue().getAsString());
    	}
    	return copiaDeParametros;
    }
    
    public String getParametro(String nombreParametro) throws ExcepcionGenerica {
    	JsonElement valorParametro = this.parametros.get(nombreParametro);
    	return valorParametro == null ? "" : valorParametro.getAsString();
    }
    
    public void setParametros(String parametros) throws ExcepcionGenerica {
    	if (isNotBlank(parametros)) {
    		try {
				this.parametros = new Gson().fromJson(parametros, new TypeToken<JsonObject>(){}.getType());
				if (!this.parametros.entrySet().isEmpty()) {					
					this.poseeParametros = true; // El formulario sí posee parámetros
				}
			} catch (JsonSyntaxException | ClassCastException e) {
				throw new ExcepcionGenerica("El String: '" + parametros + "' no tiene un formato de objeto JSON válido");
			}
    	}
    }

    /*
     * Este método se usa para reemplazar los parámetros recibidos desde el cliente
     * con los parámetros que se transformaron durante el proceso de validación
     */
    public void setParametros(JsonObject parametros) {
    	if (parametros != null) {
	    	for (Map.Entry<String, JsonElement> parametro : parametros.entrySet()) {
	    		this.parametros.addProperty(parametro.getKey(), parametro.getValue().getAsString());
	    	}
    	}
    }
    
    public boolean setParametros(String nombreParametro, Object valorParametro) {
    	if (this.parametros != null) {
    		String valorParametroString = valorParametro instanceof JsonElement ? ((JsonElement) valorParametro).getAsString() : (String) valorParametro;
    		this.parametros.addProperty(nombreParametro, valorParametroString);
    		return true;
    	}
    	return false;
    }
    
    public boolean poseeParametros() {
		return poseeParametros;
	}

    
    // ====================================================================================
    // ============================ Parámetros transformados ==============================
    // === Guardar los parámetros que se transformaron durante el proceso de validación ===
    // ====================================================================================
    
    public JsonObject getParametrosTransformados() {
		/*
    	 * Copia defensiva del campo 'new Formulario().parametrosTransformados'
    	 */
    	JsonObject copiaDeParametrosTransformados = new JsonObject();
    	for (Map.Entry<String, JsonElement> copiaDeParametro : parametrosTransformados.entrySet()) {
    		copiaDeParametrosTransformados.addProperty(copiaDeParametro.getKey(), copiaDeParametro.getValue().getAsString());
    	}
    	return copiaDeParametrosTransformados;
    }
	
    public void setParametrosTransformados(String nombreParametro, Object valorParametro, String nombreTransformacion) throws ExcepcionGenerica {
    	if (valorParametro == null) {
    		throw new ExcepcionGenerica(Mensajes.Evento.Formulario.TRANSFORMACION_FALLIDA.get(nombreParametro, nombreTransformacion));
    	}
		if (this.parametrosTransformados == null) {
			this.parametrosTransformados = new JsonObject();
		}
		String valorParametroString = valorParametro instanceof JsonElement ? ((JsonElement) valorParametro).getAsString() : (String) valorParametro;
    	this.parametrosTransformados.addProperty(nombreParametro, valorParametroString);
    	if (!this.poseeParametrosTransformados) {    		
    		this.poseeParametrosTransformados = true; // El formulario sí posee parámetros transformados
    	}
	}
    
    public boolean poseeParametrosTransformados() {
		return poseeParametrosTransformados;
	}

}
