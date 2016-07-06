package com.diewebsiten.core.almacenamiento;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public class AlmacenamientoFabrica {
	
	private static Map<MotoresAlmacenamiento, Conexion> motoresAlmacenamiento = new EnumMap<>(MotoresAlmacenamiento.class);
	
	static {
		motoresAlmacenamiento.put(MotoresAlmacenamiento.CASSANDRA, ProveedorCassandra.getInstance());
	}
	
	public static ProveedorAlmacenamiento obtenerProveedorAlmacenamiento(MotoresAlmacenamiento motorAlmacenamiento) throws Exception {
		if (motorAlmacenamiento == null) {
			throw new ExcepcionGenerica("El nombre del motor de almacenamiento a obtener no puede ser nulo");
		}
		Optional<ProveedorAlmacenamiento> proveedorAlmacenamiento = motoresAlmacenamiento.get(motorAlmacenamiento).getProveedorAlmacenamiento();
		return proveedorAlmacenamiento.orElseThrow(() -> new ExcepcionGenerica("El motor de almacenamiento '" + motorAlmacenamiento + "' no fue encontrado"));
	}
	
	public static void desactivarProveedoresAlmacenamiento() {
		for (MotoresAlmacenamiento motorAlmacenamiento : motoresAlmacenamiento.keySet()) {
			switch (motorAlmacenamiento) {
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
