package com.diewebsiten.core.negocio;

import com.datastax.driver.core.Session;
import com.diewebsiten.core.util.Log;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

public class ProbarFabrica2 {
    
    public static void main(String[] args) throws Exception {

        try {
            
             
            
            
            
            System.out.println("Opcional".equals(null));
//            Session ses = new FabricaCassandra().getSesion();
//            String idioma = "ES";
//            String pagina = "index";
//            String sitioWeb = "localhost";
//
//            //ses.execute("BEGIN BATCH SELECT * FROM diewebsiten.transacciones; SELECT * FROM diewebsiten.enunciados; APPLY BATCH;");
//            System.out.println(ses.execute(ses.prepare("select * from diewebsiten.usuarios where sitioweb = ? and dominio = ? and usuario = ?")
//                    .bind("localhost", "gmail.com", "jcflorez"))
//                    .one().getString("sitioweb"));
//            System.out.println("ok");
            
            //Map<String, Object> parametros = new HashMap<String, Object>();
            //parametros.put("basededatos", "diewebsiten");
            //parametros.put("tabla", "formularios");
            //parametros.put("lista", "tiposTransacciones");
//            parametros.put("tipo", "PAG");
//            parametros.put("sitioweb", "localhost");
            //parametros.put("tipo", "SW");
//            parametros.put("sitioweb", "miradorhumadea.com");
//            parametros.put("email", "jcflorezr@gmail.com");
//            parametros.put("password", "juan");
//            parametros.put("sessionid", "l4ukv0kqbvoirg7nkp4dncpk3");
//            parametros.put("fechaultimoacceso", "2014-11-08 12:08:9");
            
//            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarInfoTabla", parametros));
           // System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "CargaInicialPaginaEventos", parametros));
//            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarPaginasSitioWeb", parametros));
//            System.out.println(new TransaccionesCassandra().ejecutarEvento("localhost", "eventos", "ES", "ConsultarBasesDeDatosSitioWeb", parametros));
//            new FabricaCassandra().ejecutarEvento("localhost", "login", "ES", "Login usuario", parametros);
            
        } catch (Exception e) {
            Log.getInstance().imprimirErrorEnLog(e);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
