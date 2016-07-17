
package com.diewebsiten.core.util;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.BiFunction;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.text.WordUtils;

import com.google.gson.JsonObject;

/**
 *
 * @author juancamiloroman
 */
public class Transformaciones {


    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TypeFactory typeFactory = mapper.getTypeFactory();


    public static <T> List<T> stringToList(String stringAConvertir, Class<T> tipoDeLista) {
        try {
            return mapper.readValue(stringAConvertir, typeFactory.constructCollectionType(List.class, tipoDeLista));
        } catch (IOException e) {
            throw new ExcepcionGenerica("no se pudo serializar el String: " + stringAConvertir + " a una lista de tipo: " + tipoDeLista);
        }
    }
    
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
    
}
