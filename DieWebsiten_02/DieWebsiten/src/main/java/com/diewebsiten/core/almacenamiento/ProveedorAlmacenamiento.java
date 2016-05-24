package com.diewebsiten.core.almacenamiento;

import java.util.List;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public abstract class ProveedorAlmacenamiento {
	
	abstract void conectar();
	
	abstract void desconectar();
	
	abstract List<?> consultar(String sentencia) throws ExcepcionGenerica;
	
	abstract List<?> consultar(String sentencia, Object[] parametros) throws ExcepcionGenerica;
	
	abstract List<?> consultar(String sentencia, String nombreSentencia, Object[] parametros) throws ExcepcionGenerica;

}
