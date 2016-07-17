package com.diewebsiten.core.almacenamiento.dto;


import com.diewebsiten.core.almacenamiento.ProveedorAlmacenamiento;

import java.util.Optional;

public class Conexion {

    private Optional<ProveedorAlmacenamiento> proveedorAlmacenamiento = Optional.empty();
    private Exception errorConexion;

    public Optional<ProveedorAlmacenamiento> getProveedorAlmacenamiento() {
        return proveedorAlmacenamiento;
    }

    public Conexion setProveedorAlmacenamiento(ProveedorAlmacenamiento proveedorAlmacenamiento) {
        this.proveedorAlmacenamiento = Optional.of(proveedorAlmacenamiento);
        return this;
    }

    public Exception getErrorConexion() {
        return errorConexion;
    }

    public Conexion setErrorConexion(Exception errorConexion) {
        this.errorConexion = errorConexion;
        return this;
    }

}
