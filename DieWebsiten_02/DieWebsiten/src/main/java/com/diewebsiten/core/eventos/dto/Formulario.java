package com.diewebsiten.core.eventos.dto;

import com.diewebsiten.core.eventos.dto.Campo.InformacionCampo;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Formulario {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private AtomicBoolean validacionExitosa = new AtomicBoolean(true);

	// Campos
	private Campo campos;
	private boolean poseeCampos;
	// Parámetros
	private ObjectNode parametros;
	private boolean poseeParametros;


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
    
	public Supplier<Stream<Map.Entry<String, InformacionCampo>>> getCampos() {
		return () -> campos.getColumnName().entrySet().stream();
	}

    public void setCampos(JsonNode camposFormularioEvento) {
    	campos = MAPPER.convertValue(camposFormularioEvento, Campo.class);
		if (campos.getColumnName() != null) poseeCampos = true; // El formulario sí posee campos
	}

	public boolean poseeCampos() {
		return poseeCampos;
	}

    // =================================================================
    // ========================== Parámetros ===========================
	// === Guardar los parámetros que se recibieron desde el cliente ===
    // =================================================================
    
    public ObjectNode getParametros() {
    	/*
    	 * Copia defensiva del campo 'new Formulario().parametros'
    	 */
    	ObjectNode copiaDeParametros = MAPPER.createObjectNode();
		parametros.fields().forEachRemaining(actual -> copiaDeParametros.putPOJO(actual.getKey(), actual.getValue()));
    	return copiaDeParametros;
    }
    
    public String getParametro(String nombreParametro) throws ExcepcionGenerica {
    	JsonNode parametro = parametros.get(nombreParametro);
		return parametro != null ? parametro.asText() : "";
    }
    
    public void setParametro(String parametros) throws ExcepcionGenerica {
    	if (isNotBlank(parametros)) {
    		try {
				this.parametros = (ObjectNode) MAPPER.readTree(parametros);
				if (this.parametros != null && this.parametros.size() > 0) this.poseeParametros = true; // El formulario sí posee parámetros
			} catch (IOException e) {
				throw new ExcepcionGenerica("El String: '" + parametros + "' no tiene un formato de objeto JSON válido. MOTIVO: " + e.getMessage());
			}
    	}
    }
    
    public void setParametro(String nombreParametro, Object valorParametro) {
    	if (this.parametros != null) this.parametros.putPOJO(nombreParametro, valorParametro);
    }
    
    public boolean poseeParametros() {
		return poseeParametros;
	}

}
