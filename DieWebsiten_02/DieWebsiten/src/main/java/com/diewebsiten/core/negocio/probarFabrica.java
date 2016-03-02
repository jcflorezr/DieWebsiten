package com.diewebsiten.core.negocio;

import com.datastax.driver.core.PreparedStatement;
import com.diewebsiten.core.almacenamiento.ProveedorCassandra;
import com.diewebsiten.core.negocio.eventos.Evento;
import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.Log;
import com.diewebsiten.core.util.Utilidades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class probarFabrica {

    
//    public static long timeDiff(long old) {
//        return System.nanoTime() - old;
//    }
    
    
    public static void main(String[] args) {
        
        
        /*try {
            
            //String s = "miradorhumadea_mmcom";
            //System.out.println(StringUtils.isAlphanumeric(s));
            //System.out.println(s.matches("[a-z0-9._-]+"));
            
            String h = "[\"[\"sitioweb\"]\",\"[\"pagina\",\"idioma\",\"nivel\",\"etiqueta\",\"ruta\"]\"]";
            if (new Utilidades().esJSON(h) instanceof String)
                System.out.println("String");
            if (new Utilidades().esJSON(h) instanceof JSONObject)
                System.out.println("JSONObject");
            if (new Utilidades().esJSON(h) instanceof JSONArray)
                System.out.println(new Utilidades().esJSON(h));
            
        } catch (Exception e) {
            System.out.println("Excepción: " + e);
        }*/
        
        
        
        try {
            long timestamp = System.currentTimeMillis();
            
            /*String s = "miradorhumadea_mmcom";
            //System.out.println(StringUtils.isAlphanumeric(s));
            System.out.println(s.matches("[a-z0-9._-]+"));*/
            
            String parametros1 = "{" +
                                 "\"coleccion\": \"tiposTransacciones\"," +
                                 " \"sitioweb\": \"miradorhumadea.com\"," +
                                 "\"tipo\": \"SW\"," +
                                 "\"basededatos\": \"diewebsiten\"," +
                                 "\"keyspace_name\": \"diewebsiten\"," +
                                 "\"columnfamily_name\": \"eventos\"," +
                                 "\"tipotransaccion\": \"SELECT\"" +
                                 "}";
            
            /*Map<String, Object> parametros11 = new HashMap<String, Object>();
            parametros1.put("coleccion", "tiposTransacciones");
            //            parametros.put("tipo", "PAG");
            parametros1.put("sitioweb", "miradorhumadea.com");
            parametros1.put("tipo", "SW");
            parametros1.put("basededatos", "diewebsiten");
            parametros1.put("tipotransaccion", "SELECT");*/
            
            String parametros = "{" +
                                 //"\"tipo\": \"PAG\"," +
                                 //"\"coleccion\": \"tiposTransacciones\"," +
                                 " \"sitioweb\": \"miradorhumadea.com\"," +
                                 "\"tipo\": \"SW\"," +
                                 "\"basededatos\": \"diewebsiten\"," +
                                 "\"tipotransaccion\": \"SELECT\"" +
                                 "}";
            
            String parametros2 = "{" +
                                 "\"pagina\": \"eventos1\"," +
                                 "\"evento\": \"nuevo Evento\"," +
                                 " \"sitioweb\": \"localhost\"," +
                                 "\"transaccion\": \"nueva transaccion\"," +
                                 "\"basededatos\": \"diewebsiten\"," +
                                 "\"tipotransaccion\": \"SELECT\"," +
                                 "\"descripcion\": \"SELECT\"," +
                                 "\"orden\": \"SELECT\"," + 
                                 "\"sentenciacql\": \"SELECT\"," + 
                                 "\"tabla\": \"SELECT\"," +
                                 "\"clausula\": \"SELECT\"," +
                                 "\"sentenciacql\": \"SELECT\"" +
                                 "}";
            
            /*Map<String, Object> parametros00 = new HashMap<String, Object>();
            //parametros.put("coleccion", "tiposTransacciones");
            parametros.put("tipo", "PAG");
            parametros.put("sitioweb", "miradorhumadea.com");
            parametros.put("tipo", "SW");
            parametros.put("basededatos", "diewebsiten");
            parametros.put("tipotransaccion", "SELECT");
            //            parametros.put("sitioweb", "miradorhumadea.com");
            //            parametros.put("email", "jcflorezr@gmail.com");
            //            parametros.put("password", "juan");
            //            parametros.put("sessionid", "l4ukv0kqbvoirg7nkp4dncpk3");
            //            parametros.put("fechaultimoacceso", "2014-11-08 12:08:9");
            */


            Evento.setSesionBD();
            Evento.setSentenciasPreparadas();
            
            ExecutorService ejecucionEventos = Executors.newFixedThreadPool(10);
            
            List<Future<String>> grupoEventos = new ArrayList<Future<String>>();
            
            
            
            
            
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "CargaInicialPaginaEventos", null)));
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "ConsultarInfoSitioWeb", parametros)));
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "ConsultarInfoBaseDeDatos", parametros)));
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "CargaInicialPaginaEventos", parametros1)));
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "ConsultarInfoSitioWeb", parametros1)));
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "ConsultarInfoBaseDeDatos", parametros1)));
            grupoEventos.add(ejecucionEventos.submit(new Evento("localhost:@:eventos", "ConsultarInfoTabla", parametros1)));
            
            
            for (Future<String> evento : grupoEventos) {
                System.out.println(evento.get());
            }
            
            System.out.println(((System.currentTimeMillis() - timestamp) / 1000) + " seg.");
            ejecucionEventos.shutdown();
            /*System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarInfoSitioWeb", parametros));
            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarInfoBaseDeDatos", parametros));
            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "CargaInicialPaginaEventos", parametros1));
            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarInfoSitioWeb", parametros1));
            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarInfoBaseDeDatos", parametros1));*/
            
           
            
            
            
            
        } catch (Exception e) {
            Log.getInstance().imprimirErrorEnLog(e);
        } finally {
            ProveedorCassandra.cerrarConexion();
        }
        
    }
    
    
    
    
    
    
    
    
    

}
