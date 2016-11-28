
package com.diewebsiten.core.eventos;

import com.diewebsiten.core.eventos.dto.*;
import com.diewebsiten.core.eventos.dto.Campo.PorGrupoValidacion;
import com.diewebsiten.core.eventos.dto.Campo.PorGrupoValidacion.InformacionCampo;
import com.diewebsiten.core.eventos.dto.transaccion.Transaccion;
import com.diewebsiten.core.eventos.dto.transaccion.Transacciones;
import com.diewebsiten.core.eventos.dto.transaccion.columnar.Cassandra;
import com.diewebsiten.core.eventos.util.LogEventos;
import com.diewebsiten.core.excepciones.ExcepcionDeLog;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import static com.diewebsiten.core.almacenamiento.util.Sentencias.*;
import static com.diewebsiten.core.eventos.dto.transaccion.Transacciones.nuevaTransaccionCassandra;
import static com.diewebsiten.core.eventos.util.Mensajes.ERROR;
import static com.diewebsiten.core.eventos.util.Mensajes.Evento.EVENTO_NO_EXISTE;
import static com.diewebsiten.core.eventos.util.Mensajes.Evento.Formulario.CAMPOS_FORMULARIO_NO_EXISTEN;
import static com.diewebsiten.core.eventos.util.Mensajes.Evento.Formulario.PARAMETROS_FORMULARIO_NO_EXISTEN;
import static com.diewebsiten.core.eventos.util.Mensajes.Evento.Formulario.VALIDACIONES_NO_EXISTEN;
import static com.diewebsiten.core.eventos.util.ProcesamientoParametros.transformarParametro;
import static com.diewebsiten.core.eventos.util.ProcesamientoParametros.validarParametro;
import static com.google.common.base.Objects.firstNonNull;


/**
 * Este clase recibe todas las solicitudes de tipo consulta, inserción, modificación
 * y eliminación en la base de datos; luego retorna las respuestas que devuelve la capa de datos.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
public class Eventos {

	private Evento evento;
	private Formulario formulario;

    Eventos(String url, String nombreEvento, String parametros) throws Exception {
        evento = new Evento(url, nombreEvento, parametros);
		formulario = evento.getFormulario();
    }


    public ObjectNode ejecutar() throws ExcepcionDeLog {

		try {

			// TODO estos dos llamados a la BD pueden ir con CompletableFuture
			// http://www.deadcoderising.com/java8-writing-asynchronous-code-with-completablefuture/#callbackdependingonmultiplecomputations

			obtenerFormulario();
			obtenerTransacciones();

			if (validarEvento()) {
				ejecutarEvento();
			} else {
				evento.getResultadoFinal().putPOJO("VAL_" + evento.getNombreEvento(), formulario.getParametros());
			}
		} catch (Exception e) {
			Throwable excepcionReal = e.getCause();
			if (excepcionReal != null) {
				new LogEventos(evento).imprimirErrorEnLog(excepcionReal);
			} else {
				new LogEventos(evento).imprimirErrorEnLog(e);
			}
			evento.getResultadoFinal().put("error", ERROR.get());
		}
		return evento.getResultadoFinal();

    }

    private void obtenerFormulario() {
		Cassandra transaccion = nuevaTransaccionCassandra(VALIDACIONES_EVENTO.sentencia(), evento.getInformacionEvento());
        formulario.setCampos(transaccion.enJerarquiaConNombres());
	}

	private void obtenerTransacciones() {
		Cassandra transaccion = nuevaTransaccionCassandra(TRANSACCIONES.sentencia(), evento.getInformacionEvento());
		evento.setTransacciones(transaccion.plana());
	}

    private boolean validarEvento() throws Exception {

        if (formulario.sinCamposPeroConParametros()) {
            throw new ExcepcionGenerica(CAMPOS_FORMULARIO_NO_EXISTEN.get());
        } else if (formulario.conCamposPeroSinParametros()) {
            throw new ExcepcionGenerica(PARAMETROS_FORMULARIO_NO_EXISTEN.get());
        } else if (formulario.sinCampos()) {
			// Si no se encontraron campos para la ejecución de este evento significa que no los necesita.
        	return true;
        }

        // TODO como esperar a que los campos ya esten listos?
		formulario.getCamposPorGrupoValidacion().get().forEach(campoFormulario -> procesarFormulario(campoFormulario));

        return formulario.isValidacionExitosa();
        
    }

	/**
	 * Recibir los valores de los parámetros de un formulario, luego obtener de
	 * la base de datos la validación de cada parámetro y por último validar cada parámetro.
	 */
	private void procesarFormulario(Entry<String, PorGrupoValidacion> campoFormulario) {

		String grupoValidacion = campoFormulario.getKey();
		TreeMap<String, InformacionCampo> informacionCampo = campoFormulario.getValue().getColumnName();
		String columnName = informacionCampo.firstKey();
		InformacionCampo campo = informacionCampo.firstEntry().getValue();

		Cassandra transaccion = nuevaTransaccionCassandra(GRUPO_VALIDACIONES.sentencia(), grupoValidacion);
		campo.setValidaciones(transaccion.enJerarquiaConNombres());

		if (!campo.poseeValidaciones()) throw new ExcepcionGenerica(VALIDACIONES_NO_EXISTEN.get());

		Object valorParametroActual = formulario.getParametro(columnName);
		// TODO aqui podria ir otro CompletableFuture
		campo.getValidaciones()
				.map(validacion -> validarParametro(validacion, valorParametroActual))
				.filter(resultadoValidacion -> !resultadoValidacion.equals(valorParametroActual))
				.peek(resultadoValidacion -> formulario.setParametro(columnName, resultadoValidacion))
				.findFirst()
				.ifPresent(resultadoValidacion -> formulario.setValidacionExitosa(false));

		if (formulario.isValidacionExitosa()) {
			campo.getTransformaciones()
					.forEach(transformacion -> formulario.setParametro(columnName, transformarParametro(transformacion, valorParametroActual)));
		}

	}


    private void ejecutarEvento() throws Exception {

        if (!evento.poseeTransacciones()) throw new ExcepcionGenerica(EVENTO_NO_EXISTE.get());

		evento.getTransacciones().forEach(transaccion -> ejecutarTransaccion(transaccion));

    }

	private void ejecutarTransaccion(Transaccion transaccion) {

		// Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
		// y guardarlos en una lista para enviarlos a la sentencia preparada
		transaccion.setParametros(
			transaccion.getFiltrosSentencia().stream()
				.map(filtro -> formulario.getCampos().get()
						.filter(campo -> campo.getKey().equals(filtro))
						.map(campo -> firstNonNull(campo.getValue().getValorPorDefecto(),
												   formulario.getParametro(campo.getKey())))
						.findAny().get()
				).toArray());

		// Guardar los resultados de esta transacción dentro del resultado final de todos el evento
		JsonNode resultadoTransaccion = new Transacciones(transaccion).obtenerResultado();
		if (evento.getResultadoFinal().size() == 0) {
			evento.setResultadoFinal(resultadoTransaccion);
		} else if (resultadoTransaccion.size() > 0) {
			poblarResultadoFinal(resultadoTransaccion);
		}

	}
	
	private void poblarResultadoFinal(JsonNode coleccion) {
		coleccion.fieldNames().forEachRemaining(
				fieldName -> {Optional.ofNullable(evento.getResultadoFinal().get(fieldName))
									  .ifPresent(objetoActual -> poblarResultadoFinal(objetoActual));
							  evento.getResultadoFinal().setAll((ObjectNode) coleccion);}
		);
	}
	
}