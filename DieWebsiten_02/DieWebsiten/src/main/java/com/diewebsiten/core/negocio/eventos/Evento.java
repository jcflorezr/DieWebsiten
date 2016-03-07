
package com.diewebsiten.core.negocio.eventos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.ProveedorCassandra;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.Log;

import static com.diewebsiten.core.util.UtilidadValidaciones.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


/**
 * Este clase recibe todas las solicitudes de tipo consulta, inserción, modificación
 * y eliminación en la base de datos; luego retorna las respuestas que devuelve la capa de datos.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
public class Evento implements Callable<String> {
    
    private String sitioWeb;
    private String pagina;
    private String idioma;
    private String nombreEvento;
    private Map<String, Object> parametros;
    private List<Row> camposFormularioEvento;
	
    private ProveedorCassandra proveedorCassandra;
    private boolean validacionExitosa;
    
    /**
     * Este constructor se encarga de recibir un evento, este evento contiene transacciones de consulta,
     * inserción, actualización, eliminación, entre otras; relacionadas con las tablas
     * de la base de datos, Posteriormente ejecuta cada una de estas transacciones y retorna
     * los resultados de cada transacción en un string formato JSON.
     *
     * @param url Dirección del sitio web y de la página que está realizando la petición.
     * @param nombreEvento Nombre del evento que contiene las transacciones a ejecutar.
     * @param parametros Un Map que contiene los parámetros necesarios para ejecutar las
     * transacciones del evento.
     * @return String de tipo JSON con la respuesta de cada transacción que contiene el evento.
     * @throws java.lang.Exception
     */
    public Evento(String url, String nombreEvento, String parametros) throws Exception {
        
        // Validar que el nombre del evento no llegue vacío.
        if (esVacio(nombreEvento)) {
            throw new ExcepcionGenerica(Constantes.NOMBRE_EVENTO_VACIO.getString());
        } else {
        	// Obtener el nombre del evento que se ejecutará.
            this.nombreEvento = nombreEvento;
        }
        
        // Obtener la dirección URL del sitio web que está realizando la petición.
        this.sitioWeb = StringUtils.substringBefore(url, ":@:")/*.equals("localhost") ? "127.0.0.1" : sitioWeb*/;
        
        // Si el parámetro "parametros" viene vacío se convierte a formato JSONObject vacío.
        parametros = esVacio(parametros) ? "{}" : parametros;
            
        // Guardar los parámetros con los que se ejcutará el evento.
        this.parametros = new Gson().fromJson(parametros, new TypeToken<Map<String, Object>>(){}.getType());
        
        // Obtener el nombre de la página del sitio web que está realizando la petición.
        this.pagina = !esVacio(StringUtils.substringAfter(url, ":@:")) ? StringUtils.substringAfter(url, ":@:") : "";
        
        // Obtener el código del idioma en el que se desplegará la página, si no existe el código
        // se desplegará por defecto en idioma español (ES).        
        this.idioma = getParametros().containsKey("lang") ? getParametros().get("lang").toString() : "ES";
        
        this.validacionExitosa = true;
        
        this.proveedorCassandra = ProveedorCassandra.getInstance();
        
    }
    
    
    
    @Override
    public String call() {
        try {
           if (validarFormularioEvento())
                return ejecutarEvento().toString();
           else
                return "{\"VAL_" + getNombreEvento() + "\" : " + new Gson().toJson(getParametros()) + "}";
       } catch (Exception e) {
            Log.getInstance(this).imprimirErrorEnLog(e);
            return Constantes.ERROR.getString();
        }
        
    }
    
    /**
     *
     * @return
     * @throws Exception
     */
    private boolean validarFormularioEvento() throws Exception {
        
        ExecutorService ejecucionParalelaValidaciones = Executors.newFixedThreadPool(10);

        try {
            
            List<Row> camposFormularioEvento = getProveedorCassandra().consultar(Constantes.NMBR_SNT_VALIDACIONES_EVENTO.getString(), getSitioWeb(), getPagina(), getNombreEvento());

            // Si no se encontraron campos para la ejecución de este evento significa que no los necesita.
            if (camposFormularioEvento.isEmpty()) {
                return true;
            }

            // Validar si el evento posee campos y validar que existan los parámetros que necesitan dichos campos para su ejecución.
            if (!camposFormularioEvento.isEmpty() && getParametros().isEmpty())
                throw new ExcepcionGenerica(Constantes.Mensajes.CAMPOS_FORMULARIO_NO_EXISTEN.getMensaje(getNombreEvento()));

            List<Future<Boolean>> grupoEjecucionValidaciones = new ArrayList<Future<Boolean>>();

            for (Row campoFormularioEvento : camposFormularioEvento) {
                grupoEjecucionValidaciones.add(ejecucionParalelaValidaciones.submit(new Validaciones(campoFormularioEvento, this)));
            }

            for (Future<Boolean> ejecucionValidacionActual : grupoEjecucionValidaciones) {
                if (!ejecucionValidacionActual.get()) {
					setValidacionExitosa(false);
				}
            }

            if (!isValidacionExitosa()) {
                return isValidacionExitosa();
            } 
            
            // Guardar los campos del formulario del evento en una variable global
            setCamposFormularioEvento(camposFormularioEvento);

            return isValidacionExitosa();

        } finally {
            ejecucionParalelaValidaciones.shutdown();
        }
        
    }
    
    /**
     *
     * @return
     */
    private String ejecutarEvento() throws Exception {
        
    	JsonObject resultadoEvento = new JsonObject();
        ExecutorService ejecucionParalelaTransacciones = Executors.newFixedThreadPool(10);
        List<Row> transacciones;
        
        try {

            // Obtener la información de las transacciones que se ejecutarán en el evento actual.
            transacciones = getProveedorCassandra().consultar(Constantes.NMBR_SNT_TRANSACCIONES.getString(), getSitioWeb(), getPagina(), getNombreEvento());

            // Validar que el evento existe.
            if (transacciones.isEmpty()) 
                throw new ExcepcionGenerica (Constantes.Mensajes.EVENTO_NO_EXISTE.getMensaje(getSitioWeb(), getPagina(), getNombreEvento()));
            
            List<Future<Void>> grupoEjecucionTransacciones = new ArrayList<Future<Void>>();
            
            for (Row transaccion : transacciones) {                
                grupoEjecucionTransacciones.add(ejecucionParalelaTransacciones.submit(new Transacciones(transaccion, resultadoEvento, this)));                
            }
            
            for (Future<Void> ejecucionTransaccionActual : grupoEjecucionTransacciones) {
                ejecucionTransaccionActual.get();		
            }
            
        } finally {
            ejecucionParalelaTransacciones.shutdown();
        } 
        
        return resultadoEvento.toString();

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
    private String generarSentenciasCQL(String sitioWeb, String pagina, String transaccion, String tipoSentencia, String tabla) throws Exception {

        try {
            
            // Obtener los campos que contiene la sentencia CQL.
            StringBuilder sentenciaCQL = new StringBuilder("SELECT clausula, campo FROM diewebsiten.sentencias_cql WHERE sitioweb = ? AND pagina = ? AND tipotransaccion = ? AND transaccion = ?");
            List<Row> camposSentencia = getProveedorCassandra().consultar(sentenciaCQL.toString(), sitioWeb, pagina, tipoSentencia, transaccion);

            // Validar que los campos del formulario existen.
            if (camposSentencia.isEmpty()) {
                sentenciaCQL = new StringBuilder("No se puede generar la sentencia CQL de la transacción '" + transaccion 
                             + "' de la página '" + pagina + "' del sitio web '" + sitioWeb + "' " 
                             + "debido a que no se encontraron los campos que componen la sentencia CQL.");
                throw new Exception(sentenciaCQL.toString());
            }

            // Que la sentencia CQL sea de tipo válido.
            if (!contienePalabra(tipoSentencia, "SELECT,UPDATE,INSERT,DELETE")) {
                sentenciaCQL = new StringBuilder("La transacción '" + transaccion + "' de la página '" + pagina + "' del sitio web '" + sitioWeb + "' " 
                             + "tiene un tipo de transacción no válido: '" + tipoSentencia + "'. "
                             + "Los tipos de transacción válidos son: SELECT, UPDATE, INSERT o DELETE.");
                throw new Exception(sentenciaCQL.toString());
            }
            
            Map <String, String> clausulas = new LinkedHashMap<String, String>();
            
            for (Row campo : camposSentencia) {  
                
                // Guardar cada cláusula con su formato CQL y con sus campos correspondientes en cada Key del Map.
                // Ej: {"SELECT" : "campo1, campo2"}, {"WHERE" : "campo1 = ? AND campo2 = ?"}, 
                //     {"SET" : "campo1 = ?, campo2 = ?"}, {"INSERT" : "campo1, campo2"},
                //     {"VALUES" : "?,?,?"}
                if (clausulas.containsKey(campo.getString("clausula"))) {
                    
                    // {"SELECT" : "campo1, campo2"}
                    if (campo.getString("clausula").equals("SELECT")) {
                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + ", " + campo.getString("campo"));
                    // {"INSERT" : "campo1, campo2"} | {"VALUES" : "?,?"}  
                    } else if (campo.getString("clausula").equals("INSERT")) {
                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + " AND " + campo.getString("campo") + " = ?");
                        clausulas.put("VALUES", clausulas.get("VALUES") + ", ?");
                    // {"SET" : "campo1 = ?, campo2 = ?"}  
                    } else if (campo.getString("clausula").equals("SET")) {
                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + ", " + campo.getString("campo") + " = ?");
                    // {"WHERE" : "campo1 = ? AND campo2 = ?"}  
                    } else if (campo.getString("clausula").equals("WHERE")) {
                        clausulas.put(campo.getString("clausula"), clausulas.get(campo.getString("clausula")) + " AND " + campo.getString("campo") + " = ?");                        
                    }
                    
                } else {
                    
                    //{"SELECT" : "campo1"}
                    if (campo.getString("clausula").equals("SELECT")) {
                        clausulas.put(campo.getString("clausula"), campo.getString("campo"));
                    // {"INSERT" : "campo1"} | {"VALUES" : "?"}    
                    } else if (campo.getString("clausula").equals("INSERT")) {
                        clausulas.put(campo.getString("clausula"), clausulas.get("clausula") + " AND " + campo.getString("campo") + " = ?");
                        clausulas.put("VALUES", "?");
                    // {"SET" : "campo1 = ?"} | {"WHERE" : "campo1 = ?"}
                    } else if (contienePalabra(campo.getString("clausula"), "SET,WHERE")) {
                        clausulas.put(campo.getString("clausula"), campo.getString("campo") + " = ?");                        
                    }
                }              
            }            
            
            sentenciaCQL = new StringBuilder(tipoSentencia + " ");
            
            // Generar la sentencia SELECT, INSERT, UPDATE o DELETE.
            if (sentenciaCQL.toString().trim().equals("SELECT")) {                
                sentenciaCQL.append(clausulas.get("SELECT") + " FROM " + tabla + " WHERE " + clausulas.get("WHERE"));
            } else if (sentenciaCQL.toString().trim().equals("INSERT")) {                
                sentenciaCQL.append("INTO " + tabla + "(" + clausulas.get("INSERT") + ") VALUES (" + clausulas.get("VALUES") + ")");                
            } else if (sentenciaCQL.toString().trim().equals("UPDATE")) {                
                sentenciaCQL.append(tabla + " SET " + clausulas.get("SET") + " WHERE " + clausulas.get("WHERE"));                
            } else if (sentenciaCQL.toString().trim().equals("DELETE")) {
                sentenciaCQL.append("FROM " + tabla + " WHERE " + clausulas.get("WHERE"));  
            } 
            
            return sentenciaCQL.append(";").toString();
               
        } catch (Exception e) {
            Log.getInstance().imprimirErrorEnLog(e);
            throw new Exception(e);
        }
        
    }// generarSentenciasCQL
    
    
    
    // =============================
    // ==== Getters and Setters ====
    // =============================
    
    
    String getSitioWeb() {
        return sitioWeb;
    }

    String getPagina() {
        return pagina;
    }

    String getIdioma() {
        return idioma;
    }

    String getNombreEvento() {
        return nombreEvento;
    }

    Map<String, Object> getParametros() {
        return parametros;
    }

//    private void setParametros(Map<String, Object> parametros) {
//        this.parametros = parametros;
//    }
    
    void setParametros(String nombreParametro, Object valorParametro) {
        this.parametros.put(nombreParametro, valorParametro);
    }
    
    private void setValidacionExitosa(boolean validacionExitosa) {
        this.validacionExitosa = validacionExitosa;
    }

    private boolean isValidacionExitosa() {
        return validacionExitosa;
    }
    
    List<Row> getCamposFormularioEvento() {
		return camposFormularioEvento;
	}

	void setCamposFormularioEvento(List<Row> camposFormularioEvento) {
		this.camposFormularioEvento = camposFormularioEvento;
	}
	
	ProveedorCassandra getProveedorCassandra() {
		return this.proveedorCassandra;
	}
}
