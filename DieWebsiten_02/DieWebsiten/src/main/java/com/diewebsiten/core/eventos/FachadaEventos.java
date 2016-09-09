package com.diewebsiten.core.eventos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.diewebsiten.core.almacenamiento.AlmacenamientoFabrica;
import com.diewebsiten.core.almacenamiento.ProveedorAlmacenamiento;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.eventos.util.Mensajes;
import com.diewebsiten.core.excepciones.ExcepcionDeLog;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FachadaEventos {
	
	private static Log logger;
    private static ProveedorAlmacenamiento proveedorAlmacenamiento;
	
    public static void main(String[] args) throws ExcepcionGenerica {
    	new FachadaEventos().iniciarModuloEventos();
    }
    
    public void iniciarModuloEventos() {
        
    	final ThreadFactory threadFactoryBuilder = new ThreadFactoryBuilder().setNameFormat("Eventos-%d").setDaemon(true).build();
        ExecutorService ejecucionEventos = Executors.newFixedThreadPool(10, threadFactoryBuilder);
        
        try {
        	
        	iniciarLog();

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
                                 "\"tipotransaccion\": \"sELECT\"" +
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
                                 "\"tipotransaccion\": \"seLECT\"" +
                                 "}";
            
//            String parametros2 = "{" +
//                                 "\"pagina\": \"eventos1\"," +
//                                 "\"evento\": \"nuevo Evento\"," +
//                                 " \"sitioweb\": \"localhost\"," +
//                                 "\"transaccion\": \"nueva transaccion\"," +
//                                 "\"basededatos\": \"diewebsiten\"," +
//                                 "\"tipotransaccion\": \"SELECT\"," +
//                                 "\"descripcion\": \"SELECT\"," +
//                                 "\"orden\": \"SELECT\"," + 
//                                 "\"sentenciacql\": \"SELECT\"," + 
//                                 "\"tabla\": \"SELECT\"," +
//                                 "\"clausula\": \"SELECT\"," +
//                                 "\"sentenciacql\": \"SELECT\"" +
//                                 "}";
            
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


            //Evento.setSesionBD();
            //Evento.setSentenciasPreparadas();
            
            
            
            List<Future<ObjectNode>> grupoEventos = new ArrayList<>();
            
            
            
            
            
            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "CargaInicialPaginaEventos", null)));
            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "ConsultarInfoSitioWeb", parametros)));
            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "ConsultarInfoBaseDeDatos", parametros)));
            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "CargaInicialPaginaEventos", null)));
            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "ConsultarInfoSitioWeb", parametros1)));
//            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "ConsultarInfoBaseDeDatos", parametros1)));
            grupoEventos.add(ejecucionEventos.submit(new Eventos("localhost:@:eventos", "ConsultarInfoTabla", parametros1)));

//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "CargaInicialPaginaEventos", null).call()
////                    ));
//            );
//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "ConsultarInfoSitioWeb", parametros).call()
////            ));
//            );
//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "ConsultarInfoBaseDeDatos", parametros).call()
////            ));
//            );
//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "CargaInicialPaginaEventos", null).call()
////            ));
//            );
//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "ConsultarInfoSitioWeb", parametros1).call()
////            ));
//            );
//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "ConsultarInfoBaseDeDatos", parametros1).call()
////            ));
//            );
//            System.out.println(
////            grupoEventos.add(ejecucionEventos.submit(
//                    new Eventos("localhost:@:eventos", "ConsultarInfoTabla", parametros1).call()
////            ));
//            );
            
            for (Future<ObjectNode> evento : grupoEventos) {
                System.out.println(evento.get());
            }
            
            System.out.println(((System.currentTimeMillis() - timestamp) / 1000) + " seg.");
            
            
           
            
            
            
            
        } catch (ExcepcionDeLog elog) {
        	elog.printStackTrace();
        } catch (Exception e) {
        	
            try {
            	logger.imprimirErrorEnLog(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
            
            System.out.println(Mensajes.ERROR.get());
            
        } finally {
//        	ejecucionEventos.shutdown();
        	// Finalizar la conexión con la base de datos cassandra
            AlmacenamientoFabrica.desactivarProveedoresAlmacenamiento();
        }
        
    }
    
    private static void iniciarLog() throws ExcepcionDeLog {
    	logger = Log.getInstance();
    }
    
//    private static void desactivarProveedoresAlmacenamiento() {
//    	AlmacenamientoFabrica.desactivarProveedoresAlmacenamiento();
//	}
    
    /**
     * 
     * @param transaccion
     * @return
     * @throws Exception
     */
    static JsonNode ejecutarTransaccion(Transaccion transaccion) {
    	return AlmacenamientoFabrica.obtenerProveedorAlmacenamiento(transaccion.getMotorAlmacenamiento()).ejecutarTransaccion(transaccion);
    }
    
}