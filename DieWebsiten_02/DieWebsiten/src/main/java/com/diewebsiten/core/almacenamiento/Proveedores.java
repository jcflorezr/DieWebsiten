package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.EnumMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Proveedores implements AutoCloseable {
	
	private static Map<MotoresAlmacenamiento, ProveedorAlmacenamiento> instanciasBasesDeDatos = new EnumMap<>(MotoresAlmacenamiento.class);
	private static Object obj = new Object();

	public Proveedores() {
		obtenerProveedorAlmacenamiento(MotoresAlmacenamiento.CASSANDRA);
	}

	private static ProveedorAlmacenamiento obtenerProveedorAlmacenamiento(MotoresAlmacenamiento nombreBaseDeDatos) {
		ProveedorAlmacenamiento proveedorAlmacenamiento = instanciasBasesDeDatos.get(nombreBaseDeDatos);
		if (proveedorAlmacenamiento == null) {
			synchronized (obj) {
				proveedorAlmacenamiento = instanciasBasesDeDatos.get(nombreBaseDeDatos);
				if (instanciasBasesDeDatos.get(nombreBaseDeDatos) == null) {
					proveedorAlmacenamiento = crearNuevaInstanciaBaseDeDatos(nombreBaseDeDatos);
					proveedorAlmacenamiento.conectar();
					instanciasBasesDeDatos.put(nombreBaseDeDatos, proveedorAlmacenamiento);
				}
			}
		}
		return proveedorAlmacenamiento;
	}

	private static ProveedorAlmacenamiento crearNuevaInstanciaBaseDeDatos(MotoresAlmacenamiento nombreBaseDeDatos) {
		switch (nombreBaseDeDatos) {
			case CASSANDRA:
				return new ProveedorCassandra();
			case MYSQL:
				return new ProveedorMySql();
			default:
				throw new ExcepcionGenerica("'" + nombreBaseDeDatos + "' no es un motor de almacenamiento válido");
		}
	}

	public static JsonNode ejecutarTransaccion(Transaccion transaccion) {
		MotoresAlmacenamiento nombreBaseDeDatos = transaccion.getMotorAlmacenamiento();
		if (nombreBaseDeDatos == null) {
			throw new ExcepcionGenerica("El nombre del motor de almacenamiento a obtener no puede ser nulo");
		}
		if (isBlank(transaccion.getSentencia())) {
			throw new ExcepcionGenerica("No hay ninguna sentencia a ejecutar");
		}
		return obtenerProveedorAlmacenamiento(nombreBaseDeDatos).ejecutarTransaccion(transaccion);
	}

	@Override
	public void close() {
		instanciasBasesDeDatos.values().forEach(proveedorAlmacenamiento -> proveedorAlmacenamiento.desconectar());
		instanciasBasesDeDatos = null;
	}
	
	public enum MotoresAlmacenamiento {
		CASSANDRA, MYSQL
	}
}
