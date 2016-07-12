package com.diewebsiten.core.almacenamiento;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public class AlmacenamientoFabrica {
	
	private static Map<MotoresAlmacenamiento, Conexion> motoresAlmacenamiento = new EnumMap<>(MotoresAlmacenamiento.class);
	
	static {
		motoresAlmacenamiento.put(MotoresAlmacenamiento.CASSANDRA, ProveedorCassandra.inicializar());
	}
	
	public static ProveedorAlmacenamiento obtenerProveedorAlmacenamiento(MotoresAlmacenamiento motorAlmacenamiento) throws Exception {
		if (motorAlmacenamiento == null) {
			throw new ExcepcionGenerica("El nombre del motor de almacenamiento a obtener no puede ser nulo");
		}
		Conexion infoConexion = motoresAlmacenamiento.get(motorAlmacenamiento);
		Optional<ProveedorAlmacenamiento> proveedorAlmacenamiento = infoConexion.getProveedorAlmacenamiento();
		return proveedorAlmacenamiento.orElseThrow(() -> infoConexion.getErrorConexion());
	}
	
	public static void desactivarProveedoresAlmacenamiento() {
		for (Entry<MotoresAlmacenamiento, Conexion> motorAlmacenamiento : motoresAlmacenamiento.entrySet()) {
			switch (motorAlmacenamiento.getKey()) {
				case CASSANDRA:
					motorAlmacenamiento.getValue().getProveedorAlmacenamiento().ifPresent(ProveedorAlmacenamiento::desconectar);
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
