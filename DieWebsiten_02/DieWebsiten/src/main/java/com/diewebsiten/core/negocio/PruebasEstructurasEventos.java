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

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PruebasEstructurasEventos {
	
	private static Object objetoFinal;

	public static void main(String[] args) throws Exception {
		
		
		//================================================//
		// esto se debe hacer dinámicamente
		
		//Map<String, List<String>> estructura = new LinkedHashMap<String, List<String>>();
		
		String tabla = "paginas";
		
		List<String> filtros = new ArrayList<String>();
		filtros.add("sitioweb");
		List<String> columnas = new ArrayList<String>();
		columnas.add("pagina");
		
		//================================================//
		
		
		//BufferedReader br = new BufferedReader(new FileReader("/Users/juaflore/DieWebsiten/DieWebsitenBD/estructura.json"));
		BufferedReader br = new BufferedReader(new FileReader("/Users/juancamiloroman/DieWebsiten/DieWebsitenBD/estructura.json"));
		Gson gson = new Gson();
		
		// ========== Obtener la coleccion donde se encuentra la tabla
		Object jsonObj = encontrarObjeto(tabla, gson.fromJson(br, Object.class));
		
		
		Object things = new Object();
		
		
		String ruta = "";
				
				
				
		
		for (String rutaActual : ((Map<String, List<String>>)jsonObj).get("!!rutas")) {
			
			// ========== Obtener la ruta donde se encuentra la tabla
			if (rutaActual.matches(".*?\\b" + tabla + "\\b.*?")) {
				
				List<String> tablasRuta = new ArrayList<String>(Arrays.asList((StringUtils.substringBefore(rutaActual, tabla) + tabla).split("@")));
				
				// ========== obtener la coleccion para la ruta
				int i = 0;
				for (String tablaActual : tablasRuta) {
					
					jsonObj = (encontrarObjeto(tablaActual, jsonObj));
					if (i == tablasRuta.size()) {
						things = encontrarObjeto(tablaActual, jsonObj);
						System.out.println("=============================");
						System.out.println(things);
					} else {
						things = new Map().put(tablaActual, "");
					}
					
					
					REVISAR COMO SE PUEDE RETORNAR UN JSON DESDE CAMPOS INTERMEDIOS (CREAR NUEVA COLUMNA EN LA TABLA EVENTOS)
					
					
					i++;
					
				}
				
				//ruta = rutaActual;
				
				break;
			}
		}
		
		
		
		/*for (String estruc : (Set<String>)estructura.keySet()) {
			Object estrucActual = estructura.get(estruc);
			if (estrucActual instanceof String)
				formarEstructuraConsulta(things, estrucActual);
			else {
				for (String clausula : (List<String>)estrucActual) {
					formarEstructuraConsulta(things, clausula);
				}
			}
			
		}*/
		
		
		
		
		//JsonObject jsonObj2;
		//System.out.println(((Map)jsonObj).keySet());
		//Set<Map.Entry<String, JsonElement>> entries = jsonObj.entrySet();
		
		//Object things = new Object();
		

	}
	
	
	
	// RETORNAR OBJETOS BUSCADOS
	static Object encontrarObjeto(String llaveBuscar, Object coleccion) {
		
		Map map;
		Collection list;
		
		if (coleccion instanceof Map) {
			map = (Map) coleccion;
			for (String llave : (Set<String>)map.keySet()) {
				if (llave.matches(".*?\\b" + llaveBuscar + "\\b.*?")) 
					return map.get(llave);
			}
		} else if (coleccion instanceof Collection) {
			list = (Collection) coleccion;
			for (String elemento : (List<String>)list) {
				if (elemento.matches(".*?\\b" + llaveBuscar + "\\b.*?"))
					return elemento;
			}
		}
		return null;
	}
	

	
	/*static Object formarEstructuraConsulta(Object things, String campo) {
		
		if (things instanceof Map) {
			
		}
		
		
		for (Map.Entry<String, JsonElement> entry: entries) {
			if (new Utilidades().contienePalabra(tabla, entry.getKey())) {
				jsonObj2 = jsonObj.getAsJsonObject(entry.getKey());
				for (String filtro : filtros) {
					
				}
			}
		}
		
	}*/
	
	
	/*public static void main(String[] args) throws Exception
	  {
	    List keys1 = getKeysFromJson("/Users/juaflore/DieWebsiten/DieWebsitenBD/estructura.json", null);
	    System.out.println(keys1.size());
	    System.out.println(keys1);

	    System.out.println("====================================================");
	    
	    
	    String estructura = "{\"codigoIPS\": \"\",\"codigoSucursalIPS\": \"\",\"identificacionAfiliado\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"diagnosticosCTC\": [{\"codigo\": \"\",\"tipo\": \"\"}],\"origenSolicitud\": \"\",\"fechaSolicitud\": \"\",\"claseCTC\": \"\",\"fechaIngresoHospitalario\": \"2015-05-09\",\"servicioNoPOS\": {\"codigo\": \"\",\"descripcion\": \"\",\"cantidad\": \"\",\"presentacion\": \"\",\"frecuencia\": \"\",\"diasTratamiento\": \"\",\"marcaComercial\": \"\"},\"objetivosCTC\": [], \"motivoEvaluacion\": \"\", \"otraAlternativa\": \"\",\"riesgoInminente\": \"\",\"descripcionRiesgo\": \"\",\"referenciaBibliografica\": \"\",\"justificacion\": \"\",\"observaciones\": \"\",\"profesionalSalud\": {\"identificacion\": {\"tipoIdentificacion\": \"\",\"numeroIdentificacion\": \"\"},\"nombre\": \"\",\"primerApellido\": \"\", \"segundoApellido\": \"\",\"especialidad\": \"\"},\"radicadorCTC\": {\"identificacion\": \"\",\"nombre\": \"\", \"apellido\": \"\"},\"archivosCTC\": [{\"nombre\": \"\",\"contenido\": [],\"tipoDocumental\": \"\"}],\"numeroSolicitud\": \"\"}";
	    List keys2 = getKeysFromJson(null, estructura);
	    System.out.println(keys2.size());
	    System.out.println(keys2);
	  }*/

	  static List getKeysFromJson(String fileName, String json) throws Exception
	  {
		  
	    Object things;
	    if (null != fileName)
	    	things = new Gson().fromJson(new FileReader(fileName), Object.class);
	    else
	    	things = new Gson().fromJson(json, Object.class);
	    
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
