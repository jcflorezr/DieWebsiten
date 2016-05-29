package com.diewebsiten.core.eventos.dto;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Formulario {
	
	private List<Campo> campos;
	private boolean poseeCampos;
	private JsonObject parametros;
	private boolean poseeParametros;
    private JsonObject parametrosTransformados;
    private boolean poseeParametrosTransformados;
    private AtomicBoolean validacionExitosa = new AtomicBoolean();
    
    
    private static final String COLUMN_NAME = "column_name";
    private static final String GRUPO_VALIDACION = "grupovalidacion";
    private static final String FORMA_INGRESO = "formaingreso";
    private static final String VALOR_POR_DEFECTO = "valorpordefecto";
    
    
    // =====================================================
    // ====================== Campos =======================
    // === Guardar los campos que componen el formulario === 
    // =====================================================
    
	public List<Campo> getCampos() {
		/*
    	 * Copia defensiva del campo 'new Formulario().campos'
    	 */
		return new ArrayList<>(campos);
	}

    public void setCampos(List<JsonObject> camposFormularioEvento) {
    	this.campos = new ArrayList<>();
    	for (JsonObject campoObject : camposFormularioEvento) {
    		Campo campo = new Campo();
    		campo.setColumnName(campoObject.get(COLUMN_NAME).getAsString());
    		campo.setGrupoValidacion(campoObject.get(GRUPO_VALIDACION).getAsString());
    		campo.setFormaIngreso(campoObject.get(FORMA_INGRESO).getAsString());
    		campo.setValorPorDefecto(campoObject.get(VALOR_POR_DEFECTO).getAsString());
    		this.campos.add(campo);
    	}
    	if (!this.campos.isEmpty()) {
    		this.poseeCampos = true; // El formulario sí posee campos
    	}
	}

	public boolean poseeCampos() {
		return poseeCampos;
	}
	
    
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
	
    public void setParametrosTransformados(String nombreParametro, Object valorParametro) {
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
    
    // Validación exitosa

	public boolean isValidacionExitosa() {
		return validacionExitosa.get();
	}

	public void setValidacionExitosa(boolean validacionExitosa) {
		this.validacionExitosa.set(validacionExitosa);
	}

}
