
package com.diewebsiten.core.eventos;

import static com.diewebsiten.core.almacenamiento.dto.Sentencia.TiposResultado.JERARQUÍA_CON_NOMBRES_DE_COLUMNAS;
import static com.diewebsiten.core.almacenamiento.dto.Sentencia.TiposResultado.PLANO;
import static com.diewebsiten.core.almacenamiento.util.Sentencias.*;
import static com.diewebsiten.core.eventos.util.Mensajes.*;
import static com.diewebsiten.core.eventos.util.Mensajes.Evento.*;
import static com.diewebsiten.core.eventos.util.Mensajes.Evento.Formulario.*;
import static com.diewebsiten.core.eventos.util.ProcesamientoParametros.transformarParametro;
import static com.diewebsiten.core.eventos.util.ProcesamientoParametros.validarParametro;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.diewebsiten.core.eventos.dto.Campo.InformacionCampo;
import com.diewebsiten.core.eventos.dto.Evento;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.eventos.util.LogEventos;
import com.diewebsiten.core.excepciones.ExcepcionDeLog;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


/**
 * Este clase recibe todas las solicitudes de tipo consulta, inserción, modificación
 * y eliminación en la base de datos; luego retorna las respuestas que devuelve la capa de datos.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
public class Eventos implements Callable<ObjectNode> {
    
    private Evento evento;

    Eventos(String url, String nombreEvento, String parametros) throws Exception {
        evento = new Evento(url, nombreEvento, parametros);
    }


    @Override
    public ObjectNode call() throws ExcepcionDeLog {

		try {
			if (validarEvento()) {
				ejecutarEvento();
			} else {
				evento.getResultadoFinal().putPOJO("VAL_" + evento.getNombreEvento(), evento.getFormulario().getParametros());
			}
			return evento.getResultadoFinal();
		} catch (Exception e) {
			Throwable excepcionReal = e.getCause();
			if (excepcionReal != null) {
				new LogEventos(evento).imprimirErrorEnLog(excepcionReal);
			} else {
				new LogEventos(evento).imprimirErrorEnLog(e);
			}
			evento.getResultadoFinal().put("error", ERROR.get());
			return evento.getResultadoFinal();
		}

    }

    /**
     *
     * @return
     * @throws Exception
     */
    private boolean validarEvento() {
        	
    	Transaccion datosValidaciones = new Transaccion(VALIDACIONES_EVENTO.sentencia(), VALIDACIONES_EVENTO.nombre(), JERARQUÍA_CON_NOMBRES_DE_COLUMNAS, evento.getInformacionEvento());
        evento.getFormulario().setCampos(ejecutarTransaccion(datosValidaciones));
        
        if (!evento.getFormulario().poseeCampos() && evento.getFormulario().poseeParametros()) {
            throw new ExcepcionGenerica(CAMPOS_FORMULARIO_NO_EXISTEN.get());
        } else if (evento.getFormulario().poseeCampos() && !evento.getFormulario().poseeParametros()) {
            throw new ExcepcionGenerica(PARAMETROS_FORMULARIO_NO_EXISTEN.get());
        } else if (!evento.getFormulario().poseeCampos()) {
        	return true; // Si no se encontraron campos para la ejecución de este evento significa que no los necesita.
        }

        ExecutorService grupoEjecucion = obtenerGrupoEjecucion();
        try {
			evento.getFormulario().getCampos().get().forEach(campoFormulario ->
					grupoEjecucion.execute(new ValidacionFormularios(campoFormulario.getKey(), campoFormulario.getValue())));
		} finally {
			if (grupoEjecucion != null) grupoEjecucion.shutdown();
		}

        if (!evento.getFormulario().isValidacionExitosa()) return false;

        return true;
        
    }
    
    /**
     *
     * @return
     */
    private void ejecutarEvento() {
    	
    	Transaccion datosTransacciones = new Transaccion(TRANSACCIONES.sentencia(), TRANSACCIONES.nombre(), PLANO, evento.getInformacionEvento());

        // Obtener la información de las transacciones que se ejecutarán en el evento actual.
    	evento.setTransacciones(ejecutarTransaccion(datosTransacciones));

        // Validar que el evento existe.
        if (!evento.poseeTransacciones()) throw new ExcepcionGenerica(EVENTO_NO_EXISTE.get());

		ExecutorService grupoEjecucion = obtenerGrupoEjecucion();
		try {
			List<Future<Void>> l = new ArrayList<>();
			for (Transaccion transaccion : evento.getTransacciones()) {
				l.add(grupoEjecucion.submit(new EjecucionTransacciones(transaccion)));
			}
			for (Future<Void> f : l) f.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			if (grupoEjecucion != null) grupoEjecucion.shutdown();
		}

    }

    private ExecutorService obtenerGrupoEjecucion() {
		final ThreadFactory threadFactoryBuilder = new ThreadFactoryBuilder().setNameFormat(evento.getNombreEvento() + "-%d").setDaemon(true).build();
        return Executors.newFixedThreadPool(10, threadFactoryBuilder);
	}
    
    /*
     * Para consultas con resultado en jerarquía
     */
    public static JsonNode ejecutarTransaccion(Transaccion transaccion) {
    	return FachadaEventos.ejecutarTransaccion(transaccion); 
    }
    
    
    // =========================================================================== //
    // =========================== FORMULARIOS =================================== //
    // =========================================================================== //
	
	private class ValidacionFormularios implements Runnable {

		private final String columnName;
	    private final InformacionCampo campo;
	    
	    private ValidacionFormularios(String columnName, InformacionCampo campo) {
	    	this.columnName = columnName;
	        this.campo = campo;
	    } 
	    
	    @Override
	    public void run() {
	    	try {			
	    		procesarFormulario();
			} catch (Exception e) {
				Throwable excepcionReal = e.getCause();
				if (excepcionReal != null) {
					throw new RuntimeException(excepcionReal);
				} else {
					throw e;
				}
			}
	    }
	    
	    /**
	     * Recibir los valores de los parámetros de un formulario, luego obtener de
	     * la base de datos la validación de cada parámetro y por último validar cada parámetro.
	     */
	    private void procesarFormulario() {

	        String grupoValidacionCampo = campo.getGrupoValidacion();
	        Transaccion datosGruposValidaciones = new Transaccion(GRUPO_VALIDACIONES.sentencia(), GRUPO_VALIDACIONES.nombre(), JERARQUÍA_CON_NOMBRES_DE_COLUMNAS, grupoValidacionCampo);
	        campo.setValidaciones(Eventos.ejecutarTransaccion(datosGruposValidaciones));

	        // Validar que sí existan las setValidaciones del grupo.
	        if (!campo.poseeValidaciones()) throw new ExcepcionGenerica(Formulario.VALIDACIONES_NO_EXISTEN.get());

			Object valorParametroActual = evento.getFormulario().getParametro(columnName);
			campo.getValidaciones()
			     .map(validacion -> validarParametro(validacion, valorParametroActual))
			     .filter(resultadoValidacion -> !resultadoValidacion.equals(valorParametroActual))
			     .peek(resultadoValidacion -> evento.getFormulario().setParametro(columnName, resultadoValidacion))
			     .findFirst()
				 .ifPresent(resultadoValidacion -> evento.getFormulario().setValidacionExitosa(false));

			if (evento.getFormulario().isValidacionExitosa()) {
				campo.getTransformaciones()
					 .forEach(transformacion -> evento.getFormulario().setParametro(columnName, transformarParametro(transformacion, valorParametroActual)));
			}

	    }
	    
	}

	
	// =========================================================================== //
    // ============================== TRANSACCIONES ============================== //
    // =========================================================================== //
	
	private class EjecucionTransacciones implements Callable<Void> {

		private final Transaccion transaccion;

	    private EjecucionTransacciones(Transaccion transaccion) {
	        this.transaccion = transaccion;
	    }

	    @Override
	    public Void call() {
	    	try {
	    		ejecutarTransaccion();
				return null;
			} catch (Exception e) {
				Throwable excepcionReal = e.getCause();
				if (excepcionReal != null) {
					throw new RuntimeException(excepcionReal);
				} else {
					throw e;
				}
			}
	    }

        /**
		 *
		 */
	    private void ejecutarTransaccion() {

	        // Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
	        // y guardarlos en una lista para enviarlos a la sentencia preparada
			transaccion.setParametrosTransaccion(
				transaccion.getFiltrosSentencia().stream().parallel()
					.map(filtro -> evento.getFormulario().getCampos().get()
							.filter(campo -> campo.getKey().equals(filtro))
							.map(campo -> isNotBlank(campo.getValue().getValorPorDefecto()) ? campo.getValue().getValorPorDefecto()
																							: evento.getFormulario().getParametro(campo.getKey()))
							.findAny().get()
					).toArray());

			// Guardar los resultados de esta transacción dentro del resultado final de todos el evento
			JsonNode resultadoTransaccion = Eventos.ejecutarTransaccion(transaccion);
			if (evento.getResultadoFinal().size() == 0) {
				evento.setResultadoFinal(resultadoTransaccion);
			} else if (resultadoTransaccion.size() > 0) {
				poblarResultadoFinal(resultadoTransaccion);
			}

	    }
	    
	}
	
	private void poblarResultadoFinal(JsonNode coleccion) {
		coleccion.fieldNames().forEachRemaining(
				fieldName -> {Optional.ofNullable(evento.getResultadoFinal().get(fieldName))
									  .ifPresent(objetoActual -> poblarResultadoFinal(objetoActual));
							  evento.getResultadoFinal().setAll((ObjectNode) coleccion);}
		);
	}
	
	
	
	
	
	
	/**
     * En este método se genera dinámicamente la sentencia CQL (SELECT, INSERT, UPDATE o DELETE) 
     * que pertenece a una nueva transacción.
     * 
     * @param sitioWeb sitio web a donde pertenece la página
     * @param pagina página a donde pertenece la transacción
     * @param transaccion nombre de la transacción a la que pertenece la nueva sentencia CQL
     * @param tipoSentencia si es (SELECT, INSERT, UPDATE o DELETE)
     * @param tabla nombre de la tabla de la base de datos donde se ejecutará la sentencia CQL
     * @return sentencia CQL preparada. Ej: SELECT campo1 FROM base_de_datos.tabla WHERE campo2 = ?
     * @throws java.lang.Exception 
     */
//    private String generarSentenciasCQL(String sitioWeb, String pagina, String transaccion, String tipoSentencia, String tabla) throws Exception {
//
//        try {
//            
//            // Obtener los campos que contiene la sentencia CQL.
//            StringBuilder sentenciaCQL = new StringBuilder("SELECT clausula, campo FROM diewebsiten.sentencias_cql WHERE sitioweb = ? AND pagina = ? AND tipotransaccion = ? AND transaccion = ?");
//            List<Row> camposSentencia = consultar(sentenciaCQL.toString(), "cualquierNombreDeSentencia", new Object[] {sitioWeb, pagina, tipoSentencia, transaccion});
//
//            // Validar que los campos del formulario existen.
//            if (camposSentencia.isEmpty()) {
//                sentenciaCQL = new StringBuilder("No se puede generar la sentencia CQL de la transacción '" + transaccion 
//                             + "' de la página '" + pagina + "' del sitio web '" + sitioWeb + "' " 
//                             + "debido a que no se encontraron los campos que componen la sentencia CQL.");
//                throw new Exception(sentenciaCQL.toString());
//            }
//
//            // Que la sentencia CQL sea de tipo válido.
//            if (!contienePalabra(tipoSentencia, "SELECT,UPDATE,INSERT,DELETE")) {
//                sentenciaCQL = new StringBuilder("La transacción '" + transaccion + "' de la página '" + pagina + "' del sitio web '" + sitioWeb + "' " 
//                             + "tiene un tipo de transacción no válido: '" + tipoSentencia + "'. "
//                             + "Los tipos de transacción válidos son: SELECT, UPDATE, INSERT o DELETE.");
//                throw new Exception(sentenciaCQL.toString());
//            }
//            
//            Map <String, String> clausulas = new LinkedHashMap<String, String>();
//            
//            for (Row campo : camposSentencia) {  
//                
//                // Guardar cada cláusula con su formato CQL y con sus campos correspondientes en cada Key del Map.
//                // Ej: {"SELECT" : "campo1, campo2"}, {"WHERE" : "campo1 = ? AND campo2 = ?"}, 
//                //     {"SET" : "campo1 = ?, campo2 = ?"}, {"INSERT" : "campo1, campo2"},
//                //     {"VALUES" : "?,?,?"}
//                if (clausulas.containsKey(campo.getString("clausula"))) {
//                    
//                    // {"SELECT" : "campo1, campo2"}
//                    if (campo.getString("clausula").equals("SELECT")) {
//                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + ", " + campo.getString("campo"));
//                    // {"INSERT" : "campo1, campo2"} | {"VALUES" : "?,?"}  
//                    } else if (campo.getString("clausula").equals("INSERT")) {
//                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + " AND " + campo.getString("campo") + " = ?");
//                        clausulas.put("VALUES", clausulas.get("VALUES") + ", ?");
//                    // {"SET" : "campo1 = ?, campo2 = ?"}  
//                    } else if (campo.getString("clausula").equals("SET")) {
//                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + ", " + campo.getString("campo") + " = ?");
//                    // {"WHERE" : "campo1 = ? AND campo2 = ?"}  
//                    } else if (campo.getString("clausula").equals("WHERE")) {
//                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + " AND " + campo.getString("campo") + " = ?");                        
//                    }
//                    
//                } else {
//                    
//                    //{"SELECT" : "campo1"}
//                    if (campo.getString("clausula").equals("SELECT")) {
//                        clausulas.put(campo.getString("clausula"), campo.getString("campo"));
//                    // {"INSERT" : "campo1"} | {"VALUES" : "?"}    
//                    } else if (campo.getString("clausula").equals("INSERT")) {
//                        clausulas.put(campo.getString("clausula"), clausulas.get("clausula") + " AND " + campo.getString("campo") + " = ?");
//                        clausulas.put("VALUES", "?");
//                    // {"SET" : "campo1 = ?"} | {"WHERE" : "campo1 = ?"}
//                    } else if (contienePalabra(campo.getString("clausula"), "SET,WHERE")) {
//                        clausulas.put(campo.getString("clausula"), campo.getString("campo") + " = ?");                        
//                    }
//                }              
//            }            
//            
//            sentenciaCQL = new StringBuilder(tipoSentencia + " ");
//            
//            // Generar la sentencia SELECT, INSERT, UPDATE o DELETE.
//            if (sentenciaCQL.toString().trim().equals("SELECT")) {                
//                sentenciaCQL.append(clausulas.get("SELECT") + " FROM " + tabla + " WHERE " + clausulas.get("WHERE"));
//            } else if (sentenciaCQL.toString().trim().equals("INSERT")) {                
//                sentenciaCQL.append("INTO " + tabla + "(" + clausulas.get("INSERT") + ") VALUES (" + clausulas.get("VALUES") + ")");                
//            } else if (sentenciaCQL.toString().trim().equals("UPDATE")) {                
//                sentenciaCQL.append(tabla + " SET " + clausulas.get("SET") + " WHERE " + clausulas.get("WHERE"));                
//            } else if (sentenciaCQL.toString().trim().equals("DELETE")) {
//                sentenciaCQL.append("FROM " + tabla + " WHERE " + clausulas.get("WHERE"));  
//            } 
//            
//            return sentenciaCQL.append(";").toString();
//               
//        } catch (Exception e) {
//            Log.getInstance().imprimirErrorEnLog(e);
//            throw new Exception(e);
//        }
//        
//    }// generarSentenciasCQL
	
	
	
}
