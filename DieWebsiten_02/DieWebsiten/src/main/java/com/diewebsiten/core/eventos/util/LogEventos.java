package com.diewebsiten.core.eventos.util;

import com.diewebsiten.core.excepciones.ExcepcionDeLog;
import com.diewebsiten.core.eventos.dto.Evento;
import com.diewebsiten.core.util.Log;

public class LogEventos {
	
	private Evento evento;
	
	public LogEventos(Evento evento) {
		this.evento = evento;
	}
	
	public void imprimirErrorEnLog(Throwable error) throws ExcepcionDeLog {
		StringBuilder trazaError = new StringBuilder();
		trazaError.append("\n").append("[SITIO WEB: '").append(evento.getSitioWeb()).append("'. ");
        trazaError.append("PÁGINA: '").append(evento.getPagina()).append("'. ");
        trazaError.append("EVENTO: '").append(evento.getNombreEvento()).append("']\n");
		trazaError.append("[PARÁMETROS] --> ").append(evento.getFormulario().getParametros());
		Log.getInstance().imprimirErrorEnLog(error, trazaError);
	}

}
