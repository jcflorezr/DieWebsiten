
package com.diewebsiten.core.util;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            throw new ExcepcionGenerica("no se pudo deserializar el String: " + stringAConvertir + " a una lista de tipo: " + tipoDeLista);
        }
    }

    public static <K, V> Map<K, V> jsonToMap(JsonNode jsonAConvertir, Class<K> tipoDeLlave, Class<V> tipoDeValor) {
        try {
            return mapper.readValue(jsonAConvertir.toString(), typeFactory.constructMapType(Map.class, tipoDeLlave, tipoDeValor));
        } catch (IOException e) {
            throw new ExcepcionGenerica("no se pudo deserializar el String: " + jsonAConvertir.toString() + " a un map de tipo: " + tipoDeLlave + "," + tipoDeValor
            + "MOTIVO: " + e.getMessage());
        }
    }

    public static BiFunction<ObjectNode, String, ObjectNode> ponerObjeto = (coleccion, nombrePropiedad) ->
            (ObjectNode) Optional.ofNullable(coleccion.get(nombrePropiedad))
            .orElseGet(() -> coleccion.putObject(nombrePropiedad));

    public static void agruparValores(ObjectNode coleccion, String nombrePropiedad, JsonNode valorPropiedad) {
        JsonNode valorExistente = coleccion.get(nombrePropiedad);
        if (valorExistente != null) {
            if (valorExistente.isArray()) {
                ((ArrayNode)valorExistente).add(valorPropiedad);
            } else {
                coleccion.set(nombrePropiedad, mapper.createArrayNode().add(valorExistente).add(valorPropiedad));
            }
        } else {
            coleccion.set(nombrePropiedad, valorPropiedad);
        }
    }
    
    /**
     * Cifrar el valor a una cadena de caracteres base 64.
     * Ej: juan --> ed08c290d7e22f7bb324b15cbadce35b0b348564fd2d5f95752388d86d71bcca
     * @param valor
     * @return 
     */
    public static String encriptarCadena(String valor) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ExcepcionGenerica(e);
        }
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
     * Transformar un valor a tipo Fecha y Hora con el siguiente formato: yyyy-MM-dd HH:mm:ss
     * @param valor
     * @return
     * @throws ParseException
     */
    public static String trasformarFechaHora(Object valor) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(valor.toString()).toString();
        } catch (ParseException e) {
            throw new ExcepcionGenerica(e);
        }
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
