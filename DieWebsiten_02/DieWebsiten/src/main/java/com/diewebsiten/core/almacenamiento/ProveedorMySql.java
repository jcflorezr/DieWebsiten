package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.eventos.dto.Transaccion;
import com.fasterxml.jackson.databind.JsonNode;

public class ProveedorMySql extends ProveedorAlmacenamiento {
    @Override
    JsonNode ejecutarTransaccion(Transaccion transaccion) {
        return null;
    }

    @Override
    void conectar() {

    }

    @Override
    void desconectar() {

    }
}
