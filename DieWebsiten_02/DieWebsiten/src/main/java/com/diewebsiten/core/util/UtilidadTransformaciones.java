
package com.diewebsiten.core.util;

import static org.apache.commons.lang3.StringUtils.*;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.text.WordUtils;

import com.google.gson.JsonObject;

/**
 *
 * @author juancamiloroman
 */
public class UtilidadTransformaciones {
    
    
    /**
     * Cifrar el valor a una cadena de caracteres base 64.
     * Ej: juan --> ed08c290d7e22f7bb324b15cbadce35b0b348564fd2d5f95752388d86d71bcca
     * @param valor
     * @return 
     */
    public static String encriptarCadena(String valor) throws Exception {
                  
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(valor.getBytes());

        byte[] byteContrasena = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteContrasena.length; i++) {
            sb.append(Integer.toString((byteContrasena[i] & 0xff) + 0x100, 16).substring(1));                                
        }

        valor = sb.toString();
        
        return valor;
        
    }
    
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
    
    /**
     * Transformar un valor a tipo Fecha y Hora con el siguiente formato: yyyy-MM-dd HH:mm:ss
     * @param valor
     * @return
     * @throws ParseException
     */
    public static String trasformarFechaHora(Object valor) throws ParseException {
    	return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(valor.toString()).toString();
    }
    
    /**
     * Transformar todos los caracteres a minúsculas
     * Ej: VaLoR --> valor
     * @param valor
     * @return
     */
    public static String minimizar(String valor) {
    	return lowerCase(valor); 
    }
    
    /**
     * Transformar todos los caracteres a mayúsculas
     * Ej: VaLoR --> VALOR
     * @param valor
     * @return
     */
    public static String maximizar(String valor) {
    	return upperCase(valor); 
    }
    
    /**
     * Transformar valor a Camel Case de tipo clase
     * Ej: valor que se convertirá en camel case tipo clase --> ValorQueSeConvertiraEnCamelCaseTipoClase
     * @param valor
     * @return
     */
    public static String transformarCamelCaseTipoClase(String valor) {
    	if (valor.toString().matches("^\\s*$")) {                 
            return deleteWhitespace(WordUtils.capitalizeFully(valor.toString()));
    	} else {
    		return capitalize(valor);
    	}
    }
    
    /**
     * Transformar valor a Camel Case de tipo método
     * Ej: valor que se convertira en camel case tipo metodo --> stringQueSeConvertiraEnCamelCaseTipoMetodo
     * @param parametro
     * @return
     */
    public static String transformarCamelCaseTipoMetodo(String parametro) {
    	return uncapitalize(deleteWhitespace(WordUtils.capitalizeFully(parametro)));
    }
    
    
    /**
     * Fachada que cumple con la funcion de ejecutar cualquiera de las transformaciones que se
     * implementan en esta clase
     * 
     * 
     * AGREGAR LA LISTA DE NOMBRES DE TRANSFORMACIONES
     * 
     * @param nombreValidacion validacion que se desea ejecutar
     * @param valor valor a validar
     * @return Si la validación no fue exitosa se retorna un mensaje de validación. Si la validación
     * fue exitosa se retorna el mismo valor recibido en el parámetro @valor 
     * @throws Exception
     */
    public Object transformarParametro (String nombreTransformacion, Object valor) throws Exception {
        //Thread.sleep(1000);
        if (UtilidadValidaciones.esVacio(valor) || UtilidadValidaciones.esVacio(nombreTransformacion)) {
            throw new Exception("No se puede hacer una validacion con valores nulos. Nombre Validación: " + nombreTransformacion + ". Parámetro : " + valor);
        }
                
        switch(Constantes.valueOf(nombreTransformacion)) {
        
            case T_EMAIL:        
                transformarEmailCassandra((String) valor);
            case T_CIFRADO:
                return encriptarCadena((String) valor);       
            case T_FECHAHORA:                
            	trasformarFechaHora((String) valor);
            case T_MINUSCULAS:
                return minimizar((String) valor);                
            case T_MAYUSCULAS :
                return maximizar((String) valor);       
            case T_IDIOMA :                
                /***************************************************************************/
                /*************************** LOGICA PARA LOS CAMPOS TIPO IDIOMA *****************************/
                /*************************** Español --> ES *****************************/ 
            	return null;
            case T_CAMELCASE_CLASE :
                return transformarCamelCaseTipoClase((String) valor);
            case T_CAMELCASE_METODO :                
                return transformarCamelCaseTipoMetodo((String) valor);
            case T_GUIONBAJO :                
                /***************************************************************************/
                /*************************** LOGICA PARA LOS CAMPOS TIPO GuionBajo *****************************/
                /*************************** nueva cadena con guiones bajos --> nueva_cadena_con_guiones_bajos *****************************/
            	return null;
            default:
                throw new Exception("La transformación '" + nombreTransformacion + "' no existe.");
        
        }
        
    }
    
}
