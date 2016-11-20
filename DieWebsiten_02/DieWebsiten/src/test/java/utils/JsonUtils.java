package utils;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;

import static com.diewebsiten.core.util.Transformaciones.jsonToObject;

public class JsonUtils {

    public JsonNode obtenerJsonDesdeArchivo(String nombreArchivo) {
        String ruta = "";
        try {
            ruta = getClass().getClassLoader().getResource(nombreArchivo).getPath();
            File archivo = new File(ruta);
            return jsonToObject(archivo, JsonNode.class);
        } catch (Exception e) {
            throw new ExcepcionGenerica("Error al procesar el archivo '" + nombreArchivo + "' en la ruta " + ruta + ". MOTIVO: " + e.getMessage());
        }
    }

}
