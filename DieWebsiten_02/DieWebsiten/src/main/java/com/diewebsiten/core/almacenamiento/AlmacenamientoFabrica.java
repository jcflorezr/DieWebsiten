package com.diewebsiten.core.almacenamiento;

import java.util.EnumMap;
import java.util.Map;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public class AlmacenamientoFabrica {
	
	private static Map<MotoresAlmacenamiento, ProveedorAlmacenamiento> motoresAlmacenamiento = new EnumMap<>(MotoresAlmacenamiento.class);
	
	static {
		motoresAlmacenamiento.put(MotoresAlmacenamiento.CASSANDRA, ProveedorCassandra.getInstance());
	}
	
	public static ProveedorAlmacenamiento obtenerProveedorAlmacenamiento(MotoresAlmacenamiento motorAlmacenamiento) throws Exception {
		if (motorAlmacenamiento == null) {
			throw new ExcepcionGenerica("El nombre del motor de almacenamiento a obtener no puede ser nulo");
		}
		ProveedorAlmacenamiento proveedorAlmacenamiento = motoresAlmacenamiento.get(motorAlmacenamiento);
		if (proveedorAlmacenamiento == null) {
			throw new ExcepcionGenerica("El motor de almacenamiento '" + motorAlmacenamiento + "' no fue encontrado");
		}
		return proveedorAlmacenamiento;
	}
	
	public static void desactivarProveedoresAlmacenamiento() {
		for (Map.Entry<MotoresAlmacenamiento, ProveedorAlmacenamiento> motorAlmacenamiento : motoresAlmacenamiento.entrySet()) {
			switch (motorAlmacenamiento.getKey()) {
				case CASSANDRA:
					ProveedorCassandra.getInstance();
					break;
				case MYSQL:
					break;
			
			}
		}
	}
	
	public enum MotoresAlmacenamiento {
		CASSANDRA, MYSQL
	}
}
