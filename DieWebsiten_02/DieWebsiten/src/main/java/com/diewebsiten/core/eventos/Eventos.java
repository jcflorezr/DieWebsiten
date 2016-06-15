
package com.diewebsiten.core.eventos;

import static com.diewebsiten.core.eventos.util.ProcesamientoParametros.*;
import static com.diewebsiten.core.util.Validaciones.contienePalabra;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.diewebsiten.core.eventos.dto.Campo;
import com.diewebsiten.core.eventos.dto.Evento;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.eventos.dto.Validacion;
import com.diewebsiten.core.eventos.util.LogEventos;
import com.diewebsiten.core.eventos.util.Constantes;
import com.diewebsiten.core.eventos.util.Mensajes;
import com.diewebsiten.core.excepciones.ExcepcionDeLog;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;


/**
 * Este clase recibe todas las solicitudes de tipo consulta, inserción, modificación
 * y eliminación en la base de datos; luego retorna las respuestas que devuelve la capa de datos.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
public class Eventos implements Callable<JsonObject> {
    
    private Evento evento;
    private static final String VALIDACIONES = "validaciones";
    private static final String TRANSACCIONES = "transacciones";
    
    Eventos(String url, String nombreEvento, String parametros) throws Exception {
        evento = new Evento(url, nombreEvento, parametros);
    }
    
    
    @Override
    public JsonObject call() throws ExcepcionDeLog {
    	
		try {
			if (validarFormulario()) {
				ejecutarEvento();
				return evento.getResultadoFinal();
			} else {
				evento.getResultadoFinal().add("VAL_" + evento.getNombreEvento(), evento.getFormulario().getParametros());
				return evento.getResultadoFinal();
			}
		} catch (Exception e) {
			Throwable excepcionReal = e.getCause();
			if (excepcionReal != null) {
				new LogEventos(evento).imprimirErrorEnLog(excepcionReal);
			} else {
				new LogEventos(evento).imprimirErrorEnLog(e);
			}
			evento.getResultadoFinal().addProperty("error", Mensajes.ERROR.get());
			return evento.getResultadoFinal();
		}
        
    }
    
    /**
     *
     * @return
     * @throws Exception
     */
    private boolean validarFormulario() throws Exception {
        	
        evento.getFormulario().setCampos(ejecutarTransaccion(Constantes.SNT_VALIDACIONES_EVENTO.get(), Constantes.NMBR_SNT_VALIDACIONES_EVENTO.get(), evento.getInformacionEvento()));

        if (!evento.getFormulario().poseeCampos() && evento.getFormulario().poseeParametros()) {
            throw new ExcepcionGenerica(Mensajes.Evento.Formulario.CAMPOS_FORMULARIO_NO_EXISTEN.get());
        } else if (evento.getFormulario().poseeCampos() && !evento.getFormulario().poseeParametros()) {
            throw new ExcepcionGenerica(Mensajes.Evento.Formulario.PARAMETROS_FORMULARIO_NO_EXISTEN.get());
        } else if (!evento.getFormulario().poseeCampos()) {
        	return true; // Si no se encontraron campos para la ejecución de este evento significa que no los necesita.
        }

        ejecucionParalela(VALIDACIONES);

        if (!evento.getFormulario().isValidacionExitosa()) {
            return evento.getFormulario().isValidacionExitosa();
        } 
        
        // Cambiar los parámetros originales por los que ya fueron transformados durante el proceso de validación.
        evento.getFormulario().setParametros(evento.getFormulario().getParametrosTransformados());

        return true;
        
    }
    
    /**
     *
     * @return
     */
    private void ejecutarEvento() throws Exception {

        // Obtener la información de las transacciones que se ejecutarán en el evento actual.
    	evento.setTransacciones(ejecutarTransaccion(Constantes.SNT_TRANSACCIONES.get(), Constantes.NMBR_SNT_TRANSACCIONES.get(), evento.getInformacionEvento()));

        // Validar que el evento existe.
        if (!evento.poseeTransacciones()) { 
            throw new ExcepcionGenerica (Mensajes.Evento.EVENTO_NO_EXISTE.get());
        }
        
        ejecucionParalela(TRANSACCIONES); 

    }
    
    /**
     * 
     * @param moduloAEjecutar
     * @throws Exception
     */
    private void ejecucionParalela(String moduloAEjecutar) throws Exception {
    	
    	final ThreadFactory threadFactoryBuilder = new ThreadFactoryBuilder().setNameFormat(evento.getNombreEvento() + "-%d").setDaemon(true).build();
        ExecutorService ejecucionParalela = Executors.newFixedThreadPool(10, threadFactoryBuilder);
        
        try {
            
            List<Future<Void>> hilosEjecucion = new ArrayList<>();
            
            if (VALIDACIONES.equals(moduloAEjecutar)) {
            	for (Campo campoFormularioEvento : evento.getFormulario().getCampos()) {
            		hilosEjecucion.add(ejecucionParalela.submit(new Formularios(campoFormularioEvento)));
            	}
            } else if (TRANSACCIONES.equals(moduloAEjecutar)) {
            	for (Transaccion transaccion : evento.getTransacciones()) {
            		hilosEjecucion.add(ejecucionParalela.submit(new Transacciones(transaccion)));                
            	}            	
            }
            
            for (Future<Void> hiloActual : hilosEjecucion) {
                hiloActual.get();		
            }
            
        } finally {
            ejecucionParalela.shutdown();
        }
        
    }
    
    /*
     * Para consultas con resultado plano
     */
    static List<JsonObject> ejecutarTransaccion(String sentencia) throws Exception { 
    	return FachadaEventos.ejecutarTransaccion(sentencia, null, null); 
    }
    static List<JsonObject> ejecutarTransaccion(String sentencia, String nombreSentencia, Object[] parametros) throws Exception { 
    	return FachadaEventos.ejecutarTransaccion(sentencia, nombreSentencia, parametros); 
    }
    
    /*
     * Para consultas con resultado en jerarquía
     */
    static void ejecutarTransaccion(Transaccion transaccion, JsonObject resultadoConsulta) throws Exception {
    	FachadaEventos.ejecutarTransaccionConJerarquia(transaccion, resultadoConsulta); 
    }
    
    
    // =========================================================================== //
    // =========================== FORMULARIOS =================================== //
    // =========================================================================== //
	
	private class Formularios implements Callable<Void> {
	    
	    private final Campo campo;
	    private static final String VALIDACION = "Validación";
	    private static final String TRANSFORMACION = "Transformación";
	    
	    private Formularios(Campo campo) {            
	        this.campo = campo;
	    } 
	    
	    @Override
	    public Void call() throws Exception {
	    	try {			
	    		return procesarFormulario();
			} catch (Exception e) {
				Throwable excepcionReal = e.getCause();
				if (excepcionReal != null) {
					throw (Exception) excepcionReal;
				} else {
					throw e;
				}
			}
	    }
	    
	    /**
	     * Recibir los valores de los parámetros de un formulario, luego obtener de
	     * la base de datos la validación de cada parámetro y por último validar cada parámetro.
	     *
	     * @param camposFormulario
	     * @param validacionesCampos
	     * @param parametros
	     * @throws com.diewebsiten.core.excepciones.ExcepcionGenerica
	     */
	    private Void procesarFormulario() throws Exception {
	    	
	    	String nombreCampo = campo.getColumnName();
	        String grupoValidacionCampo = campo.getGrupoValidacion();
	        
	        StringBuilder sentencia = new StringBuilder("SELECT grupo_validacion, tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupo_validacion = '").append(grupoValidacionCampo).append("'");
	        campo.setValidaciones(Eventos.ejecutarTransaccion(sentencia.toString()));
	
	        // Validar que sí existan las validaciones del grupo.
	        if (!campo.poseeValidaciones()) {
				throw new ExcepcionGenerica(Mensajes.Evento.Formulario.VALIDACIONES_NO_EXISTEN.get());
			}
	        
	        for (Validacion validacion : campo.getValidaciones()) {
	        	
	            Object valorParametroActual = evento.getFormulario().getParametro(nombreCampo);
	            
	            if (VALIDACION.equals(validacion.getTipo())) {
	            	String resultadoValidacion = validarParametro(validacion.getValidacion(), valorParametroActual);
	            	if (valorParametroActual != null && resultadoValidacion != null && !valorParametroActual.equals(resultadoValidacion)) {
	            		if (evento.getFormulario().isValidacionExitosa()) {
	            			evento.getFormulario().setValidacionExitosa(false);
	            		}
	            		evento.getFormulario().setParametros(nombreCampo, resultadoValidacion);	
	            	}
	            } else if (evento.getFormulario().isValidacionExitosa() && TRANSFORMACION.equals(validacion.getTipo())) {
	                Object resTrans = transformarParametro(validacion.getValidacion(), valorParametroActual);
	                if (null == resTrans) {
	                    throw new ExcepcionGenerica(Mensajes.Evento.Formulario.TRANSFORMACION_FALLIDA.get(nombreCampo, validacion.getValidacion()));
	                }
	                evento.getFormulario().setParametrosTransformados(nombreCampo, resTrans);
	            }
	            
	        }
	        
	        // Es necesario retornar null debido a que este método es de tipo Void en vez de void.
	        // El resultado de la validacion ya se está guardando en el objeto new Evento().new Formulario().validacionExitosa
	        return null;
	    	
	    }
	    
	}
	
	
	// =========================================================================== //
    // ============================== TRANSACCIONES ============================== //
    // =========================================================================== //
	
	private class Transacciones implements Callable<Void> {
	    
	    private final Transaccion transaccion;
	    private static final String TRANSACCIONES_SOPORTADAS = "SELECT,UPDATE,INSERT,DELETE";
	    
	    private Transacciones (Transaccion transaccion) {
	        this.transaccion = transaccion;
	    }
	
	    /** 
	     * @see java.util.concurrent.Callable#call()
	     */
	    @Override
	    public Void call() throws Exception {
	    	try {
	    		return ejecutarTransaccion();			
			} catch (Exception e) {
				Throwable excepcionReal = e.getCause();
				if (excepcionReal != null) {
					throw (Exception) excepcionReal;
				} else {
					throw e;
				}
			}
	    }
	    
	    /**
	     * 
	     * @return
	     * @throws Exception
	     */
	    private Void ejecutarTransaccion() throws Exception {
	      
	        // Validar que la sentencia CQL sea de tipo válido.
	        if (!contienePalabra(transaccion.getTipo(), TRANSACCIONES_SOPORTADAS)) {
				throw new ExcepcionGenerica(Mensajes.Evento.Transaccion.SENTENCIACQL_NO_SOPORTADA.get(transaccion.getNombreTransaccion(), transaccion.getTipo()));
			}
	        
	        // Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
	        // y guardarlos en una lista para enviarlos a la sentencia preparada
	        List<Object> valoresSentencia = new ArrayList<>();
	        
	        
	        
	        
	        
	        // ESTE CICLO ESTA COMPARANDO SI LA COLUMNA ACTUAL CONCUERDA CON EL FILTRO ACTUAL DE LA CONSULTA CQL
	        // EL FLAG existenFiltros = true NO ESTA BIEN CREADO
	        // ESTE CICLO ESTA FUNCIONANDO PORQUE ACTUALMENTE SOLO SE ESTA USANDO A CASSANDRA COMO ALMACENAMIENTO
	        // PERO CUANDO SE IMPLEMENTEN MAS MOTORES DE BASE DE DATOS HABRA QUE CAMBIARLO 
	        
	        for (String columnaFiltro : transaccion.getColumnasFiltroSentenciaCql()) {
	        	
	        	boolean existenFiltros = false;
	            
	        	for (Campo campo : evento.getFormulario().getCampos()) {
	            	
	            	if (columnaFiltro.equals(campo.getColumnName())) {
	                    if (isNotBlank(campo.getValorPorDefecto())) {
	                        valoresSentencia.add(campo.getValorPorDefecto());
	                    } else {
	                        valoresSentencia.add(evento.getFormulario().getParametro(campo.getColumnName()));
	                    }
	                    existenFiltros = true;
	            	}
	            	
	            }
	            
	            // Validar que existan los filtros necesarios para la sentencia CQL que ejecuta la transacción.
	            if (!existenFiltros) {
					throw new ExcepcionGenerica(Mensajes.Evento.Transaccion.FILTRO_NO_EXISTE.get(columnaFiltro, transaccion.getTipo(), transaccion.getNombreTransaccion()));
				}
	        }
	        
	        // Guardar los detalles de la sentencia de Base de Datos que contiene la transacción
	        transaccion.setDetallesSentencia(transaccion.getSentenciaCql(), transaccion.getNombreTransaccion(), valoresSentencia.toArray());
	        
	        // Guardar los resultados de esta transacción dentro del resultado final de todo el evento
	        Eventos.ejecutarTransaccion(transaccion, evento.getResultadoFinal());                  
	             
	        // Es necesario retornar null debido a que este método es de tipo Void en vez de void. Esto es debido a que 
	        // este metodo se ejecuta por varios hilos al mismo tiempo
	        return null;
	    }
	    
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
