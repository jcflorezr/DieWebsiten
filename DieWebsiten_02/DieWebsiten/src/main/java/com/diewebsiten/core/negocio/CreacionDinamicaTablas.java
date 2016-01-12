package com.diewebsiten.core.negocio;

import java.io.FileReader;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class CreacionDinamicaTablas {
	
	
	
	// COGER UNA ESTRUCTURA JSON Y CREAR TABLAS A PARTIR DE ESO
	
	
	// !!@ --> PRIMARY KEY (SE VA CREANDO UNA CADENA)
	// !!_ --> CREATE TABLE (SI LA CADENA DE PRIMARY KEYS NO ESTA VACIA, ENTONCES SE CREA LA TABLA INCLUYENDO LOS VALORES DE ESA CADENA COMO PRIMARY KEYS)
	
	/**
	 * if (valor.get() typeof Integer) then COLUMNA TIPO NUMBER
	 * if (valor.get() typeof String) then COLUMNA TIPO VARCHAR
	 * if (valor.get() typeof List) then COLUMNA TIPO LIST<valor.get(0)>
	 */
	
	// EN EL MOMENTO DE CREAR LAS TABLAS SE DEBE GUARDAR LAS RELACIONES ENTRE ELLAS
	// RELACIONES PROPUESTAS:
	// - Padre --> Hija: Si el clustering key de la tabla hija es una llave primaria de la tabla padre
	// - Hermana mayor --> Hermana menor: Si ambas tablas comparten una o mas llaves primarias en el mismo orden y en el mismo nivel, se debe
	//									  establecer en cual de las columnas deben existir los valores primero.
	
	
	
	
//	Para empezar a echar el código
//	
//	entrar a SitiosWeb.. y luego guardar en cada map las tablas hijas.. luego entrar a paginas y despues de crear todas las hijas de paginas se 
//	sigue con usuarios y asi sucesivamente
	
	
	public static void main (String[] args) throws Exception {
		
		//JsonReader estructura = new JsonReader(new FileReader("/Users/juaflore/DieWebsiten/DieWebsitenBD/PruebaCreacionDinamicaTablas.json"));
//		JsonReader estructura = new JsonReader(new FileReader("/Users/juancamiloroman/DieWebsiten/DieWebsitenBD/PruebaCreacionDinamicaTablas.json"));
		
//		JsonObject estructura = new JsonParser().parse(new FileReader("/Users/juaflore/DieWebsiten/DieWebsitenBD/PruebaCreacionDinamicaTablas.json")).getAsJsonObject();
		
//		System.out.println(estructura);
		
		
		System.out.println(Arrays.asList(StringUtils.split("¡_!sitiosweb¡@!sitioweb", "¡")));
		System.out.println(Arrays.asList(StringUtils.split("¡@!transaccion", "¡")));
		
	}
	

}
