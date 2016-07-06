package com.diewebsiten.core.almacenamiento.dto;


import com.diewebsiten.core.almacenamiento.ProveedorAlmacenamiento;

import java.util.Optional;

public class Conexion {

    private Optional<ProveedorAlmacenamiento> proveedorAlmacenamiento;
    private boolean conexionExitosa;
    private Throwable errorConexion;

    public Optional<ProveedorAlmacenamiento> getProveedorAlmacenamiento() {
        return proveedorAlmacenamiento;
    }

    public void setProveedorAlmacenamiento(ProveedorAlmacenamiento proveedorAlmacenamiento) {
        this.proveedorAlmacenamiento = Optional.of(proveedorAlmacenamiento);
    }

    public boolean isConexionExitosa() {
        return conexionExitosa;
    }

    public void setConexionExitosa(boolean conexionExitosa) {
        this.conexionExitosa = conexionExitosa;
    }

    public Throwable getErrorConexion() {
        return errorConexion;
    }

    public void setErrorConexion(Throwable errorConexion) {
        this.errorConexion = errorConexion;
    }

}
