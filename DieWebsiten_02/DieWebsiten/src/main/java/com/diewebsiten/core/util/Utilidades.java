
package com.diewebsiten.core.util;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Row;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author juancamiloroman
 */
public class Utilidades {
	
	
	//private ConcurrentHashMap resultadoEvento = new ConcurrentHashMap();
    
    /**
     * 
     * @param objeto
     * @return 
     */
    public boolean esVacio(Object objeto) {
        
        boolean vacio = true;
        
        if (null != objeto && !objeto.toString().isEmpty() && !objeto.toString().equals("")) {
            vacio = false;
        }
        
        return vacio;
        
    } // esVacio
        
    
    /**
     * 
     * @param cadena
     * @return 
     */
    public boolean esJSON(String cadena) {
        try {
        	new JsonParser().parse(cadena);
        	return true;
		} catch (JsonSyntaxException e) {
			return false;
		}            
        
    }// esJSON
    
    /**
     * 
     * @param palabra
     * @param cadena
     * @return 
     */
    public boolean contienePalabra (String palabra, String cadena) {
        return cadena.matches(".*?\\b" + palabra + "\\b.*?");
    }// contienePalabra
    
    /**
     * 
     * @param cadena
     * @return 
     */
    public String encriptarString(String cadena) throws Exception {
                  
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(cadena.getBytes());

        byte[] byteContrasena = md.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteContrasena.length; i++) {
            sb.append(Integer.toString((byteContrasena[i] & 0xff) + 0x100, 16).substring(1));                                
        }

        cadena = sb.toString();
        
        return cadena;
        
    }// encriptarString
    
    
    


    @SuppressWarnings("fallthrough")
    public List<String> validarParametro(String nombreValidacion, Object parametro) throws Exception {
        Thread.sleep(1000);
        List<String> resultadoValidacion = new ArrayList<String>();
        
       if (esVacio(nombreValidacion))
           throw new Exception("El nombre de la validación ha llegado nulo.");

       if (esVacio(parametro) && !Constantes.V_OPCIONAL.equals(nombreValidacion)) {
           resultadoValidacion.add("Campo obligatorio");
           return resultadoValidacion;
       }
       
       switch(Constantes.valueOf(nombreValidacion)) {
       
           case V_ALFANUMERICO_CON_ESPACIOS:            
               // Validar que el campo actual sea alfanumérico y sin espacios en blanco.
               if (!StringUtils.isAlphanumeric(parametro.toString()))
                   resultadoValidacion.add("Campo alfanumerico sin espacios en blanco. Ejemplo: JuaN123");
           break;        
           case V_ALFANUMERICO_SIN_ESPACIOS:                
               // Validar que el campo actual sea alfanumérico y con espacios en blanco.
               if (!StringUtils.isAlphanumericSpace(parametro.toString()))
                   resultadoValidacion.add("Campo alfanumerico con posibles espacios en blanco. Ejemplo: JuaN 123 456");
           break;        
           case V_NUMERICO_SIN_ESPACIOS:                
               // Validar que el campo actual sea numérico y sin espacios en blanco.
               if (!StringUtils.isNumeric(parametro.toString()))
                   resultadoValidacion.add("Campo numérico sin espacios en blanco. Ejemplo: 123456");
           break;                
           case V_NUMERICO_CON_ESPACIOS:                
               // Validar que el campo actual sea numérico y con espacios en blanco.
               if (!StringUtils.isNumericSpace(parametro.toString()))
                   resultadoValidacion.add("Campo numérico con posibles espacios en blanco. Ejemplo: 123 456 789");
           break;        
           case V_CARACTER_SIN_ESPACIOS:                
               // Validar que el campo actual sea de caracteres y sin espacios en blanco.
               if (!StringUtils.isAlpha(parametro.toString()))
                   resultadoValidacion.add("Campo caracter sin espacios en blanco. Ejemplo: abcDEF");
           break;                    
           case V_CARACTER_CON_ESPACIOS:                
               // Validar que el campo actual sea de caracteres y sin espacios en blanco.
               if (!StringUtils.isAlphaSpace(parametro.toString())) 
                   resultadoValidacion.add("Campo caracter con posibles espacios en blanco. Ejemplo: abc DEF hg");
           break;        
           case V_EMAIL:                
               // Validar que el campo actual sea una dirección de correo electrónico válida.
               if (!EmailValidator.getInstance().isValid(parametro.toString()))
                   resultadoValidacion.add("Dirección de correo electrónico no válida");
           break;                    
           case V_FECHAHORA:                
               // Validar que el campo actual sea una cadena con formato fecha y hora.
               if (!DateValidator.getInstance().isValid(parametro.toString(), "yyyy-MM-dd HH:mm:ss"))
                   resultadoValidacion.add("Formato de fecha y hora no válido. Formato esperado: aaaa-MM-dd HH:mm:ss");
           break;                    
           case V_URL:                
               // Validar que el campo actual sea una dirección url.
               if (!UrlValidator.getInstance().isValid(parametro.toString()))
                   resultadoValidacion.add("Dirección url no válida");
           break;            
           case V_DOMINIO:                
               // Validar que el campo actual sea un nombre de un dominio de un sitio web.
               if (!parametro.toString().matches("[a-z0-9._-]+"))
                   resultadoValidacion.add("El campo sólo acepta números (0-9), letras en minúscula (a-z), puntos (.) o guiones (_-)");
           break;
           case V_PUNTO:                
               // Validar que el campo actual contenga el caracter punto (.)
               if (!parametro.toString().matches("[.]+"))
                   resultadoValidacion.add("El campo sólo debe tener números (0-9) y puntos (.). Ejemplo: 1.5.8");
           break;
           default:
               throw new Exception("La validación '" + nombreValidacion + "' no existe.");
       
       }
       
       if (!resultadoValidacion.isEmpty()) 
           resultadoValidacion.add(parametro.toString());
           
       return resultadoValidacion;
        
    }    
    
    public Object transformarParametro (String nombreTransformacion, Object parametro) throws Exception {
        Thread.sleep(1000);
        if (esVacio(parametro) || esVacio(nombreTransformacion))
            throw new Exception("No se puede hacer una validacion con valores nulos. Nombre Validación: " + nombreTransformacion + ". Parámetro : " + parametro);
                
        switch(Constantes.valueOf(nombreTransformacion)) {
        
            case T_EMAIL:        
                // Si el campo es de tipo email se divide en dos campos.
                // Ej: email@dominio.com --> {usuario: email, dominio: dominio.com}
            	JsonObject transformacion = new JsonObject();
                transformacion.addProperty("usuario", StringUtils.substringBefore(parametro.toString(), "@"));
                transformacion.addProperty("dominio", StringUtils.substringAfter(parametro.toString(), "@"));
                return transformacion.toString();           
            case T_CIFRADO:            
                // Si el campo necesita cifrarse se transforma a una cadena de caracteres base 64. 
                // Ej: {password: juan} --> {password: ed08c290d7e22f7bb324b15cbadce35b0b348564fd2d5f95752388d86d71bcca}
                return encriptarString(parametro.toString());       
            case T_FECHAHORA:                
                // Si el campo es de tipo "FechaHora" se transforma a un java.util.Date
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parametro.toString());        
            case T_MINUSCULAS:                
                // Si el campo es de tipo "Lower" se transforman todos sus caracteres a minúsculas
                return StringUtils.lowerCase(parametro.toString());                
            case T_MAYUSCULAS :                
                // Si el campo es de tipo "Upper" se transforman todos sus caracteres a mayúsculas
                return StringUtils.upperCase(parametro.toString());       
            case T_IDIOMA :                
                /***************************************************************************/
                /*************************** LOGICA PARA LOS CAMPOS TIPO IDIOMA *****************************/
                /*************************** Español --> ES *****************************/                   
            case T_CAMELCASE_CLASE :
                // string que se convertira en camel case clase --> StringQueSeConvertiraEnCamelCaseClase
                if (parametro.toString().matches("^\\s*$"))                    
                    return StringUtils.deleteWhitespace(WordUtils.capitalizeFully(parametro.toString()));
            break;        
            case T_CAMELCASE_METODO :                
                // string que se convertira en camel case metodo --> stringQueSeConvertiraEnCamelCaseMetodo
                //if (parametro.toString().matches("^\\s*$")) 
                    return StringUtils.uncapitalize(StringUtils.deleteWhitespace(WordUtils.capitalizeFully(parametro.toString())));
            //break;                    
            case T_GUIONBAJO :                
                /***************************************************************************/
                /*************************** LOGICA PARA LOS CAMPOS TIPO GuionBajo *****************************/
                /*************************** nueva cadena con guiones bajos --> nueva_cadena_con_guiones_bajos *****************************/
            break;
            default:
                throw new Exception("La transformación '" + nombreTransformacion + "' no existe.");
        
        }
        
        return null;
        
    }// transformarParametro
    
}
