package com.famisanar.ctc.demo;

import com.famisanar.ctc.service.WSCTCImpl;
import com.famisanar.ctc.types.RadicarCTCInputType;

import com.famisanar.ctc.types.RadicarCTCOutputType;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.datatype.DatatypeFactory;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class Cont extends HttpServlet {
    
    private static final String CONTENT_TYPE = "text/html; utf-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request,
                        HttpServletResponse response) throws ServletException,
                                                             IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        DemostracionDAO d = new DemostracionDAO();
        JSONObject obj;
        int accion = null != request.getParameter("accion") ? Integer.parseInt(request.getParameter("accion")) : 99;
        
        try {            
            switch(accion) {
            case 1:
                String nombreIPS = null != request.getParameter("term") ? request.getParameter("term").replace("+", "") : null;
                out.println(d.consultarIPSs(nombreIPS));
                break;
            case 2:
                String nombreDiagnostico = null != request.getParameter("term") ? request.getParameter("term").replace("+", "") : null;
                out.println(d.consultarDiagnosticos(nombreDiagnostico));
                break;
            case 3:
                obj = new JSONObject();
                obj.put("origenesSolicitud",  new JSONArray(d.consultarOrigenes()));
                obj.put("clasesCTC",  new JSONArray(d.consultarClases()));
                obj.put("tiposIdentificacion",  new JSONArray(d.consultarTiposIdentificacion()));
                obj.put("objetivosCTC",  new JSONArray(d.consultarObjetivosCTC()));
                out.println(obj);
                break;
            case 4:
                String nombrePresentacion = null != request.getParameter("term") ? request.getParameter("term").replace("+", "") : null;
                out.println(d.consultarPresentaciones(nombrePresentacion));
                break;
            case 5:
                String nombreFrecuencia = null != request.getParameter("term") ? request.getParameter("term").replace("+", "") : null;
                out.println(d.consultarFrecuencias(nombreFrecuencia));
                break;
            case 6:
                String nombreEspecialidad = null != request.getParameter("term") ? request.getParameter("term").replace("+", "") : null;
                out.println(d.consultarEspecialidades(nombreEspecialidad));
                break;
            case 7:
                String nombreServicioNoPos = null != request.getParameter("term") ? request.getParameter("term").replace("+", "") : null;
                out.println(d.consultarServiciosNoPos(nombreServicioNoPos));
                break;
            case 8:
                obj = new JSONObject();
                obj.put("tiposDocumentales",  new JSONArray(d.consultarTiposDocumentales()));
                out.println(obj);
                break;
            case 99:
                out.print("No se ha especificado el parámetro 'acción'");
                break;
            default:
                out.print("Parámetro 'acción' inválido: (" + accion + ")");
            }
            
        } catch (Exception e) {            
            e.printStackTrace();
        } finally {
            out.close();
        }
        
    }
    
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException,
                                                            IOException {
        response.setContentType(CONTENT_TYPE);
        
        String estructura = "{\"codigoIPS\": \"\",\"codigoSucursalIPS\": \"\",\"identificacionAfiliado\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"diagnosticosCTC\": [{\"codigo\": \"\",\"tipo\": \"\"}],\"origenSolicitud\": \"\",\"fechaSolicitud\": \"\",\"claseCTC\": \"\",\"fechaIngresoHospitalario\": \"2015-05-09\",\"servicioNoPOS\": {\"codigo\": \"\",\"descripcion\": \"\",\"cantidad\": \"\",\"presentacion\": \"\",\"frecuencia\": \"\",\"diasTratamiento\": \"\",\"marcaComercial\": \"\"},\"objetivosCTC\": [], \"motivoEvaluacion\": \"\", \"otraAlternativa\": \"\",\"riesgoInminente\": \"\",\"descripcionRiesgo\": \"\",\"referenciaBibliografica\": \"\",\"justificacion\": \"\",\"observaciones\": \"\",\"profesionalSalud\": {\"identificacion\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"nombre\": \"\",\"primerApellido\": \"\", \"segundoApellido\": \"\",\"especialidad\": \"\"},\"radicadorCTC\": {\"identificacion\": \"\",\"nombre\": \"\", \"apellido\": \"\"},\"archivosCTC\": [{\"nombre\": \"\",\"contenido\": [],\"tipoDocumental\": \"\"}],\"numeroSolicitud\": \"\"}";
        String[] campos = null;
        JSONObject estructuraInput = null;
        
        try {
            
            if (ServletFileUpload.isMultipartContent(request)) {
                
                List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                RadicarCTCInputType input = new RadicarCTCInputType();
                estructuraInput = new JSONObject(estructura);
                
                for (FileItem item : items) {
                    
                    Object fieldValue = new Object();
                    
                    if (item.isFormField())
                        fieldValue = item.getString();                        
                    else
                        fieldValue = IOUtils.toByteArray(item.getInputStream());
                    
                    campos = item.getFieldName().split("_");
                    
                    if (campos.length > 1) {
                        
                        Object p = estructuraInput.get(campos[0]);
                        
                        for (int i = 1; i < campos.length; i++) {
                            
                            String campo = campos[i];
                                     
                            if (campo.substring(0, 1).matches("[0-9]")) {
                                JSONArray a = (JSONArray) p; 
                                if (i + 1 == campos.length) {                                
                                    a.put(fieldValue);
                                } else {
                                    int indice = Integer.parseInt(campo) - 1;
                                    if (a.length() == indice) {                                        
                                        if (a.get(indice - 1) instanceof JSONObject) {
                                            a.put(new JSONObject());
                                            Iterator it = a.getJSONObject(indice - 1).keys();
                                            while(it.hasNext()) {
                                                String key = (String) it.next();
                                                a.getJSONObject(indice).put(key, "");
                                            }
                                        } else if (a.get(indice - 1) instanceof JSONArray) {
                                            a.put(new JSONArray());
                                        }
                                    }
                                    p = a.get(indice);
                                }
                            } else {
                                JSONObject o = (JSONObject) p; 
                                if (i + 1 == campos.length) {
                                    o.get(campo);
                                    if (fieldValue instanceof byte[])
                                        o.put("nombre", FilenameUtils.getName(item.getName()));                                
                                    o.put(campo, fieldValue);
                                } else {
                                    p = o.get(campo);
                                }
                            }
                            
                        }
                        
                    } else {
                        if ("fechaSolicitud".equals(campos[0]) || "fechaIngresoHospitalario".equals(campos[0])) {
                            estructuraInput.put(campos[0], DatatypeFactory.newInstance().newXMLGregorianCalendar(fieldValue.toString()));
                            continue;
                        }
                        
                        estructuraInput.put(campos[0], fieldValue); 
                    }
                    
                }
                
                
                Gson gson = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, 
                                                                  new XGCalConverter.Serializer()).registerTypeAdapter(XMLGregorianCalendar.class,
                                                                  new XGCalConverter.Deserializer()).create();
                
                
                input = gson.fromJson(estructuraInput.toString(), RadicarCTCInputType.class);
                
                RadicarCTCOutputType respuesta = new WSCTCImpl().radicarSolicitudCTC(input);
                
                System.out.println(respuesta.getNumeroRadicacion() + " - " + respuesta.getResultado() + " - " + Arrays.asList(respuesta.getObservaciones()));
                
                //System.out.println(estructuraInput.toString(2));
                
            }
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(campos));
            try {
                System.out.println(estructuraInput.toString(2));
            } catch (Exception ex) {
                System.out.println("Fallo al imprimir el JSON");
            }
            
            e.printStackTrace();
        }
            
        
    }
}
