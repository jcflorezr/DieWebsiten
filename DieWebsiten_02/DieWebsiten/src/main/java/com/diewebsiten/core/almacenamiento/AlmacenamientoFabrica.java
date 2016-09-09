package com.diewebsiten.core.almacenamiento;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public class AlmacenamientoFabrica {
	
	private static Map<MotoresAlmacenamiento, Conexion> instanciasBasesDeDatos = new EnumMap<>(MotoresAlmacenamiento.class);
	private Object obj = new Object();

	static {
		instanciasBasesDeDatos.put(MotoresAlmacenamiento.CASSANDRA, ProveedorCassandra.inicializar());
	}
	
	public static ProveedorAlmacenamiento obtenerProveedorAlmacenamiento(MotoresAlmacenamiento nombreBaseDeDatos) {
		if (nombreBaseDeDatos == null) {
			throw new ExcepcionGenerica("El nombre del motor de almacenamiento a obtener no puede ser nulo");
		}
		Conexion infoConexion = instanciasBasesDeDatos.get(nombreBaseDeDatos);
		return infoConexion.getProveedorAlmacenamiento().orElseThrow(() -> infoConexion.getErrorConexion());
	}


	public static void desactivarProveedoresAlmacenamiento() {
		for (Entry<MotoresAlmacenamiento, Conexion> motorAlmacenamiento : instanciasBasesDeDatos.entrySet()) {
			switch (motorAlmacenamiento.getKey()) {
				case CASSANDRA:
					motorAlmacenamiento.getValue().getProveedorAlmacenamiento().ifPresent(ProveedorAlmacenamiento::desconectar);
					instanciasBasesDeDatos.remove(MotoresAlmacenamiento.CASSANDRA);
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
