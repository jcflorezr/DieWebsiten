package com.diewebsiten.core.almacenamiento.cassandra.util;

import com.google.gson.JsonObject;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class UtilidadCassandra {

	/**
	 * Dividir el valor con formato email en dos campos.
	 * Ej: email@dominio.com --> {"usuario": "email", "dominio": "dominio.com"}
	 * @param valor
	 * @return
	 */
	public static String transformarEmailCassandra(String valor) {
		JsonObject transformacion = new JsonObject();
		transformacion.addProperty("usuario", substringBefore(valor, "@"));
		transformacion.addProperty("dominio", substringAfter(valor, "@"));
		return transformacion.toString();
	}
	
	
	/*java.lang.String
    			java.lang.Long
    			java.nio.ByteBuffer
    			java.lang.Boolean
    			java.lang.Double
    			java.lang.Float
    			java.lang.Integer
    			java.lang.Long
    			java.math.BigDecimal
    			java.net.InetAddress
    			java.lang.String
    			java.util.Date
    			java.util.UUID
    			java.util.UUID
    			java.lang.String
    			java.math.BigInteger*/
	
	public static boolean validarTipoColumna(String tipoColumna) {
		
		try {
			Class.forName(tipoColumna);
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}

}
