package com.diewebsiten.core.webservices;

import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.Log;

import javax.annotation.Resource;

import javax.jws.WebService;

import javax.servlet.http.HttpServletRequest;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * Este Web Service sirve como intermediario entre la capa de vista y la capa
 * de negocio, recibe todas las solicitudes que se hacen desde las páginas
 * de los sitios web y retorna y las respuestas que devuelve la capa de negocio.
 *
 * @author Juan Camilo Flórez Román (www.diewebstien.com).
 */
@WebService(endpointInterface = "com.diewebsiten.core.webservices.WSPrincipal", portName = "WSPrincipalPort",
        serviceName = "WSPrincipal")
public class WSPrincipalImpl implements WSPrincipal {
    
    @Resource
    WebServiceContext ctx;    

    /**
     * Este método es la "puerta" del servicio web, se encarga de recibir las
     * peticiones que se hacen desde los sitios web y las redirecciona a la 
     * capa de negocio dependiendo el tipo de petición.
     *
     * @param idSesion el id de una sesión en caso de que se requiera (puede ser nulo).
     * @param cliente dirección IP del navegador web que hizo la petición al sitio web (para auditoría).
     * @param evento Nombre del evento que se ejecutará.
     * @param parametros cadena de tipo JSON con los parámetros necesarios para 
     * ejecutar la petición recibida, los primeros 2 argumentos de la cadena con formato
     * JSON tienen que ser el nombre de la página y el idioma.
     * @return String: cadena de tipo JSON con el código HTML y JS para ser
     * desplegado en el navegador web.
     */
    @Override
    public String puntoEntrada(String idSesion, String cliente, String evento, String parametros) {
        
        String parametrosRecibidos = " idSesion: " + idSesion
                                   + " cliente: " + cliente
                                   + " evento: " + evento
                                   + " param: " + parametros;
        
        // Obtener los datos de la petición.
        MessageContext msg = ctx.getMessageContext();
        HttpServletRequest request = (HttpServletRequest) msg.get(MessageContext.SERVLET_REQUEST);
        
        String resultadoTransaccion; // Variable para guardar la respuesta de la petición (estructura de la página y otros valores).

        try {
            
            resultadoTransaccion = ""; 
                
            //resultadoTransaccion = new TransaccionesCassandra(evento, parametros, request.getServerName()).ejecutarEvento();

        } /*catch (ExcepcionGenerica eg) {
            return eg.getMessage() + " " + parametrosRecibidos;
        }*/ catch (Exception e) {
            //Log.getInstance().imprimirErrorEnLog(e);
            return "Intente más tarde";
        }
        
        return resultadoTransaccion;

    }// puntoEntrada
    
}
