package com.famisanar.ctc.demo;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class Class1 {
    
    
    public static void main(String[] args) throws Exception {
        
        String estructura = "{\"codigoIPS\": \"\",\"codigoSucursalIPS\": \"\",\"identificacionAfiliado\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"diagnosticosCTC\": [{\"codigo\": \"\",\"tipo\": \"\"}],\"origenSolicitud\": \"\",\"fechaSolicitud\": \"\",\"claseCTC\": \"\",\"fechaIngresoHospitalario\": \"2015-05-09\",\"servicioNoPOS\": {\"codigo\": \"\",\"descripcion\": \"\",\"cantidad\": \"\",\"presentacion\": \"\",\"frecuencia\": \"\",\"diasTratamiento\": \"\",\"marcaComercial\": \"\"},\"objetivosCTC\": [], \"motivoEvaluacion\": \"\", \"otraAlternativa\": \"\",\"riesgoInminente\": \"\",\"descripcionRiesgo\": \"\",\"referenciaBibliografica\": \"\",\"justificacion\": \"\",\"observaciones\": \"\",\"profesionalSalud\": {\"identificacion\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"nombre\": \"\",\"primerApellido\": \"\", \"segundoApellido\": \"\",\"especialidad\": \"\"},\"radicadorCTC\": {\"identificacion\": \"\",\"nombre\": \"\", \"apellido\": \"\"},\"archivosCTC\": [{\"nombre\": \"\",\"contenido\": [],\"tipoDocumental\": \"\"}],\"numeroSolicitud\": \"\"}";
        String[] campos = null;
        
        JsonObject estructuraInput = null;
          
        estructuraInput = new JsonParser().parse(estructura).getAsJsonObject();
            
        campos = "objetivosCTC_2".split("_");
        
        if (campos.length > 1) {
            
            Object p = estructuraInput.get(campos[0]);
            
            for (int i = 1; i < campos.length; i++) {
                
                String campo = campos[i];
                         
                if (campo.substring(0, 1).matches("[0-9]")) {
                    JsonArray a = (JsonArray) p; 
                    if (i + 1 == campos.length) {                                
                        a.add(new JsonPrimitive("FUNCIONA"));
                    } else {
                        int indice = Integer.parseInt(campo) - 1;
                        if (a.size() == indice) {                                        
                            if (a.get(indice - 1).isJsonObject()) {
                                a.add(new JsonObject());
                                for(Map.Entry<String, JsonElement> key : a.get(indice - 1).getAsJsonObject().entrySet()) {
                                    a.get(indice).getAsJsonObject().addProperty(key.getKey(), "");
                                }
                            } else if (a.get(indice - 1).isJsonArray()) {
                                a.add(new JsonArray());
                            }
                        }
                        p = a.get(indice);
                    }
                } else {
                    JsonObject o = (JsonObject) p; 
                    if (i + 1 == campos.length) {
                        o.get(campo);                               
                        o.addProperty(campo, "FUNCIONA");
                    } else {
                        p = o.get(campo);
                    }
                }
                
            }
                        
                    
                
                System.out.println(estructuraInput.toString());
                //Gson gson = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Serializer()).registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Deserializer()).create();
                
                
                
            }
            
        
    }
    
    
}












 class App
{
  public static void main(String[] args) throws Exception
  {
    List keys1 = getKeysFromJson("/Users/juaflore/DieWebsiten/DieWebsitenBD/estructura.json");
    System.out.println(keys1.size());
    System.out.println(keys1);

    /*List keys2 = getKeysFromJson("input_with_lists.json");
    System.out.println(keys2.size());
    System.out.println(keys2);*/
  }

  static List getKeysFromJson(String fileName) throws Exception
  {
    Object things = new Gson().fromJson(new FileReader(fileName), Object.class);
    List keys = new ArrayList();
    collectAllTheKeys(keys, things);
    return keys;
  }

  static void collectAllTheKeys(List keys, Object o)
  {
    Collection values = null;
    if (o instanceof Map)
    {
      Map map = (Map) o;
      keys.addAll(map.keySet()); // collect keys at current level in hierarchy
      values = map.values();
    }
    else if (o instanceof Collection)
      values = (Collection) o;
    else // nothing further to collect keys from
      return;

    for (Object value : values)
      collectAllTheKeys(keys, value);
  }
}
