package com.diewebsiten.core.almacenamiento;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ProveedorMySql extends ProveedorAlmacenamiento {

    @Override
    void conectar() {

    }

    @Override
    void desconectar() {

    }

    @Override
    Supplier<Stream<Map<String, Object>>> ejecutarTransaccion(String sentencia, Object[] parametros) {
        return null;
    }
}
