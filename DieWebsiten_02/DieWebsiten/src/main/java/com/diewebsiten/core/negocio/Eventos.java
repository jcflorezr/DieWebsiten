/**
 * ANTES DE SUBIR A PRODUCCION
 * 
 * 1. convertir variable "batch" a StringBuilder
 */


package com.diewebsiten.core.negocio;


import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.Log;
import com.diewebsiten.core.util.Utilidades;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Este clase recibe todas las solicitudes de tipo consulta, inserción, modificación
 * y eliminación en la base de datos; luego retorna las respuestas que devuelve la capa de datos.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
public class Eventos implements Callable<String> {
    
    private String sitioWeb;
    private String pagina;
    private String idioma;
    private String nombreEvento;
    private Map<String, Object> parametros;
    private static Map<String, PreparedStatement> sentenciasPreparadas;
    private static Session sesionBD;
    private boolean validacionExitosa;
    private final Utilidades util;
    
    /**
     * Este constructor se encarga de recibir un evento, éste evento contiene transacciones de consulta,
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
    public Eventos(String url, String nombreEvento, String parametros) throws Exception {

        util = new Utilidades();
        
        // Validar que el nombre del evento no llegue vacío.
        if (getUtil().esVacio(nombreEvento)) 
            throw new ExcepcionGenerica(Constantes.NOMBRE_EVENTO_VACIO.getString());
        
        // Obtener el nombre del evento que se ejecutará.
        this.nombreEvento = nombreEvento + " ";
        
        // Obtener la dirección URL del sitio web que está realizando la petición.
        this.sitioWeb = StringUtils.substringBefore(url, ":@:")/*.equals("localhost") ? "127.0.0.1" : sitioWeb*/;
        
        // Si el parámetro "parametros" viene vacío se convierte a formato JSONObject vacío.
        parametros = getUtil().esVacio(parametros) ? "{}" : parametros;
            
        // Guardar los parámetros con los que se ejcutará el evento.
        this.parametros = new Gson().fromJson(parametros, new TypeToken<Map<String, Object>>(){}.getType());
        
        // Obtener el nombre de la página del sitio web que está realizando la petición.
        this.pagina = !getUtil().esVacio(StringUtils.substringAfter(url, ":@:")) ? StringUtils.substringAfter(url, ":@:") : "";
        
        // Obtener el código del idioma en el que se desplegará la página, si no existe el código
        // se desplegará por defecto en idioma español (ES).        
        this.idioma = getParametros().containsKey("lang") ? getParametros().get("lang").toString() : "ES";
        
        this.validacionExitosa = true;
        
    }
    
    public String getSitioWeb() {
        return sitioWeb;
    }

    public String getPagina() {
        return pagina;
    }

    public String getIdioma() {
        return idioma;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    private Map<String, Object> getParametros() {
        return parametros;
    }

    public void setParametros(Map<String, Object> parametros) {
        this.parametros = parametros;
    }
    
    public void setParametros(String nombreParametro, Object valorParametro) {
        this.parametros.put(nombreParametro, valorParametro);
    }
    
    public static void setSentenciasPreparadas() {
        synchronized (Eventos.class) {
            if (null == sentenciasPreparadas) {
                sentenciasPreparadas = new HashMap<String, PreparedStatement>();
                sentenciasPreparadas.put("SentenciaTransacciones", getSesionBD().prepare(Constantes.SENTENCIA_TRANSACCIONES.getString()));
                //sentenciasPreparadas.put("SentenciaFormularios", getSesionBD().prepare(Constantes.SENTENCIA_FORMULARIOS.getString()));
                //sentenciasPreparadas.put("SentenciaSentenciasCql", getSesionBD().prepare(Constantes.SENTENCIA_SENTENCIAS_CQL.getString()));
                sentenciasPreparadas.put("SentenciaValidacionesEvento", getSesionBD().prepare(Constantes.SENTENCIA_VALIDACIONES_EVENTO.getString()));
            }
        }
    }

    public Map<String, PreparedStatement> getSentenciasPreparadas() {
        return sentenciasPreparadas;
    }

    public static void setSesionBD() throws Exception {
        sesionBD = Fabrica.conectar();
    }

    public static Session getSesionBD() {
        return sesionBD;
    }

    private Utilidades getUtil() {
        return util;
    }
    
    private void setValidacionExitosa(boolean validacionExitosa) {
        this.validacionExitosa = validacionExitosa;
    }

    private boolean isValidacionExitosa() {
        return validacionExitosa;
    }
    
    @Override
    public String call() {
        try {
           if (validarFormularioEvento())
                return ejecutarEvento();
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

            PreparedStatement sentenciaValidacionesEvento = getSentenciasPreparadas().get("SentenciaValidacionesEvento");
            
            List<Row> camposFormularioEvento = getSesionBD().execute(sentenciaValidacionesEvento.bind(getSitioWeb(), getPagina(), getNombreEvento())).all();

            // Si no se encontraron campos para la ejecución de este evento significa que no los necesita.
            if (camposFormularioEvento.isEmpty())
                return true;

            // Validar si el evento posee campos y validar que existan los parámetros que necesitan dichos campos para su ejecución.
            if (!camposFormularioEvento.isEmpty() && getParametros().isEmpty())
                throw new ExcepcionGenerica(Constantes.Mensajes.CAMPOS_FORMULARIO_NO_EXISTEN.getMensaje(getNombreEvento()));

            List<Future<Boolean>> grupoEjecucionValidaciones = new ArrayList<Future<Boolean>>();

            for (Row campoFormularioEvento : camposFormularioEvento) {
                grupoEjecucionValidaciones.add(ejecucionParalelaValidaciones.submit(new Validaciones(campoFormularioEvento, true)));
            }

            for (Future<Boolean> ejecucionValidacionActual : grupoEjecucionValidaciones) {
                if (!ejecucionValidacionActual.get())
                    setValidacionExitosa(false);
            }

            if (!isValidacionExitosa())
                return isValidacionExitosa();

            grupoEjecucionValidaciones = new ArrayList<Future<Boolean>>();

            for (Row campoFormularioEvento : camposFormularioEvento) {
                grupoEjecucionValidaciones.add(ejecucionParalelaValidaciones.submit(new Validaciones(campoFormularioEvento, false)));
            }

            for (Future<Boolean> ejecucionValidacionActual : grupoEjecucionValidaciones) {
                ejecucionValidacionActual.get();
            }

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
        
        JsonObject resultadoEvento;
        ExecutorService ejecucionParalelaTransacciones = Executors.newFixedThreadPool(10);
        List<Row> transacciones;
        
        try {

            // Obtener la información de las transacciones que se ejecutarán en el evento actual.
            transacciones = getSesionBD().execute(getSentenciasPreparadas().get("SentenciaTransacciones").bind(getSitioWeb(), getPagina()/*, getNombreEvento()*/)).all();
//System.out.println(getSesionBD().execute(getSentenciasPreparadas().get("SentenciaTransacciones").bind(getSitioWeb(), getPagina()/*, getNombreEvento()*/)).getExecutionInfo().getQueryTrace());
            // Validar que el evento existe.
            if (transacciones.isEmpty()) 
                throw new ExcepcionGenerica (Constantes.Mensajes.EVENTO_NO_EXISTE.getMensaje(getSitioWeb(), getPagina(), getNombreEvento()));
            
            List<Future<String>> grupoEjecucionTransacciones = new ArrayList<Future<String>>();
            
            for (Row transaccion : transacciones) {                
                grupoEjecucionTransacciones.add(ejecucionParalelaTransacciones.submit(new Transacciones(transaccion)));                
            }
            
            StringBuilder resultadoTransacciones = new StringBuilder();            
            for (Future<String> ejecucionTransaccionActual : grupoEjecucionTransacciones) {
                resultadoTransacciones.append(",").append(ejecucionTransaccionActual.get());
            }
            
            resultadoEvento = (JsonObject) new JsonParser().parse("{" + getNombreEvento() + ": {" + resultadoTransacciones.toString().substring(1) + "}}");
            
        } finally {
            ejecucionParalelaTransacciones.shutdown();
        } 
        
        return resultadoEvento.toString();

    }// ejecutarEvento
    
    
    // ================ CLASE Validaciones ================ //
    private class Validaciones implements Callable<Boolean> {
        
        private final Row campo;
        private final boolean validar;
        
        public Validaciones(Row campo, boolean validar) {            
            this.campo = campo;
            this.validar = validar;
        }
        
        /**
         * Recibir los valores de los campos de un formulario, luego consultar la información
         * de dichos campos en la base de datos para después validarlos con respecto a dicha
         * información consultada.
         *
         * @param camposFormulario
         * @param validacionesCampos
         * @param parametros
         * @return Un Map que contiene los detalles de la validación de cada campo.
         * @throws com.diewebsiten.core.excepciones.ExcepcionGenerica
         */
        @Override
        public Boolean call() throws Exception {                        
            StringBuilder sentencia = new StringBuilder("SELECT grupo, ");
            String nombreCampoActual = campo.getString("campo");
            String grupoValidacionesCampoActual = campo.getString("grupovalidacion");

            if (validar)
                sentencia.append("validaciones FROM diewebsiten.grupos_validaciones WHERE grupo = '").append(grupoValidacionesCampoActual).append("'");
            else
                sentencia.append("transformaciones FROM diewebsiten.grupos_validaciones WHERE grupo = '").append(grupoValidacionesCampoActual).append("'");

            List<Row> grupoValidacion = getSesionBD().execute(sentencia.toString()).all();

            // Validar que existen las validaciones del grupo.
            if (grupoValidacion.isEmpty())
                throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje(getSitioWeb(), getPagina(), getNombreEvento()));            

            Object valorParametroActual = getParametros().get(nombreCampoActual);
            if (validar) {                
                for (String validacion : grupoValidacion.get(0).getSet("validaciones", String.class)) {
                    List<String> resVal = getUtil().validarParametro(validacion, valorParametroActual);
                    if (!resVal.isEmpty()) {
                        setParametros(nombreCampoActual, resVal);
                        return false;
                    }
                }
            } else {                
                if (!grupoValidacion.get(0).isNull("transformaciones")) {
                    for (String transformacion : grupoValidacion.get(0).getSet("transformaciones", String.class)) {
                        Object resTrans = getUtil().transformarParametro(transformacion, valorParametroActual);
                        if (null == resTrans)
                            throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, getNombreEvento(), (String)valorParametroActual, transformacion));
                        setParametros(nombreCampoActual, resTrans);
                    }
                }
            }

            return true;
            
        }      
        
    }// Clase Validaciones


    // ================ CLASE Transacciones =============== //
    private class Transacciones implements Callable<String> {
        
        private final Row transaccion;        
        
        Transacciones (Row transaccion) {
            this.transaccion = transaccion;
        }

        @Override
        public String call() throws Exception {
            
            String nombreTransaccion = transaccion.getString("transaccion");
            String tipoTransaccion = transaccion.getString("tipotransaccion");
            
            PreparedStatement sentenciaSentenciasCql = getSentenciasPreparadas().get("SentenciaSentenciasCql");
            
            String resultadoTransaccion = "";
                
            //Thread.sleep(1000);
            List<Row> camposFormularios_Sentencias;         
            
            // Agregar las supercolumnas al Map de parámetros del formulario, debido a que
            // están presentes en casi todas las transacciones que componen un evento
            // NOTA: solo se agregan estas supercolumnas en caso de que el Map de parámetros
            //       no las contenga.
            if (null == getParametros().get("sitioweb")) setParametros("sitioweb", getSitioWeb());
            if (null == getParametros().get("pagina")) setParametros("pagina", getPagina());
            if (null == getParametros().get("idioma")) setParametros("idioma", getIdioma());
            
            // Obtener los campos que contiene la sentencia CQL.
            camposFormularios_Sentencias = getSesionBD().execute(sentenciaSentenciasCql.bind(getSitioWeb(), getPagina(), tipoTransaccion, nombreTransaccion)).all();
        
            // Validar que las columnas de la sentencia CQL que ejecuta la transacción existen.
            if (camposFormularios_Sentencias.isEmpty())
                throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.CAMPOSCQL_NO_EXISTEN.getMensaje(nombreTransaccion, tipoTransaccion, getNombreEvento(), getPagina(), getSitioWeb()));
        
            // Validar que la sentencia CQL sea de tipo válido.
            if (!getUtil().contienePalabra(tipoTransaccion, "SELECT,UPDATE,INSERT,DELETE"))
                throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.SENTENCIACQL_NO_SOPORTADA.getMensaje(nombreTransaccion, getNombreEvento(), getPagina(), getSitioWeb(), tipoTransaccion));
        
            // Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
            // y guardarlos en una lista para enviarlos a la sentencia preparada
            List<Object> valoresSentencia = new ArrayList<Object>();
            for (Row campo : camposFormularios_Sentencias) {
                // Solo se extraen los valores para las cláusulas SET de los UPDATES, WHERE, y VALUES de los INSERTS.
                if (getUtil().contienePalabra(campo.getString("clausula"), "SET,WHERE,VALUES")) {
                    // Si el campo de la sentencia tiene un valor por defecto se guardará este valor
                    // en vez de guardar el valor que viene en el Map de parámetros.
                    if (!getUtil().esVacio(campo.getString("valorpordefecto")))
                        valoresSentencia.add(campo.getString("valorpordefecto"));
                    else 
                        valoresSentencia.add(getParametros().get(campo.getString("campo")));
                }                        
            }
            
            // Obtener la sentencia CQL de la transacción.
            String sentenciaCQL = transaccion.getString("sentenciacql");
            synchronized (Eventos.class) {
                if (null == getSentenciasPreparadas().get(nombreTransaccion)) {
                    getSentenciasPreparadas().put(nombreTransaccion, getSesionBD().prepare(sentenciaCQL));
                }
            }                       
            
            // Ejecutar la sentencia CQL de la transacción.
            ResultSet rs = getSesionBD().execute(getSentenciasPreparadas().get(nombreTransaccion).bind(valoresSentencia.toArray()));
            
            // Obtener los resultados de la transacción.
            camposFormularios_Sentencias = rs.all();
            
            // Obtener los nombres de las columnas que contiene la transacción.
            List<ColumnDefinitions.Definition> columnas = rs.getColumnDefinitions().asList();
            
            if (tipoTransaccion.equals("SELECT")) {
                
                resultadoTransaccion = nombreTransaccion + ":" + getUtil().transformarResultSet(camposFormularios_Sentencias, columnas, transaccion.getString("tipolista"));
                
            }                    
                   
            return resultadoTransaccion; 
            
        }            
        
    }// Clase Transacciones
    
    
    
    
    
    
    
    
    
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
    public String generarSentenciasCQL(String sitioWeb, String pagina, String transaccion, String tipoSentencia, String tabla) throws Exception {

        try {
            
            Utilidades u = new Utilidades();
            
            // Obtener los campos que contiene la sentencia CQL.
            StringBuilder sentenciaCQL = new StringBuilder("SELECT clausula, campo FROM diewebsiten.sentencias_cql WHERE sitioweb = ? AND pagina = ? AND tipotransaccion = ? AND transaccion = ?");
            List<Row> camposSentencia = getSesionBD().execute(getSesionBD().prepare(sentenciaCQL.toString()).bind(sitioWeb, pagina, tipoSentencia, transaccion)).all();

            // Validar que los campos del formulario existen.
            if (camposSentencia.isEmpty()) {
                sentenciaCQL = new StringBuilder("No se puede generar la sentencia CQL de la transacción '" + transaccion 
                             + "' de la página '" + pagina + "' del sitio web '" + sitioWeb + "' " 
                             + "debido a que no se encontraron los campos que componen la sentencia CQL.");
                throw new Exception(sentenciaCQL.toString());
            }

            // Que la sentencia CQL sea de tipo válido.
            if (!u.contienePalabra(tipoSentencia, "SELECT,UPDATE,INSERT,DELETE")) {
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
                    } else if (u.contienePalabra(campo.getString("clausula"), "SET,WHERE")) {
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
    
}
