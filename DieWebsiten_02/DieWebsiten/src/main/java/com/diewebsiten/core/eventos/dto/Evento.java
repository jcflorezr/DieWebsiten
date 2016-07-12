package com.diewebsiten.core.eventos.dto;

import static com.diewebsiten.core.eventos.dto.Transaccion.obtenerDatosTransaccion;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.util.ArrayList;
import java.util.List;

import com.diewebsiten.core.eventos.util.Mensajes;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Evento {
	
	/**
     * Este constructor se encarga de recibir un evento, este evento contiene transacciones de consulta,
     * inserción, actualización, eliminación, entre otras; relacionadas con las tablas
     * de la base de datos, Posteriormente ejecuta cada una de estas transacciones y retorna
     * los resultados de cada transacción en un string formato JSON.
     *
     * @param url Dirección del sitio web y de la página que está realizando la petición.
     * @param nombreEvento Nombre del evento que contiene las transacciones a ejecutar.
     * @param parametrosFormularioEvento Un Map que contiene los parámetros necesarios para ejecutar las
     * transacciones del evento.
     * @return String de tipo JSON con la respuesta de cada transacción que contiene el evento.
     * @throws java.lang.Exception
     */
    public Evento(String url, String nombreEvento, String parametrosFormularioEvento) throws Exception {
        
        // Validar que el nombre del evento no llegue vacío.
        if (isBlank(nombreEvento)) {
            throw new ExcepcionGenerica(Mensajes.Evento.NOMBRE_EVENTO_VACIO.get());
        } else {
        	// Obtener el nombre del evento que se ejecutará.
            this.nombreEvento = nombreEvento;
        }
        
        /*
         * AQUI FALTA UNA VALIDACION PARA 'NOMBRE SITIO WEB' Y 'NOMBRE PAGINA' 
         */
        
        
        // Obtener la dirección URL del sitio web que está realizando la petición.
        this.nombreSitioWeb = substringBefore(url, ":@:")/*.equals("localhost") ? "127.0.0.1" : sitioWeb*/;

        // Obtener el nombre de la página del sitio web que está realizando la petición.
        this.nombrePagina = !isBlank(substringAfter(url, ":@:")) ? substringAfter(url, ":@:") : "";
        
        this.informacionEvento = new Object[]{this.nombreSitioWeb, this.nombrePagina, this.nombreEvento};
        
        this.formulario = new Formulario();
        
        // Es posible que un evento no necesite formulario.
        this.formulario.setParametros(isBlank(parametrosFormularioEvento) ? "{}" : parametrosFormularioEvento);
        
        // Obtener el código del idioma en el que se desplegará la página, si no existe el código
        // se desplegará por defecto en idioma español (ES).        
        this.idioma = this.formulario.getParametro("lang") != null ? this.formulario.getParametro("lang") : "ES";
        
        // validacionExitosa = true
        this.formulario.setValidacionExitosa(true);
        
        // Inicializar el objeto JSON que va a contener el resultado de la ejecución del evento.
        this.resultadoFinal = new ObjectMapper().createObjectNode();
        
    }
	
	private final String nombreSitioWeb;
    private final String nombrePagina;
    private final String nombreEvento;
    private final String idioma;
    private final Object[] informacionEvento;
    private Formulario formulario;
    private List<Transaccion> transacciones;
    private boolean poseeTransacciones;
    private ObjectNode resultadoFinal;

    
    public String getSitioWeb() {
        return nombreSitioWeb;
    }

    public String getPagina() {
        return nombrePagina;
    }

    public String getIdioma() {
        return idioma;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public Object[] getInformacionEvento() {
    	return informacionEvento;
    }
    
	public Formulario getFormulario() {
		return formulario;
	}

	public List<Transaccion> getTransacciones() {
		/*
    	 * Copia defensiva del campo 'new Evento().transacciones'
    	 */
		return new ArrayList<>(transacciones);
	}

	public void setTransacciones(JsonElement transacciones) throws Exception {
		this.transacciones = new ArrayList<>();
		for (JsonElement transaccionActual : transacciones.getAsJsonArray()) {
			this.transacciones.add(obtenerDatosTransaccion(transaccionActual.getAsJsonObject(), this.nombreEvento));
		}
		if (!this.transacciones.isEmpty()) {
			this.poseeTransacciones = true;
		}
	}
	
	public boolean poseeTransacciones() {
		return poseeTransacciones;
	}

	public ObjectNode getResultadoFinal() {
		return resultadoFinal;
	}

	public void setResultadoFinal(JsonNode resultadoFinal) {
		this.resultadoFinal = (ObjectNode) resultadoFinal;
	}
	
}
