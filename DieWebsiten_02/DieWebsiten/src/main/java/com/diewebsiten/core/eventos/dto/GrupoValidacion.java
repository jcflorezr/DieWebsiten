package com.diewebsiten.core.eventos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class GrupoValidacion {

	private Map<String, Validacion> grupoValidacion;

    @JsonProperty("grupovalidacion")
    public Map<String, Validacion> getGrupoValidacion() {
        return grupoValidacion;
    }

    public void setGrupoValidacion(Map<String, Validacion> grupoValidacion) {
        this.grupoValidacion = grupoValidacion;
    }

    class Validacion {

		private String tipo;
		private String validacion;

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public String getValidacion() {
			return validacion;
		}

		public void setValidacion(String validacion) {
			this.validacion = validacion;
		}

	}

}
