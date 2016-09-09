package com.diewebsiten.core.eventos.dto;

import com.diewebsiten.core.eventos.dto.GrupoValidacion.Validacion;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;

import java.util.Map;
import java.util.stream.Stream;

public class Campo {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String VALIDACION = "Validación";
    private static final String TRANSFORMACION = "Transformación";

    private Map<String, InformacionCampo> columnName;

    @JsonProperty("column_name")
    public Map<String, InformacionCampo> getColumnName() {
        return columnName;
    }

    public void setColumnName(Map<String, InformacionCampo> columnName) {
        this.columnName = columnName;
    }

    public static class InformacionCampo {

        private String grupoValidacion;
        private String formaIngreso;
        private String valorPorDefecto;

        private GrupoValidacion validaciones;
        private boolean poseeValidaciones;

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

        // ==============================================================================
        // ====================== Validaciones y Transformaciones =======================
        // ====== Guardar las setValidaciones y transformaciones que posee cada campo ======
        // ==============================================================================

        public void setValidaciones(JsonNode grupoValidaciones) {
            validaciones = MAPPER.convertValue(grupoValidaciones, GrupoValidacion.class);
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