package com.diewebsiten.core.almacenamiento.util;

public enum Sentencias {
	

	TRANSACCIONES("SentenciaTransacciones", "SELECT transaccion, motoralmacenamiento, sentencia, filtrossentencia, tiporesultado FROM diewebsiten.eventos WHERE sitioweb = ? AND pagina = ? AND evento = ?;"),
	VALIDACIONES_EVENTO("SentenciaValidacionesEvento", "SELECT column_name, grupovalidacion, formaingreso, valorpordefecto FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND evento = ?;"),
    GRUPO_VALIDACIONES("SentenciaGrupoValidaciones", "SELECT tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupovalidacion = ?;");
	
	private String nombre;
	private String sentencia;
	
	Sentencias(String nombre, String sentencia) {
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
