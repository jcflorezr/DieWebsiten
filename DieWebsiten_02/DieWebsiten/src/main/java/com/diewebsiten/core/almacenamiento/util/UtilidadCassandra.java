package com.diewebsiten.core.almacenamiento.util;

public class UtilidadCassandra {
	
	public static boolean validarTipoColumna(String tipoColumna) {
		
		try {
			Class.forName(tipoColumna);
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}

}
