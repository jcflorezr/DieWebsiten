package com.diewebsiten.core.eventos.dto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.diewebsiten.core.util.Transformaciones.objectToList;

public class GrupoValidacion {

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
			else this.validacion = objectToList(validacion, String.class);
		}

	}

}
