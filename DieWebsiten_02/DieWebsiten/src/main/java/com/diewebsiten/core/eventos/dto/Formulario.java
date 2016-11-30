package com.diewebsiten.core.eventos.dto;

import com.diewebsiten.core.eventos.dto.Campo.PorGrupoValidacion;
import com.diewebsiten.core.eventos.dto.Campo.PorGrupoValidacion.InformacionCampo;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.util.Transformaciones.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Formulario {

	private AtomicBoolean validacionExitosa = new AtomicBoolean(true);

	// Campos
	private Campo campos;
	private boolean poseeCampos;
	// Parámetros
	private ObjectNode parametros;
	private boolean poseeParametros;

	public Formulario() {}

	public Formulario(String parametros) {
		setParametros(parametros);
	}

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
    
	public Supplier<Stream<Entry<String, InformacionCampo>>> getCampos() {
		return () -> campos.getGrupoValidacion().entrySet()
				.stream()
				.flatMap((k) -> k.getValue().getColumnName().entrySet().stream());
	}

	public Supplier<Stream<Entry<String, PorGrupoValidacion>>> getCamposPorGrupoValidacion() {
		return () -> campos.getGrupoValidacion().entrySet().stream();
	}

    public void setCampos(JsonNode camposFormularioEvento) {
    	campos = jsonToObject(camposFormularioEvento, Campo.class);
		if (campos.getGrupoValidacion() != null) poseeCampos = true; // El formulario sí posee campos
	}

	public boolean sinCampos() {
		return poseeCampos == true ? false : poseeCampos;
	}

    // =================================================================
    // ========================== Parámetros ===========================
	// === Guardar los parámetros que se recibieron desde el cliente ===
    // =================================================================
    
    public ObjectNode getParametros() {
    	/*
    	 * Copia defensiva del campo 'new Formulario().parametros'
    	 */
    	ObjectNode copiaDeParametros = newJsonObject();
		parametros.fields().forEachRemaining(actual -> copiaDeParametros.putPOJO(actual.getKey(), actual.getValue()));
    	return copiaDeParametros;
    }
    
    public String getParametro(String nombreParametro) throws ExcepcionGenerica {
    	JsonNode parametro = parametros.get(nombreParametro);
		return parametro != null ? parametro.asText() : "";
    }
    
    private void setParametros(String parametros) throws ExcepcionGenerica {
    	if (isNotBlank(parametros)) {
			this.parametros = stringToJsonObject(parametros);
			if (this.parametros != null && this.parametros.size() > 0) this.poseeParametros = true; // El formulario sí posee parámetros
    	} else {
			this.parametros = newJsonObject();
		}
    }
    
    public void setParametro(String nombreParametro, Object valorParametro) {
    	if (this.parametros != null) this.parametros.putPOJO(nombreParametro, valorParametro);
    }

	public boolean sinCamposPeroConParametros() {
		return !poseeCampos && poseeParametros;
	}

	public boolean conCamposPeroSinParametros() {
		return poseeCampos && !poseeParametros;
	}
}
