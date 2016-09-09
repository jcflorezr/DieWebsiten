package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.eventos.dto.Transaccion;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class ProveedorAlmacenamiento {

	public abstract JsonNode ejecutarTransaccion(Transaccion transaccion);

	abstract void desconectar();

}
