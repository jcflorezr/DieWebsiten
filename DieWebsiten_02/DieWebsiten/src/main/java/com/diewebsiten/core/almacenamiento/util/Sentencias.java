package com.diewebsiten.core.almacenamiento.util;

public enum Sentencias {
	
	LLAVES_PRIMARIAS("SentenciaLlavesPrimarias", "SELECT key_aliases, column_aliases FROM system.schema_columnfamilies WHERE keyspace_name = ? AND columnfamily_name = ?;"),
	TRANSACCIONES("SentenciaTransacciones", "SELECT transaccion, motoralmacenamiento, sentencia, filtrossentencia, resultadoenjerarquia FROM diewebsiten.eventos WHERE sitioweb = ? AND pagina = ? AND evento = ?;"),
	VALIDACIONES_EVENTO("SentenciaValidacionesEvento", "SELECT column_name, grupovalidacion, formaingreso, valorpordefecto FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND evento = ?;"),
    GRUPO_VALIDACIONES("SentenciaGrupoValidaciones", "SELECT tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupo_validacion = ?;");
	
	private String nombre;
	private String sentencia;
	
	private Sentencias(String nombre, String sentencia) {
		this.nombre = nombre;
		this.sentencia = sentencia;
	}

	public String nombre() {
		return nombre;
	}

	public String sentencia() {
		return sentencia;
	}

}
