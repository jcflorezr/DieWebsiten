package com.diewebsiten.core.almacenamiento;

import java.util.List;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public abstract class ProveedorAlmacenamiento {
	
	abstract void conectar();
	
	abstract void desconectar();
	
	abstract List<?> consultar(DetallesSentencias detallesSentencia) throws ExcepcionGenerica;

}
