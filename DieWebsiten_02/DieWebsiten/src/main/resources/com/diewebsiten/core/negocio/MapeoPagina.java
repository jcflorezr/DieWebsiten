
package com.diewebsiten.core.negocio;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.Log;
import com.diewebsiten.core.util.Utilidades;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Esta clase se encarga de gestionar toda la información relacionada con las páginas
 * de un sitio web y la forma en como estas páginas se despliegan en el navegador web.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
public class MapeoPagina {
    
//    private String inicialesSitioWeb; // Guardar las iniciales del sitio web para buscar sesiones de usuario.    
//    private String datosSesion; // Guardar la información de la sesión en caso de que la página actual lo requiera.    
//    private String datosTransaccion; // Guardar la información de una transacción que no tenga que ver con el despliegue de la página actual.
//    
//    
//    public MapeoPagina() {
//    }
//    
//    /**
//     * @param sitioWeb las iniciales del sitio web para asociar el sitio web con una sesión.
//     * @param datosSesion Información de la sesión, solo acepta un string formato JSONObject 
//     *                    con los datos de la sesión y del usuario (en caso de que la sesión 
//     *                    haya sido creada anteriormente), o un string con el ID de la sesión
//     *                    (en caso de que no exista una sesión).
//     * @param datosTransaccion datos de la transacción que no tenga que ver con el despliegue
//     *                         de la página actual.
//     */
//    public MapeoPagina(String sitioWeb, String datosSesion, String datosTransaccion) {
//        this.inicialesSitioWeb = sitioWeb;
//        this.datosSesion = datosSesion;
//        this.datosTransaccion = datosTransaccion;
//    }    
//    
//    
//    /**
//     * Recibe los elementos del despliegue de la página actual para mapearlos de tal 
//     * forma que la página JSP los pueda renderizar en el navegador.
//     *
//     * @param sitioWeb Iniciales del sitio web de donde pertenece la página que 
//     *                 está realizando la petición.
//     * @param respuesta String con formato JSONObject que contiene un mapeo plano del
//     *                  despliegue inicial para la página del sitio web que está 
//     *                  realizando la petición.
//     * @param peticionInterna TRUE si la petición se hace desde la aplicación (Servlet), FALSE si
//     *                        la petición se está haciendo desde el Web Service.
//     * @return String: cadena de tipo JSON con el mapeo completo del despliegue
//     * por niveles y jerarquías (si la petición se hace desde el Web Service) (FALTA DOCUMENTACION DETALLADA);
//     * o el código HTML, JS y CSS de la página (si la petición se hace internamente desde la capa de control).
//     */
//    public void guardarMapeoEstructuraPagina(String sitioWeb, String pagina, String idioma, String codigoHtml) throws Exception {
//
//        Map<String, String> enunciado;
//
//        StringBuilder datosElemento;
//        
//        List<Object> paramDespliegues = new ArrayList<Object>();
//        
//        String batch = "BEGIN BATCH ";        
//
//        try {            
//            
//            Session ses = new FabricaCassandra().getSesion();
//
//            Elements estructuraHtml = Jsoup.parse(codigoHtml).getAllElements();
//            estructuraHtml.remove(0);
//
//            // ===== codigo HTML ===== //
//            batch += "INSERT INTO diewebsiten.codigos_por_pagina (sitioweb, idioma, pagina, codigo) VALUES (?,?,?,?); ";
//            paramDespliegues.add(sitioWeb);
//            paramDespliegues.add(idioma);
//            paramDespliegues.add(pagina);
//            paramDespliegues.add(codigoHtml);
//
//            codigoHtml = null;
//
//            for (Element elemento : estructuraHtml) {
//
//                enunciado = new LinkedHashMap<String, String>();
//                datosElemento = new StringBuilder();
//
//                for (Map.Entry<String, String> atributo : elemento.attributes()) {
//                    if (!atributo.getKey().equals("class")) {
//                        enunciado.put(atributo.getKey(), atributo.getValue());
//                    } else {
//                        enunciado.put("classAttr", atributo.getValue());
//                    }
//                }
//
//                String texto = elemento.nodeName().equals("script") || elemento.nodeName().equals("style") ? elemento.data() : elemento.ownText();
//
//                // ruta
//                for (Element padre : elemento.parents()) {
//                    datosElemento.append((padre.elementSiblingIndex() + 1)).append(".");
//                }
//
//                if (elemento.parents().size() > 0) {
//                    datosElemento = new StringBuilder(StringUtils.reverseDelimited(datosElemento.toString(), ".".charAt(0))).append(".");
//                }
//
//                // ===== Enunciados ===== //
//                batch += "INSERT INTO diewebsiten.enunciados (sitioweb, pagina, nivel, idioma, etiqueta, ruta, atributos, texto) VALUES (?,?,?,?,?,?,?,?); ";
//                paramDespliegues.add(sitioWeb);
//                paramDespliegues.add(pagina);
//                paramDespliegues.add(elemento.parents().size() + 1);
//                paramDespliegues.add(idioma);
//                paramDespliegues.add(elemento.tagName());
//                paramDespliegues.add(datosElemento.toString() + (elemento.elementSiblingIndex() + 1));
//                paramDespliegues.add(enunciado);
//                paramDespliegues.add(texto);
//                
//                texto = null;
//                
//                // ===== Despliegue ===== //
//                batch += "INSERT INTO diewebsiten.despliegues (sitioweb, pagina, ruta) VALUES (?,?,?); ";
//                paramDespliegues.add(sitioWeb);
//                paramDespliegues.add(pagina);
//                paramDespliegues.add(datosElemento.toString() + (elemento.elementSiblingIndex() + 1));
//
//            }
//
//            batch += "APPLY BATCH;";
//            PreparedStatement p = ses.prepare(batch);
//            ses.execute(p.bind(paramDespliegues.toArray()));
//
//            
//            
//        } catch (Exception e) {
//            String mensaje = "/******************** Ocurrio un error al procesar la estructura de la página **********************/";
//            System.out.println(Log.encontrarError(e, this.getClass(), e.getClass().toString(), mensaje));
//        }
//
//    }// guardarMapeoEstructuraPagina
//    
//
//    /**
//     * Recibe los datos del despliegue para invocar a todos los métodos que contienen el código
//     * HTML, JS y CSS necesarios para desplegar la página en el navegador web.
//     * 
//     * @param sitioWeb las iniciales del sitio web para obtener algunos códigos HTML, JS y CSS.
//     * @param pagina El nombre de la pagina.
//     * @return Todo el código HTML, JS y CSS necesario para desplegar la página.
//     */
////    private String obtenerCodigoPagina(String sitioWeb, String pagina, String idioma) {
////        
////        boolean transaccionExitosa = false;
////        String codigoPagina = "";
////        
////        try {
////            
////            Session ses = new FabricaCassandra().getSesion();
////
////            String select;
////            PreparedStatement p;
////            
////            // ====== Nuevo Sitio Web ====== //
////            select = "SELECT * FROM diewebsiten.codigos_por_pagina WHERE sitioweb = ? AND pagina = ? AND idioma = ?;";
////            p = ses.prepare(select);
////            ses.execute(p.bind(sitioWeb, pagina, idioma));
////            
////            // La transacción fue exitosa.
////            transaccionExitosa = true;
////
////        } catch (Exception e) {
////            String mensaje = "/*************** Ocurrió un error al obtener el código HTML y JS de la página **************/";
////            System.out.println(Log.encontrarError(e, this.getClass(), e.getClass().toString(), mensaje));
////        } finally {
////            // Por último verificar si la transacción fue exitosa.
////            if (!transaccionExitosa) {
////                // Si ocurrió un error se retornará un arreglo con un solo registro para enviar un mensaje de error.                
////                codigoPagina = "{\"error\" : \"" + Constantes.ERROR + "\"}";
////            }
////        }
////        
////        return codigoPagina;
////        
////    }// obtenerCodigoPagina
//    
//    
//    /**
//     * Crear una sesión en la base de datos o actualizar una que ya existe.
//     * 
//     
//     * @return string formato JSONObject con los datos de la sesión (ID de la sesión, usuario, etc.) o 
//     *         un string formato JSONObject con un mensaje de error.
//     */
//    public String gestionarSesion() {
//        
//        boolean transaccionExitosa = false;
//        String resultadoGestionSesion = null;
//        String usuario = null;
//        String sesion = null;
//        String activa;
//        
//        try {
//            
//            JSONObject jsonTransaccion = new JSONObject(datosTransaccion);
//            JSONObject jsonSesion;
//            Utilidades u = new Utilidades();
//            boolean esJSON = u.esJSON(datosSesion).equals("JSONObject");
//            
//            System.out.println("idSes: " + datosSesion);
//            System.out.println("trans:" + datosTransaccion);
//            
//            
//            
//            if (esJSON) {
//                
//                // ================================================================== //
//                //  Si los datos de la sesión vienen en un string formato JSONObject  //
//                //  significa que ya hay una sesión creada en el cliente y se están.  //
//                // ================================================================== //
//                
//                jsonSesion = new JSONObject(datosSesion);
//                
//                // Es posible que se reciba desde el cliente (navegador web) un JSONObject con un mensaje de error.
//                if (jsonSesion.has("error"))
//                    throw new Exception("Se recibió desde el cliente un mensaje de error en la variable de sesión");
//                
//                sesion = jsonSesion.getString("sesion");
//                usuario = !u.esVacio(jsonSesion.getString("usuario"))
//                        ? jsonSesion.getString("usuario")
//                        : "";
//            
//            } else if (jsonTransaccion.has("trans") && jsonTransaccion.has("exito")) {
//                
//                // ========================================================================= //
//                //  Si solo se está recibiendo el ID de la sesión por medio de un string     //
//                //  significa que no hay una sesión creada, o el usuario perdió la sesión    //
//                //  se logueó de nuevo, o el usuario limpió el historial del navegador web.  //
//                // ========================================================================= //
//                
//                sesion = datosSesion;
//                
//                // Si la solicitud de gestión de sesión no viene del método "SentenciasSQL.consultarUsuariosLogin"
//                // se pone un valor vacío en la variable "usuario" para prevenir ataques.
//                usuario = jsonTransaccion.getString("trans").equals("ConsultarUsuariosLogin")
//                        ? jsonTransaccion.getJSONArray("exito").getJSONObject(0).getString("codigo") + "-" +
//                          jsonTransaccion.getJSONArray("exito").getJSONObject(0).getString("usuario")
//                        : "";
//                
//            }
//            
//            // Validar que el usuario existe antes de gestionar la sesión.
//            if (new Utilidades().esVacio(usuario)) 
//                throw new Exception("No se puede gestionar una sesión sin tener el nombre de usuario.");
//
//            // Buscar la sesión en la base de datos.
//            String fabDTO = "{\"atrib1\" : \"" + usuario + "\","
//                          + " \"atrib2\" : \"" + sesion + "\"}";
//            jsonSesion = new JSONObject(new Transacciones().transaccion(inicialesSitioWeb, "Sesiones", "Consultar", fabDTO));
//            
//            // Validar que no hubo error al buscar la sesión en la base de datos.
//            if (jsonSesion.has("error")) 
//                throw new Exception("Error al verificar en la base de datos la existencia de la sesión");            
//
//            if (jsonSesion.toString().equals("{}")) {
//                
//                // =========================================== //
//                // =========== CREAR NUEVA SESIÓN ============ //
//                // =========================================== //
//                
//                // Crear en la base de datos una nueva sesión con un nuevo ID para el usuario.
//                fabDTO = "{\"atrib1\" : \"id_sesion = " + sesion + "\","
//                       + " \"atrib2\" : \"fecha_ultimo_acceso = " + new Timestamp(new Date().getTime()) + "\","
//                       + " \"atrib3\" : \"activa = 1\","
//                       + " \"atrib4\" : \"codigo_usuario = " + StringUtils.substringBefore(usuario, "-") + "\","
//                       + " \"atrib5\" : \"codigo_sitio_web = " + StringUtils.substringBefore(inicialesSitioWeb, "-") + "\"}";
//                datosSesion = new Transacciones().transaccion(inicialesSitioWeb, "Sesiones", "AgregarRegistros", fabDTO);
//                
//                // Obtener el usuario junto con su "codigo_usuario" concatenado. Ej: "1-juan890202"
//                usuario = jsonTransaccion.getJSONArray("exito").getJSONObject(0).getString("codigo") + "-" + usuario;
//                
//            } else {
//                
//                // ==================================================== //
//                // =========== ACTUALIZAR SESIÓN EXISTENTE ============ //
//                // ==================================================== //
//                
//                // Obtener la fecha en la que se tuvo último acceso a la sesión y convertirla a variable "long".
//                Timestamp fechaUltimoAcceso = Timestamp.valueOf(jsonSesion.getJSONArray("exito").getJSONObject(0).getString("fecha_ultimo_acceso"));
//                
//                // Calcular la diferencia en segundos desde que se accedió por última vez a la sesión hasta ahora.
//                long tiempoAcceso = (new Timestamp(new Date().getTime()).getTime() / 1000) - (fechaUltimoAcceso.getTime() / 1000);
//                
//                // Obtener el máximo en segundos permitido para mantener una sesión activa.
//                long tiempoExpiracionMaximo = Long.parseLong(jsonSesion.getJSONArray("exito").getJSONObject(0).getString("tiempo_maximo"));
//
//                // Se actualiza la fecha de último acceso a la sesión sólo si ésta aun no ha expirado.
//                fabDTO = "{\"atrib1\" : \"fecha_ultimo_acceso = " + new Timestamp(new Date().getTime()) + " = SET\",";
//                
//                if (tiempoAcceso > tiempoExpiracionMaximo) {     System.out.println("excedió");               
//                    // Ya no se actualiza la fecha de último acceso a la sesión si no que se actualiza
//                    // el campo "activa" a 0 debido a que la sesión ya expiró.
//                    // Pero si la solicitud viene del método "SentenciasSQL.consultarUsuariosLogin"
//                    // significa que el usuario se logueó nuevamente para reactivar la sesión.                           
//                    if (jsonTransaccion.has("trans")) {
//                        if (!jsonTransaccion.getString("trans").equals("ConsultarUsuariosLogin")) {
//                            fabDTO = "{\"atrib1\" : \"activa = 0 = SET\",";
//                        }
//                    }                    
//                }
//                
//                fabDTO += " \"atrib2\" : \"codigo_usuario = " + StringUtils.substringBefore(usuario, "-") + " = WHERE\","
//                        + " \"atrib3\" : \"codigo_sitio_web = " + StringUtils.substringBefore(inicialesSitioWeb, "-") + " = WHERE\","
//                        + " \"atrib4\" : \"id_sesion = " + sesion + " = WHERE\"}";
//                datosSesion = new Transacciones().transaccion(inicialesSitioWeb, "Sesiones", "ModificarRegistros", fabDTO);
//                System.out.println(fabDTO);
//            }
//            
//            // Validar que se creó o se actualizó la sesión satisfactoriamente.
//            if (!datosSesion.equals("exito")) {
//                datosSesion += " - Error al crear o al actualizar la sesión."
//                            + "\nUsuario --> " + usuario
//                            + "\nID sesión --> " + sesion
//                            + "\nSitio web --> " + inicialesSitioWeb
//                            + "\nFecha y hora --> " + new Date() + "\n";
//                throw new Exception(datosSesion);
//            }
//            
//            // Obtener los datos de la sesión actualizada.
//            fabDTO = "{\"atrib1\" : \"" + usuario + "\","
//                   + " \"atrib2\" : \"" + sesion + "\"}";
//            jsonSesion = new JSONObject(new Transacciones().transaccion(inicialesSitioWeb, "Sesiones", "Consultar", fabDTO));
//            
//            // Validar que no hubo error al buscar la sesión en la base de datos.
//            if (jsonSesion.has("error") || jsonSesion.toString().equals("{}")) 
//                throw new Exception(jsonSesion.toString() + " - Error al obtener los datos de la sesión despues de ser actualizada");
//            
//            activa = jsonSesion.getJSONArray("exito").getJSONObject(0).getString("activa");
//            
//            // Crear un string con formato JSONObject para retornar los datos de la sesión.
//            resultadoGestionSesion = "{\"sesion\" : \"" + sesion + "\", \"usuario\" : \"" + usuario + "\", \"activa\" : \"" + activa + "\"}";
//            
//            // La transacción fue exitosa.
//            transaccionExitosa = true;
//            
//        } catch (Exception e) {
//            String mensaje = "/*************** Ocurrió un error al gestionar la sesión **************/";
//            System.out.println(Log.encontrarError(e, this.getClass(), e.getClass().toString(), mensaje));            
//        } finally {            
//            if(!transaccionExitosa) {
//                resultadoGestionSesion = "{\"error\" : \"" + Constantes.ERROR_SESION + "\"}";
//            }            
//        }
//        
//        return resultadoGestionSesion;
//        
//    }// gestionarSesion    
    
}