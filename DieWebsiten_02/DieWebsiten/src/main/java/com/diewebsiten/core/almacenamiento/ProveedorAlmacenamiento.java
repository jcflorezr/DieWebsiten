package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.eventos.dto.Transaccion;
import com.google.gson.JsonElement;

public abstract class ProveedorAlmacenamiento {
	
	public abstract JsonElement ejecutarTransaccion(Transaccion transaccion) throws Exception;

}
