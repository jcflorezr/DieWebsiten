
package com.diewebsiten.core.util;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.*;

public class Transformaciones {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeFactory TYPE_FACTORY = MAPPER.getTypeFactory();

    public static ObjectNode newJsonObject() {
        return MAPPER.createObjectNode();
    }

    public static <T> List<T> stringToList(String listString, Class<T> tipoDeLista) {
        try {
            return MAPPER.readValue(listString, TYPE_FACTORY.constructCollectionType(List.class, tipoDeLista));
        } catch (IOException e) {
            throw new ExcepcionGenerica("no se pudo deserializar el String a una lista de tipo: " + tipoDeLista + ". MOTIVO: " + e.getMessage());
        }
    }

    public static <T> List<T> objectToList(Object objeto, Class<T> tipoDeLista) {
        return MAPPER.convertValue(objeto, TYPE_FACTORY.constructCollectionType(List.class, tipoDeLista));
    }

    public static ObjectNode stringToJsonObject(String jsonString) {
        try {
            return (ObjectNode) MAPPER.readTree(jsonString);
        } catch (IOException e) {
            throw new ExcepcionGenerica("no se pudo convertir el String: " + jsonString + " a una JsonObject. MOTIVO: " + e.getMessage());
        }
    }

    public static <T> ArrayNode listToJsonArray(List<T> lista) {
        return MAPPER.convertValue(lista, ArrayNode.class);
    }

    public static <T> T jsonToObject(File json, Class<T> tipoDeObjeto) {
        try {
            return MAPPER.readValue(json, tipoDeObjeto);
        } catch (IOException e) {
            throw new ExcepcionGenerica("no se pudo deserializar el String: " + json.getPath() + " a un objeto de tipo: " + tipoDeObjeto
                    + ". MOTIVO: " + e.getMessage());
        }
    }

    public static <T> T jsonToObject(JsonNode json, Class<T> tipoDeObjeto) {
        try {
            return MAPPER.treeToValue(json, tipoDeObjeto);
        } catch (JsonProcessingException e) {
            throw new ExcepcionGenerica("no se pudo deserializar el String: " + json.toString() + " a un objeto de tipo: " + tipoDeObjeto
                    + ". MOTIVO: " + e.getMessage());
        }
    }

    public static <K, V> Map<K, V> jsonToMap(JsonNode json, Class<K> tipoDeLlave, Class<V> tipoDeValor) {
        try {
            return MAPPER.readValue(json.toString(), TYPE_FACTORY.constructMapType(Map.class, tipoDeLlave, tipoDeValor));
        } catch (IOException e) {
            throw new ExcepcionGenerica("no se pudo deserializar el String: " + json.toString() + " a un map de tipo: " + tipoDeLlave + "," + tipoDeValor
            + ". MOTIVO: " + e.getMessage());
        }
    }

    public static JsonNode objectToValue(Object objeto) {
        return MAPPER.valueToTree(objeto);
    }

    public static ObjectNode ponerObjeto (ObjectNode coleccion, String nombrePropiedad) {
        return (ObjectNode) Optional.ofNullable(coleccion.get(nombrePropiedad))
                .orElseGet(() -> coleccion.putObject(nombrePropiedad));
    }

    public static void agruparValores(ObjectNode coleccion, String nombrePropiedad, JsonNode valorPropiedad) {
        JsonNode valorExistente = coleccion.get(nombrePropiedad);
        if (valorExistente != null) {
            if (valorExistente.isArray()) {
                ((ArrayNode)valorExistente).add(valorPropiedad);
            } else {
                coleccion.set(nombrePropiedad, MAPPER.createArrayNode().add(valorExistente).add(valorPropiedad));
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

    public static String removerCaracteres(String cadena, String... caracteres) {
        for (String caracter : caracteres) {
            cadena = remove(cadena, caracter);
        }
        return cadena;
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
