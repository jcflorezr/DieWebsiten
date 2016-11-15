package com.diewebsiten.core.almacenamiento;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class ProveedorAlmacenamiento {

	abstract void conectar();

	abstract void desconectar();

	abstract Supplier<Stream<Map<String, Object>>> ejecutarTransaccion(String sentencia, Object [] parametros);

}
