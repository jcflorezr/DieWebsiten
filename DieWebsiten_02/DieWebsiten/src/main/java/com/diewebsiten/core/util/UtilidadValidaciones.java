
package com.diewebsiten.core.util;

import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.isAlphaSpace;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isAlphanumericSpace;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.isNumericSpace;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author juancamiloroman
 */
public class UtilidadValidaciones {
	

	/**
     * 
     * @param objeto
     * @return 
     */
    public static boolean esVacio(Object objeto) {
        
        boolean vacio = true;
        
        if (null != objeto && !objeto.toString().isEmpty() && !objeto.toString().equals("")) {
            vacio = false;
        }
        
        return vacio;
        
    } // esVacio
        
    
    /**
     * 
     * @param valor
     * @return 
     */
    public static boolean esJSON(String valor) {
        try {
        	new JsonParser().parse(valor);
        	return true;
		} catch (JsonSyntaxException e) {
			return false;
		}            
        
    }// esJSON
    
    /**
     * 
     * @param palabra
     * @param valor
     * @return 
     */
    public static boolean contienePalabra (String palabra, String valor) {
        return valor.matches(".*?\\b" + palabra + "\\b.*?");
    }// contienePalabra
    
    /**
     * Validar que el valor sea alfanumérico y sin espacios en blanco.
     * @param valor
     * @return
     */
    public static boolean esAlfanumerico(String valor) {
    	return isAlphanumeric(valor);
    }
    
    /**
     * Validar que el valor sea alfanumérico y con espacios en blanco.
     * @param valor
     * @return
     */
    public static boolean esAlfanumericoConEspacios(String valor) {
    	return isAlphanumericSpace(valor);
    }
    
    /**
     * Validar que el valor sea numérico y sin espacios en blanco.
     * @param valor
     * @return
     */
    public static boolean esNumerico(String valor) {
    	return isNumeric(valor);
    }
    
    /**
     * Validar que el valor sea numérico y con espacios en blanco.
     * @param valor
     * @return
     */
    public static boolean esNumericoConEspacios(String valor) {
    	return isNumericSpace(valor);
    }
    
    /**
     * Validar que el valor sea de caracteres y sin espacios en blanco.
     * @param valor
     * @return
     */
    public static boolean esCaracter(String valor) {
    	return isAlpha(valor);
    }
    
    /**
     * Validar que el valor sea de caracteres y sin espacios en blanco.
     * @param valor
     * @return
     */
    public static boolean esCaracterConEspacios(String valor) {
    	return isAlphaSpace(valor);
    }
    
    /**
     * Validar que el valor sea una dirección de correo electrónico válida.
     * @param valor
     * @return
     */
    public static boolean esEmailValido(String valor) {
    	return EmailValidator.getInstance().isValid(valor);
    }
    
    /**
     * Validar que el valor sea una cadena con formato fecha y hora.
     * @param valor
     * @return
     */
    public static boolean esFechaHora(String valor) {
    	return DateValidator.getInstance().isValid(valor, "yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * Validar que el valor sea una dirección url.
     * @param valor
     * @return
     */
    public static boolean esDireccionUrl(String valor) {
    	return UrlValidator.getInstance().isValid(valor);
    }
    
    /**
     * Validar que el valor sea un nombre de un dominio de un sitio web.
     * @param valor
     * @return
     */
    public static boolean esDominioSitioWeb(String valor) {
    	return valor.matches("[a-z0-9._-]+");
    }
    
    /**
     * Validar que el valor sea numérico y con puntos. Ejemplo: 1.5.8
     * @param parametro
     * @return
     */
    public static boolean esNumericoConPuntos(String parametro) {
    	return parametro.matches("[.]+");
    }
    
}
