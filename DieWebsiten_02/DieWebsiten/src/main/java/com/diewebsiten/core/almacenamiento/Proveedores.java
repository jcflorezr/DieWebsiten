package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Proveedores implements AutoCloseable {
	
	private static Map<MotoresAlmacenamiento, ProveedorAlmacenamiento> instanciasBasesDeDatos = new EnumMap<>(MotoresAlmacenamiento.class);
	private static Map<Integer, Object> sentencias = new HashMap<>();
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

	public static Supplier<Stream<Map<String, Object>>> ejecutarTransaccion(String nombreBaseDeDatos, String sentencia, Object[] parametros) {
		MotoresAlmacenamiento motorAlmacenamiento = MotoresAlmacenamiento.valueOf(nombreBaseDeDatos);
		if (motorAlmacenamiento == null) throw new ExcepcionGenerica("El motor de almacenamiento '" + nombreBaseDeDatos + "' no está soportado");
		if (isBlank(sentencia)) throw new ExcepcionGenerica("No hay ninguna sentencia a ejecutar");
		return obtenerProveedorAlmacenamiento(motorAlmacenamiento).ejecutarTransaccion(sentencia, parametros);
	}

	@Override
	public void close() {
		instanciasBasesDeDatos.values().forEach(proveedorAlmacenamiento -> proveedorAlmacenamiento.desconectar());
		instanciasBasesDeDatos = null;
	}

	static Object obtenerSentenciaExistente(String sentencia) {
		return sentencias.get(sentencia.hashCode());
	}

	static void guardarNuevaSentencia(String sentencia, Object sentenciaPreparada) {
		int idSentencia = sentencia.hashCode();
		sentencias.put(idSentencia, sentenciaPreparada);
	}
	
	private enum MotoresAlmacenamiento {
		CASSANDRA, MYSQL
	}
}
