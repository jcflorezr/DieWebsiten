package com.diewebsiten.core.almacenamiento;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import com.diewebsiten.core.almacenamiento.dto.Conexion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;

public class AlmacenamientoFabrica implements AutoCloseable {
	
	private static Map<MotoresAlmacenamiento, Conexion> instanciasBasesDeDatos = new EnumMap<>(MotoresAlmacenamiento.class);
	private Object obj = new Object();

	public AlmacenamientoFabrica() {
		if (instanciasBasesDeDatos.isEmpty()) {
			synchronized (obj) {
				if (instanciasBasesDeDatos.isEmpty()) {
					instanciasBasesDeDatos.put(MotoresAlmacenamiento.CASSANDRA, ProveedorCassandra.inicializar());
				}
			}
		}
	}
	
	public static ProveedorAlmacenamiento obtenerProveedorAlmacenamiento(MotoresAlmacenamiento nombreBaseDeDatos) throws Exception {
		if (nombreBaseDeDatos == null) {
			throw new ExcepcionGenerica("El nombre del motor de almacenamiento a obtener no puede ser nulo");
		}
		Conexion infoConexion = instanciasBasesDeDatos.get(nombreBaseDeDatos);
		return infoConexion.getProveedorAlmacenamiento().orElseThrow(() -> infoConexion.getErrorConexion());
	}

	@Override
	public void close() {
		for (Entry<MotoresAlmacenamiento, Conexion> motorAlmacenamiento : instanciasBasesDeDatos.entrySet()) {
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
