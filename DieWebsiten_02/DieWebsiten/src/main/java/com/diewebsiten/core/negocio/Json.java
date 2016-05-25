package com.diewebsiten.core.negocio;

import static org.apache.commons.lang3.StringUtils.*;

import java.util.Arrays;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * 
 * Llave --> ¡__!grupos_validaciones¡-@!¿V?grupovalidacion
 * prefijos --> ¡__!  ¡-@!  ¿V?
 * delimitadores para los prefijos de tablas o columnas --> ¡!
 * delimitadores para los prefijos de tipos de columna --> ¿?
 * 
 * 
 * 
 * @author juancamiloroman
 *
 */
public class Json {

	private JsonObject CQLs = new JsonObject();
	private JsonObject primaryKeys = new JsonObject();
	
	private static final String TABLA = "__";
	private static final String TABLA_SISTEMA = "_#";
	private static final String PARTITION_KEY = "-@";
	private static final String COLUMNA_RECUPERADA = "-$";
	private static final String COLUMNA_POBLADA_DESDE_SISTEMA = "-&";
	private static final String COLUMNA_REGULAR = "-*";
	
	private static final String DELIMITADOR_INICIO_PREFIJO_TABLA_O_COLUMNA = "¡";
	private static final String DELIMITADOR_FIN_PREFIJO_TABLA_O_COLUMNA = "!";
	private static final String DELIMITADOR_INICIO_PREFIJO_TIPO_DE_COLUMNA = "¿";
	private static final String DELIMITADOR_FIN_PREFIJO_TIPO_DE_COLUMNA = "?";
	
	private String tablaActual = "";
	
	JsonObject json;
	JsonElement jsonElement;
	
	public static void main(String[] args) throws Exception {
		
		Class.forName("java.util.List");
		
		String jsonObjectString = "{\n" +
                "  \"¡__!sitiosweb¡-@!¿V?sitioweb\": {\n" +
                "    \"¡-*!¿V?nombre\": \"\",\n" +
                "    \"¡-*!¿V?descripcion\": \"\",\n" +
                "    \"¡-$!¿L<V>?keyspaces\": [\n" +
                "      \"system.schema_keyspaces.keyspace_name\"\n" +
                "    ],\n" +
                "    \"¡__!paginas¡-@!¿V?pagina\": {\n" +
                "      \"¡-*!¿V?nombre\": \"\",\n" +
                "      \"¡-*!¿V?descripcion\": \"\",\n" +
                "      \"¡__!eventos¡-@!¿V?evento\": {\n" +
                "        \"¡-@!¿V?transaccion\": {\n" +
                "          \"¡-*!¿V?descripcion\": \"\",\n" +
                "          \"¡-*!¿V?tipo\": \"\",\n" +
                "          \"¡-$!¿V?keyspace_name\": \"diewebsiten.sitiosweb.keyspaces\",\n" +
                "          \"¡-$!¿V?column_family\": \"columnas.columnfamily_name\",\n" +
                "          \"¡-&!¿V?orden\": 1,\n" +
                "          \"¡-&!¿V?sentencia_cql\": \"\",\n" +
                "          \"¡-&!¿V?filtrossentenciacql\": [],\n" +
                "          \"¡-&!¿V?columnasintermediassentenciacql\": []\n" +
                "        },\n" +
                "        \"¡__!formularios¡-@!¿V?column_name\": {\n" +
                "          \"¡-$!¿V?grupovalidacion\": \"diewebsiten.columnas.column_name.grupovalidacion\",\n" +
                "          \"¡-*!¿V?valorpordefecto\": \"\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"¡__!usuarios¡-@!¿V?dominio\": {\n" +
                "      \"¡-@!¿V?usuario\": {\n" +
                "        \"¡-*!¿V?email\": \"\",\n" +
                "        \"¡-*!¿V?contrasena\": \"\",\n" +
                "        \"¡__!sesiones¡-@!¿V?sessionid\": {\n" +
                "          \"¡-&!¿T?fechaultimoacceso\": \"\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
		
		String jsonObjectString2 = "{\n" +
                "  \"¡__!sitiosweb¡-@!¿V?sitioweb\": {\n" +
                "    \"¡-*!¿V?nombre\": \"\",\n" +
                "    \"¡-*!¿V?descripcion\": \"\",\n" +
                "    \"¡-$!¿L?keyspaces\": [\n" +
                "      \"system.schema_keyspaces.keyspace_name\"\n" +
                "    ],\n" +
                "    \"¡__!paginas¡-@!¿V?pagina\": {\n" +
                "      \"¡-*!¿V?nombre\": \"\",\n" +
                "      \"¡-*!¿V?descripcion\": \"\",\n" +
                "      \"¡__!eventos¡-@!¿V?evento\": {\n" +
                "        \"¡-@!¿V?transaccion\": {\n" +
                "          \"¡-*!¿V?descripcion\": \"\",\n" +
                "          \"¡-*!¿V?tipo\": \"\",\n" +
                "          \"¡-$!¿V?keyspace_name\": \"diewebsiten.sitiosweb.keyspaces\",\n" +
                "          \"¡-$!¿V?column_family\": \"columnas.columnfamily_name\",\n" +
                "          \"¡-&!¿V?orden\": 1,\n" +
                "          \"¡-&!¿V?sentencia_cql\": \"\",\n" +
                "          \"¡-&!¿V?filtrossentenciacql\": [],\n" +
                "          \"¡-&!¿V?columnasintermediassentenciacql\": []\n" +
                "        },\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
		
		String jsonObjectString3 = "{\n" +
                "  \"¡__!sitiosweb¡-@!¿V?sitioweb\": {\n" +
                "    \"¡-*!¿V?nombre\": \"\",\n" +
                "    \"¡-*!¿V?descripcion\": \"\",\n" +
                "    \"¡-$!¿L?keyspaces\": [\n" +
                "      \"system.schema_keyspaces.keyspace_name\"\n" +
                "    ],\n" +
                "    \"¡__!paginas¡-@!¿V?pagina\": {\n" +
                "      \"¡-*!¿V?nombre\": \"\",\n" +
                "      \"¡-*!¿V?descripcion\": \"\"\n" +
                "    },\n" +
                "    \"¡-$!¿V?column\": \"\"\n" +
                "  }\n" +
                "}";
		
		
		Json j = new Json();
		j.json = new JsonParser().parse(jsonObjectString3).getAsJsonObject();
		j.jsonElement = j.json;
//		j.iterar(null);primaryKeys.addProperty(tablaActual, primaryKeys.get(ta));
		System.out.println(j.json.get("¡__!sitiosweb¡-@!¿V?sitioweb"));

	}
	
	void iterar(String tabla) throws Exception {
		
		for (Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
			
			administrarLlaves(entry.getKey(), tabla);
			jsonElement = entry.getValue();
			if (jsonElement.isJsonObject()) {
				iterar(entry.getKey());
			}
			
		}
	}
	
	void administrarLlaves(String llave, String tabla) throws Exception {
		String prefijo = substringBetween(llave, DELIMITADOR_INICIO_PREFIJO_TABLA_O_COLUMNA, DELIMITADOR_FIN_PREFIJO_TABLA_O_COLUMNA);
		if (TABLA.equals(prefijo) || TABLA_SISTEMA.equals(prefijo)) {
			crearTabla(llave);
		} else if (indexOf(prefijo, PARTITION_KEY) > -1 || 
				   indexOf(prefijo, COLUMNA_RECUPERADA) > -1 || 
				   indexOf(prefijo, COLUMNA_POBLADA_DESDE_SISTEMA) > -1 || 
				   indexOf(prefijo, COLUMNA_REGULAR) > -1) { 
			agregarColumna(llave, tabla);
		} else {
			throw new Exception("El primer elemento " + prefijo + " de la LLAVE --> " + llave + " no está dentro del rango de elementos válidos");
		}
		
	}
	
	void crearTabla(String llave) throws Exception {
		
		String[] elementosTabla = split(trim(llave), DELIMITADOR_INICIO_PREFIJO_TABLA_O_COLUMNA);
		int numElementos = elementosTabla.length;
		
		if (numElementos < 2) {
			throw new Exception("Dentro de la LLAVE --> " + llave + " deben existir al menos dos elementos, uno de tipo Tabla --> " + TABLA + " o tipo Tabla Sistema --> " + TABLA_SISTEMA + ", y otro de tipo Partition Key --> " + PARTITION_KEY);
		}
		
		StringBuilder cql;
		
		for (int i = 0; i < numElementos; i++) {
			
			String elemento = elementosTabla[i];
			String elementoAnterior = i == 0 ? "" : elementosTabla[i-1];
			String prefijoDeTablaOColumna = substringBefore(elemento, DELIMITADOR_FIN_PREFIJO_TABLA_O_COLUMNA);
			String prefijoDeTablaOColumnaAnterior = substringBefore(elementoAnterior, DELIMITADOR_FIN_PREFIJO_TABLA_O_COLUMNA);
			String prefijoDeTipoDeColumna = substringBetween(elemento, DELIMITADOR_INICIO_PREFIJO_TIPO_DE_COLUMNA, DELIMITADOR_FIN_PREFIJO_TIPO_DE_COLUMNA);
			cql = new StringBuilder();
			
			if (!TABLA.equals(prefijoDeTablaOColumna) && !TABLA_SISTEMA.equals(prefijoDeTablaOColumna) && indexOf(prefijoDeTablaOColumna, PARTITION_KEY) < 0) {
				throw new Exception("El tipo de elemento " + prefijoDeTablaOColumna + " no está dentro del rango de tipos de elementos permitidos. 'TABLA' --> " + TABLA + ", 'TABLA DEL SISTEMA' --> " + TABLA_SISTEMA + " o 'PARTITION KEY' --> " + PARTITION_KEY + ". LLAVE --> " + llave);
			} else if (i == numElementos - 1 && indexOf(prefijoDeTablaOColumna, PARTITION_KEY) < 0) {
				throw new Exception("Dentro de la llave debe existir al menos un elemento de tipo 'PARTITION KEY' --> " + PARTITION_KEY + ". LLAVE --> " + llave);
			} else if (indexOf(prefijoDeTablaOColumnaAnterior, PARTITION_KEY) > -1 && (TABLA.equals(prefijoDeTablaOColumna) || TABLA_SISTEMA.equals(prefijoDeTablaOColumna))) {
				throw new Exception("El elemento de tipo 'PARTITION KEY' --> " + PARTITION_KEY + " no puede preceder a un elemento de tipo 'TABLA' --> " + TABLA + " o 'TABLA DEL SISTEMA'--> " + TABLA_SISTEMA + ". LLAVE --> " + llave);
			} else if ((TABLA.equals(prefijoDeTablaOColumna) || TABLA_SISTEMA.equals(prefijoDeTablaOColumna)) && isNotBlank(prefijoDeTipoDeColumna)) {
				throw new Exception("El elemento" + elemento + " es de tipo 'TABLA'. Por tanto no puede contener un prefijo de 'Tipo de Columna' (¿?)");
			}
						
			// Si el elemento actual es de tipo TABLA_SISTEMA, entonces no se tendrá en cuenta para construir la sentencia CQL que creará la nueva tabla.
			if (TABLA.equals(prefijoDeTablaOColumna)) {
				tablaActual = upperCase(substringAfter(elemento, DELIMITADOR_FIN_PREFIJO_TABLA_O_COLUMNA));
				cql.append("CREATE TABLE ").append(tablaActual).append("(");
				CQLs.addProperty(tablaActual, cql.toString());
			} else if (indexOf(prefijoDeTablaOColumna, PARTITION_KEY) > -1) {
				String nombreColumna = upperCase(substringAfter(elemento, DELIMITADOR_FIN_PREFIJO_TIPO_DE_COLUMNA));
				cql.append(CQLs.get(tablaActual).getAsString())
				   .append(nombreColumna)
				   .append(" ")
				   .append(retornarNombreTipoDeColumna(prefijoDeTipoDeColumna))
				   .append(",");
				CQLs.addProperty(tablaActual, cql.toString());
				primaryKeys.addProperty(tablaActual, nombreColumna);
				
//				IR GUARDANDO LAS PARTITION KEY DE LAS TABLAS PADRES
			}
		
		}
		System.out.println(CQLs.toString());
		
	}
	
	void agregarColumna(String llave, String tabla) {
		switch (tabla) {
			case PARTITION_KEY:
				
				break;
		}
	}
	
	String retornarNombreTipoDeColumna(String inicialesDeTipoDeColumna) throws Exception {
		
		final String VARCHAR = "VARCHAR";
		final String INTEGER = "INT";
		final String TIMESTAMP = "TIMESTAMP";
		final String MAP = "MAP";
		final String LIST = "LIST";
		final String SET = "SET";
		
		final char INI_VARCHAR = 'V';
		final char INI_INTEGER = 'I';
		final char INI_TIMESTAMP = 'T';
		final char INI_MAP = 'M';
		final char INI_LIST = 'L';
		final char INI_SET = 'S';
		
		final char DELIMITADOR_INICIO = '<';
		final char DELIMITADOR_FIN = '>';
		final char SEPARADOR_MAP = ',';
		
		StringBuilder tipoDeColumna = new StringBuilder();
		
		for (int i = 0; i < inicialesDeTipoDeColumna.length(); i++) {
			
			char inicial = inicialesDeTipoDeColumna.charAt(i);
			
			if (inicial == DELIMITADOR_INICIO || inicial == DELIMITADOR_FIN || inicial == SEPARADOR_MAP) {
				tipoDeColumna.append(inicial);
				continue;
			}
			
			if (INI_MAP == inicial || INI_LIST == inicial || INI_SET == inicial) {
				String inicialesPendientes = substring(inicialesDeTipoDeColumna, i, inicialesDeTipoDeColumna.length());
				if (isBlank(substringBetween(inicialesPendientes, "<", ">"))) {
					throw new Exception("Las columnas de tipo colección (MAP, LIST y SET) deben contener los delimitadores < >. Iniciales para la columna actual --> " + inicialesDeTipoDeColumna);
				} else if (INI_MAP == inicial && substringsBetween(inicialesPendientes, "<", ">").length != 2) {
					throw new Exception("Las columnas de tipo MAP deben contener dos (2) iniciales de tipos de columna dentro de los delimitadores < >. Iniciales para la columna actual --> " + inicialesDeTipoDeColumna);
				}
			}
			
			switch (inicial) {
				case INI_VARCHAR:
					tipoDeColumna.append(VARCHAR);
					break;
				case INI_INTEGER:
					tipoDeColumna.append(INTEGER);
					break;
				case INI_TIMESTAMP:
					tipoDeColumna.append(TIMESTAMP);
					break;
				case INI_MAP:
					tipoDeColumna.append(MAP);
					break;
				case INI_LIST:
					tipoDeColumna.append(LIST);
					break;
				case INI_SET:
					tipoDeColumna.append(SET);
					break;
				default:
					throw new Exception("La inicial '" + inicial + "' no le corresponde a ninguno de los tipos de columna definidos.");
			}
			
		}
		
		return tipoDeColumna.toString();
		
	}

}
