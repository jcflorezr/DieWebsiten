package com.diewebsiten.core.negocio;

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
	
	
	
	
	Para empezar a echar el código
	
	entrar a SitiosWeb.. y luego guardar en cada map las tablas hijas.. luego entrar a paginas y despues de crear todas las hijas de paginas se 
	sigue con usuarios y asi sucesivamente
	

}
