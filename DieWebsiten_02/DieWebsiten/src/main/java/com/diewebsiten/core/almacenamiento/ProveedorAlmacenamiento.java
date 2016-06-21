package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.eventos.dto.transaccion.Transaccion;
import com.google.gson.JsonElement;

public abstract class ProveedorAlmacenamiento {
	
	public abstract <T extends Transaccion> JsonElement ejecutarTransaccion(T transaccion) throws Exception;

}
