package com.diewebsiten.core.eventos.dto;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GrupoValidacion {

	private static final ObjectMapper mapper = new ObjectMapper();

	private Map<String, Validacion> tipo;

	public Map<String, Validacion> getTipo() {
		return tipo;
	}

	public void setTipo(Map<String, Validacion> tipo) {
		this.tipo = tipo;
	}

	public static class Validacion {

		private List<String> validacion;

		public List<String> getValidacion() {
			return validacion;
		}

		public void setValidacion(Object validacion) {
			if (validacion instanceof String) this.validacion = Arrays.asList((String) validacion);
			else this.validacion = mapper.convertValue(validacion, List.class);
		}

	}

}
