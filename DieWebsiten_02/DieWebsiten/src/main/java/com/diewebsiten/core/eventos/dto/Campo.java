package com.diewebsiten.core.eventos.dto;

import com.diewebsiten.core.eventos.dto.GrupoValidacion.Validacion;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.diewebsiten.core.util.Transformaciones.jsonToObject;

public class Campo {

    private static final String VALIDACION = "Validación";
    private static final String TRANSFORMACION = "Transformación";

    private Map<String, PorGrupoValidacion> grupoValidacion;

    @JsonProperty("grupovalidacion")
    public Map<String, PorGrupoValidacion> getGrupoValidacion() {
        return grupoValidacion;
    }

    public void setGrupoValidacion(TreeMap<String, PorGrupoValidacion> grupoValidacion) {
        this.grupoValidacion = grupoValidacion;
    }

    public static class PorGrupoValidacion {

        private TreeMap<String, InformacionCampo> columnName;

        @JsonProperty("column_name")
        public TreeMap<String, InformacionCampo> getColumnName() {
            return columnName;
        }

        public void setColumnName(TreeMap<String, InformacionCampo> columnName) {
            this.columnName = columnName;
        }

        public static class InformacionCampo {

            private String formaIngreso;
            private String valorPorDefecto;

            private GrupoValidacion validaciones;
            private boolean poseeValidaciones;

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

            // ==============================================================================
            // ====================== Validaciones y Transformaciones =======================
            // ==== Guardar las setValidaciones y transformaciones que posee cada campo =====
            // ==============================================================================

            public void setValidaciones(JsonNode grupoValidaciones) {
                validaciones = jsonToObject(grupoValidaciones, GrupoValidacion.class);
                if (validaciones.getTipo() != null && !validaciones.getTipo().isEmpty()) this.poseeValidaciones = true; // El campo sí posee setValidaciones
            }

            public Stream<String> getValidaciones() {
                return validaciones.getTipo().get(VALIDACION).getValidacion().stream();
            }

            public Stream<String> getTransformaciones() {
                Validacion transformaciones = validaciones.getTipo().get(TRANSFORMACION);
                return transformaciones != null ? transformaciones.getValidacion().stream() : Stream.empty();
            }

            public boolean poseeValidaciones() {
                return poseeValidaciones;
            }

        }
    }
}
