package com.diewebsiten.core.almacenamiento.dto;


import com.diewebsiten.core.almacenamiento.ProveedorAlmacenamiento;

import java.util.Optional;

public class Conexion {

    private Optional<ProveedorAlmacenamiento> proveedorAlmacenamiento = Optional.empty();
    private Exception errorConexion;

    public Optional<ProveedorAlmacenamiento> getProveedorAlmacenamiento() {
        return proveedorAlmacenamiento;
    }

    public void setProveedorAlmacenamiento(ProveedorAlmacenamiento proveedorAlmacenamiento) {
        this.proveedorAlmacenamiento = Optional.of(proveedorAlmacenamiento);
    }

    public Exception getErrorConexion() {
        return errorConexion;
    }

    public void setErrorConexion(Exception errorConexion) {
        this.errorConexion = errorConexion;
    }

}
