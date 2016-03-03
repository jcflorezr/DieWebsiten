
package com.diewebsiten.core.util;

import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.isAlphaSpace;
import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isAlphanumericSpace;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.isNumericSpace;

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
    
    
    /**
     * Fachada que cumple con la funcion de ejecutar cualquiera de las validaciones que se
     * implementan en esta clase
     * 
     * AGREGAR LA LISTA DE NOMBRES DE VALIDACIONES
     * 
     * @param nombreValidacion validacion que se desea ejecutar
     * @param valor valor a validar
     * @return Si la validación no fue exitosa se retorna un mensaje de validación. Si la validación
     * fue exitosa se retorna el mismo valor recibido en el parámetro @valor 
     * @throws Exception
     */
    public String validarParametro(String nombreValidacion, Object valor) throws Exception {
        
       if (esVacio(nombreValidacion)) {
           throw new Exception("El nombre de la validación ha llegado nulo.");
       }

       if (esVacio(valor) && !Constantes.V_OPCIONAL.equals(nombreValidacion)) {
           return "Campo obligatorio";
       }
       
       switch(Constantes.valueOf(nombreValidacion)) {
       
           case V_ALFANUMERICO_CON_ESPACIOS:
               if (!esAlfanumerico((String) valor)) {
            	   return "Campo alfanumerico sin espacios en blanco. Ejemplo: JuaN123";
               }
           break;        
           case V_ALFANUMERICO_SIN_ESPACIOS:
               if (!esAlfanumericoConEspacios((String) valor)) {
            	   return "Campo alfanumerico con posibles espacios en blanco. Ejemplo: JuaN 123 456";
               }
           break;        
           case V_NUMERICO_SIN_ESPACIOS:
               if (!esNumerico((String) valor)) {
            	   return "Campo numérico sin espacios en blanco. Ejemplo: 123456";
               }
           break;                
           case V_NUMERICO_CON_ESPACIOS:
               if (!esNumericoConEspacios((String) valor)) {
            	   return "Campo numérico con posibles espacios en blanco. Ejemplo: 123 456 789";
               }
           break;        
           case V_CARACTER_SIN_ESPACIOS:
               if (!esCaracter((String) valor)) {
            	   return "Campo caracter sin espacios en blanco. Ejemplo: abcDEF";
               }
           break;                    
           case V_CARACTER_CON_ESPACIOS:
               if (!esCaracterConEspacios((String) valor)) {
            	   return "Campo caracter con posibles espacios en blanco. Ejemplo: abc DEF hg";
               }
           break;        
           case V_EMAIL:
               if (!esEmailValido((String) valor)) {
            	   return "Dirección de correo electrónico no válida";
               }
           break;                    
           case V_FECHAHORA:
               if (!esFechaHora((String) valor)) {
            	   return "Formato de fecha y hora no válido. Formato esperado: aaaa-MM-dd HH:mm:ss";
               }
           break;                    
           case V_URL:
               if (!esDireccionUrl((String) valor)) {
            	   return "Dirección url no válida";
               }
           break;            
           case V_DOMINIO:
               if (!esDominioSitioWeb((String) valor)) {
            	   return "El campo sólo acepta números (0-9), letras en minúscula (a-z), puntos (.) o guiones (_-)";
               }
           break;
           case V_PUNTO:
               if (!esNumericoConPuntos((String) valor)) {
            	   return "El campo sólo debe tener números (0-9) y puntos. Ejemplo: 1.5.8";
               }
           break;
           default:
               throw new Exception("La validación '" + nombreValidacion + "' no existe.");
       
       }
       
       return (String) valor;
        
    }
    
}
