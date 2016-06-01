package com.diewebsiten.core.almacenamiento;

import java.util.List;

public abstract class ProveedorAlmacenamiento {
	
	public abstract List<?> ejecutarTransaccion(String sentenciaCQL, String nombreSentencia, Object[] parametros) throws Exception;

}
