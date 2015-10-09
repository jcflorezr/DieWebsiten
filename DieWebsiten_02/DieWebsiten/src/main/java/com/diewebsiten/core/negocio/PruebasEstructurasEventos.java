package com.diewebsiten.core.negocio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class PruebasEstructurasEventos {
	
//	private static Object objetoFinal;
//
//	public static void main(String[] args) throws Exception {
//		
//		
//		//================================================//
//		// esto se debe hacer dinámicamente
//		
//		//Map<String, List<String>> estructura = new LinkedHashMap<String, List<String>>();
//		
//		String tabla = "paginas";
//		
//		List<String> filtros = new ArrayList<String>();
//		filtros.add("sitioweb");
//		List<String> columnas = new ArrayList<String>();
//		columnas.add("pagina");
//		
//		//================================================//
//		
//		
//		//BufferedReader br = new BufferedReader(new FileReader("/Users/juaflore/DieWebsiten/DieWebsitenBD/estructura.json"));
//		BufferedReader br = new BufferedReader(new FileReader("/Users/juancamiloroman/DieWebsiten/DieWebsitenBD/estructura.json"));
//		Gson gson = new Gson();
//		
//		// ========== Obtener la coleccion donde se encuentra la tabla
//		Object jsonObj = encontrarObjeto(tabla, gson.fromJson(br, Object.class));
//		
//		
//		Object things = new Object();
//		
//		
//		String ruta = "";
//				
//				
//				
//		
//		for (String rutaActual : ((Map<String, List<String>>)jsonObj).get("!!rutas")) {
//			
//			// ========== Obtener la ruta donde se encuentra la tabla
//			if (rutaActual.matches(".*?\\b" + tabla + "\\b.*?")) {
//				
//				List<String> tablasRuta = new ArrayList<String>(Arrays.asList((StringUtils.substringBefore(rutaActual, tabla) + tabla).split("@")));
//				
//				// ========== obtener la coleccion para la ruta
//				int i = 0;
//				for (String tablaActual : tablasRuta) {
//					
//					jsonObj = (encontrarObjeto(tablaActual, jsonObj));
//					if (i == tablasRuta.size()) {
//						things = encontrarObjeto(tablaActual, jsonObj);
//						System.out.println("=============================");
//						System.out.println(things);
//					} else {
//						//things = new Map().put(tablaActual, "");
//					}
//					
//					
//					//REVISAR COMO SE PUEDE RETORNAR UN JSON DESDE CAMPOS INTERMEDIOS (CREAR NUEVA COLUMNA EN LA TABLA EVENTOS)
//					
//					
//					i++;
//					
//				}
//				
//				//ruta = rutaActual;
//				
//				break;
//			}
//		}
//		
//		
//		
//		/*for (String estruc : (Set<String>)estructura.keySet()) {
//			Object estrucActual = estructura.get(estruc);
//			if (estrucActual instanceof String)
//				formarEstructuraConsulta(things, estrucActual);
//			else {
//				for (String clausula : (List<String>)estrucActual) {
//					formarEstructuraConsulta(things, clausula);
//				}
//			}
//			
//		}*/
//		
//		
//		
//		
//		//JsonObject jsonObj2;
//		//System.out.println(((Map)jsonObj).keySet());
//		//Set<Map.Entry<String, JsonElement>> entries = jsonObj.entrySet();
//		
//		//Object things = new Object();
//		
//
//	}
//	
//	
//	
//	// RETORNAR OBJETOS BUSCADOS
//	static Object encontrarObjeto(String llaveBuscar, Object coleccion) {
//		
//		Map map;
//		Collection list;
//		
//		if (coleccion instanceof Map) {
//			map = (Map) coleccion;
//			for (String llave : (Set<String>)map.keySet()) {
//				if (llave.matches(".*?\\b" + llaveBuscar + "\\b.*?")) 
//					return map.get(llave);
//			}
//		} else if (coleccion instanceof Collection) {
//			list = (Collection) coleccion;
//			for (String elemento : (List<String>)list) {
//				if (elemento.matches(".*?\\b" + llaveBuscar + "\\b.*?"))
//					return elemento;
//			}
//		}
//		return null;
//	}
//	
//
//	
//	/*static Object formarEstructuraConsulta(Object things, String campo) {
//		
//		if (things instanceof Map) {
//			
//		}
//		
//		
//		for (Map.Entry<String, JsonElement> entry: entries) {
//			if (new Utilidades().contienePalabra(tabla, entry.getKey())) {
//				jsonObj2 = jsonObj.getAsJsonObject(entry.getKey());
//				for (String filtro : filtros) {
//					
//				}
//			}
//		}
//		
//	}*/
//	
//	
//	/*public static void main(String[] args) throws Exception
//	  {
//	    List keys1 = getKeysFromJson("/Users/juaflore/DieWebsiten/DieWebsitenBD/estructura.json", null);
//	    System.out.println(keys1.size());
//	    System.out.println(keys1);
//
//	    System.out.println("====================================================");
//	    
//	    
//	    String estructura = "{\"codigoIPS\": \"\",\"codigoSucursalIPS\": \"\",\"identificacionAfiliado\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"diagnosticosCTC\": [{\"codigo\": \"\",\"tipo\": \"\"}],\"origenSolicitud\": \"\",\"fechaSolicitud\": \"\",\"claseCTC\": \"\",\"fechaIngresoHospitalario\": \"2015-05-09\",\"servicioNoPOS\": {\"codigo\": \"\",\"descripcion\": \"\",\"cantidad\": \"\",\"presentacion\": \"\",\"frecuencia\": \"\",\"diasTratamiento\": \"\",\"marcaComercial\": \"\"},\"objetivosCTC\": [], \"motivoEvaluacion\": \"\", \"otraAlternativa\": \"\",\"riesgoInminente\": \"\",\"descripcionRiesgo\": \"\",\"referenciaBibliografica\": \"\",\"justificacion\": \"\",\"observaciones\": \"\",\"profesionalSalud\": {\"identificacion\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"nombre\": \"\",\"primerApellido\": \"\", \"segundoApellido\": \"\",\"especialidad\": \"\"},\"radicadorCTC\": {\"identificacion\": \"\",\"nombre\": \"\", \"apellido\": \"\"},\"archivosCTC\": [{\"nombre\": \"\",\"contenido\": [],\"tipoDocumental\": \"\"}],\"numeroSolicitud\": \"\"}";
//	    List keys2 = getKeysFromJson(null, estructura);
//	    System.out.println(keys2.size());
//	    System.out.println(keys2);
//	  }*/
//
//	  static List getKeysFromJson(String fileName, String json) throws Exception
//	  {
//		  
//	    Object things;
//	    if (null != fileName)
//	    	things = new Gson().fromJson(new FileReader(fileName), Object.class);
//	    else
//	    	things = new Gson().fromJson(json, Object.class);
//	    
//	    List keys = new ArrayList();
//	    collectAllTheKeys(keys, things);
//	    return keys;
//	  }
//	  
//	  
//
//	  static void collectAllTheKeys(List keys, Object o)
//	  {
//	    Collection values = null;
//	    if (o instanceof Map)
//	    {
//	      Map map = (Map) o;
//	      keys.addAll(map.keySet()); // collect keys at current level in hierarchy
//	      values = map.values();
//	    }
//	    else if (o instanceof Collection)
//	      values = (Collection) o;
//	    else // nothing further to collect keys from
//	      return;
//
//	    for (Object value : values)
//	      collectAllTheKeys(keys, value);
//	  }
	
	
	
	
	
	public static void main(String[] args) throws Exception {
        
        String estructura = "{\"codigoIPS\": \"\",\"codigoSucursalIPS\": \"\",\"identificacionAfiliado\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"diagnosticosCTC\": [{\"codigo\": \"\",\"tipo\": \"\"}],\"origenSolicitud\": \"\",\"fechaSolicitud\": \"\",\"claseCTC\": \"\",\"fechaIngresoHospitalario\": \"2015-05-09\",\"servicioNoPOS\": {\"codigo\": \"\",\"descripcion\": \"\",\"cantidad\": \"\",\"presentacion\": \"\",\"frecuencia\": \"\",\"diasTratamiento\": \"\",\"marcaComercial\": \"\"},\"objetivosCTC\": [], \"motivoEvaluacion\": \"\", \"otraAlternativa\": \"\",\"riesgoInminente\": \"\",\"descripcionRiesgo\": \"\",\"referenciaBibliografica\": \"\",\"justificacion\": \"\",\"observaciones\": \"\",\"profesionalSalud\": {\"identificacion\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"nombre\": \"\",\"primerApellido\": \"\", \"segundoApellido\": \"\",\"especialidad\": \"\"},\"radicadorCTC\": {\"identificacion\": \"\",\"nombre\": \"\", \"apellido\": \"\"},\"archivosCTC\": [{\"nombre\": \"\",\"contenido\": [],\"tipoDocumental\": \"\"}],\"numeroSolicitud\": \"\"}";
        String[] campos = null;
        
        JsonObject estructuraInput = null;
          
        estructuraInput = new JsonParser().parse(estructura).getAsJsonObject();
            
        //campos = "objetivosCTC_2".split("_");
        //campos = "archivosCTC_3_contenido_1".split("_");
        
        insertarValorEnJson(estructuraInput, "archivosCTC_3_contenido_1");
        
        
        
    }


	static void insertarValorEnJson (Object obj, String ruta) throws Exception{
		
		String[] campos = ruta.split("_");
        
		if (campos.length > 1) {
            
            Object p = ((JsonObject) obj).get(campos[0]);
            
            if (null == p)
            	throw new Exception("el campo: " + campos[0] + " no existe dentro de la estructura");
            
            for (int i = 1; i < campos.length; i++) {
                
                String campo = campos[i];
                
                
                //======= ARRAY
                if (campo.substring(0, 1).matches("[0-9]")) {
                	
                    JsonArray a = (JsonArray) p;// Pasar el arreglo a otra referencia                    
                    
                    if (i + 1 == campos.length) {  //====== Es el ultimo elemento de la ruta                              
                        a.add(new JsonPrimitive("FUNCIONA"));
                        
                    } else {//====== No Es el ultimo elemento de la ruta
                        int indice = Integer.parseInt(campo) - 1;
                        
                        
                        if (a.size() == indice) { // El array actual contiene valores?
                        	
                            if (a.get(indice - 1).isJsonObject()) { // El array actual contiene un object
                            	
                            	
                            	// ====== Agregar un object hermano con los mismos atributos
                                a.add(new JsonObject());
                                
                                for(Map.Entry<String, JsonElement> key : a.get(indice - 1).getAsJsonObject().entrySet()) {
                                    a.get(indice).getAsJsonObject().add(key.getKey(), key.getValue());
                                }
                                
                                
                                
                            } else if (a.get(indice - 1).isJsonArray()) { // El array actual contiene un array
                            	
                                a.add(new JsonArray());
                            }
                            
                        }
                        
                        
                        try {
                        	p = a.get(indice); // Pasar el arreglo actual para busqueda en el siguiente nivel
    					} catch (Exception e) {
    						throw new Exception("El índice: " + indice + " no existe dentro del arreglo");
    					}
                        
                        
                    }
                // ======= OBJECT
                } else {
                	
                    JsonObject o = (JsonObject) p;// Pasar el arreglo a otra referencia
                    
                    if (i + 1 == campos.length) {//====== Es el ultimo elemento de la ruta
                        o.get(campo);                               
                        o.addProperty(campo, "FUNCIONA");
                        
                    } else {//====== No Es el ultimo elemento de la ruta
                        p = o.get(campo); // Pasar el objeto actual para busqueda en el siguiente nivel
                        
                        if (null == p)
                        	throw new Exception("el campo: " + campo + " no existe dentro de la estructura");
                    }
                }
                
            }
                   
                
            System.out.println(obj.toString());
            //Gson gson = new GsonBuilder().registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Serializer()).registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Deserializer()).create();
                
                
        }     
            
	}
	
	
	public static void crearJson(List<Row> ResultSet, List<ColumnDefinitions.Definition> columnas, List<String> filtros, List<String> camposIntermedios, List<String> camposConsulta) throws Exception {
		
//		JsonObject jsonObj = new JsonObject();
//		JsonObject aux = new JsonObject();
//		
//		
//		jsonObj.add("1", new JsonObject());
//		
//		//aux = jsonObj;
//		aux.add("ee", new JsonObject());
//		
//		
//		jsonObj.getAsJsonObject("1").add(aux);
//		System.out.println("2 --> " + aux.equals(jsonObj));
//		//filtros.add("d");
		
		String[] s = "1,2,3,4,5".split(",");
				
				
		ConcurrentHashMap map1 = new ConcurrentHashMap();
		
		ConcurrentHashMap aux = new ConcurrentHashMap();
		
		
		if (map1.isEmpty())
			map1.put(s[0], new ConcurrentHashMap());
		
		//s = "2,3,4,5".split(",");
		
		
		encontrar(map1, s);
		
		
	}
	
	static ConcurrentHashMap encontrar (ConcurrentHashMap aux, String[] s) throws Exception {
		
		if (null == aux)
			throw new Exception("La colección donde se va a buscar el elemento no puede ser vacía");
		else if (s.length < 1)
			throw new Exception("La ruta no puede ser vacía");
		
		ConcurrentHashMap map2;
		
		int i = 0;
		for (String ss : s) {
				
			map2 = (ConcurrentHashMap)aux.get(ss);
			System.out.println(s[i] + " map2 00: ---> " + map2);
			if (null != map2) {
				aux = map2;
			} else {
				System.out.println(s[i] + " map2 0: ---> " + map2);
				map2 = new ConcurrentHashMap();
				
				for (;i<s.length;i++) {
					
					System.out.println("------------------------------------");
					
					
					System.out.println(s[i] + " aux 1: ---> " + aux);
					map2.put(s[i], new ConcurrentHashMap());
					
					//System.out.println(s[i] + " map2 1: ---> " + map2);
					aux.putAll(map2);
					System.out.println(s[i] + " aux 2: ---> " + aux);
					//System.out.println(s[i] + " map2 2: ---> " + map2);
					map2 = (ConcurrentHashMap)aux.get(s[i]);
					//System.out.println(s[i] + " map2 3: ---> " + map2);
					System.out.println(s[i] + " aux 3: ---> " + aux);
					
					
					
				}
				
				break;
				
			}
			
			i++;
		}
		System.out.println("aux: " + aux);
		
		throw new Exception("La ruta no fue encontrada");
	}
	
	static void llenar(ConcurrentHashMap aux, String[] s) {
		
		for (String ss : s) {
			if (aux)
			map2.put(ss, new ConcurrentHashMap());
			aux.putAll(map2);
			System.out.println("A: " + map1);
			aux = new ConcurrentHashMap();
		}
		
		System.out.println("B: " + map1);	
		
		 
		
	}
	
	
	
}
	
	


