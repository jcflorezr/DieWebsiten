
package com.diewebsiten.core.eventos.util;

import static com.diewebsiten.core.almacenamiento.cassandra.util.UtilidadCassandra.validarTipoColumna;
import static com.diewebsiten.core.util.Transformaciones.*;
import static com.diewebsiten.core.util.Validaciones.*;

/**
 *
 * @author juancamiloroman
 */
public class ProcesamientoParametros {
	

	/**
     * Fachada que cumple con la funcion de ejecutar cualquiera de las validaciones que se
     * implementan en esta clase
     * 
     * AGREGAR LA LISTA DE NOMBRES DE VALIDACIONES
     * 
     * @param nombreValidacion validación que se desea ejecutar
     * @param valor valor a validar
     * @return Si la validación no fue exitosa se retorna un mensaje de validación. Si la validación
     * fue exitosa se retorna el mismo valor recibido en el parámetro @valor 
     * @throws Exception
     */
    public static String validarParametro(String nombreValidacion, Object valor) throws Exception {
    	
       	if (esVacio(nombreValidacion)) {
       		throw new Exception("El nombre de la validación ha llegado nulo.");
       	}

       	if (esVacio(valor) && !Validaciones.V_OPCIONAL.equals(nombreValidacion)) {
       		return "Campo obligatorio";
       	}
       
       	switch(Validaciones.valueOf(nombreValidacion)) {
       
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
           	case V_TIPO_DATO_CASSANDRA:
           		if (!validarTipoColumna((String) valor)) {
           			return "La clase java '" + (String) valor + "' no existe.";
           		}
           		break;
           	default:
           		throw new Exception("La validación '" + nombreValidacion + "' no existe.");
       
       	}
    	
       	return (String) valor;
        
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
    public static Object transformarParametro (String nombreTransformacion, Object valor) throws Exception {
        //Thread.sleep(1000);
        if (esVacio(valor) || esVacio(nombreTransformacion)) {
            throw new Exception("No se puede hacer la ransformación de un valor nulo. Nombre Validación: " + nombreTransformacion + ". Parámetro : " + valor);
        }
                
        switch(Transformaciones.valueOf(nombreTransformacion)) {
        
            case T_EMAIL:        
                transformarEmailCassandra((String) valor);
            case T_CIFRADO:
                return encriptarCadena((String) valor);       
            case T_FECHAHORA:                
            	return trasformarFechaHora((String) valor);
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
    
    public enum Validaciones {
    	V_ALFANUMERICO_SIN_ESPACIOS, V_ALFANUMERICO_CON_ESPACIOS, V_NUMERICO_SIN_ESPACIOS, V_NUMERICO_CON_ESPACIOS, V_CARACTER_SIN_ESPACIOS,
    	V_CARACTER_CON_ESPACIOS, V_EMAIL, V_FECHAHORA, V_URL, V_DOMINIO, V_PUNTO, V_OPCIONAL, V_TIPO_DATO_CASSANDRA
    }
    
    public enum Transformaciones {
    	T_EMAIL, T_CIFRADO, T_FECHAHORA, T_MINUSCULAS, T_MAYUSCULAS, T_IDIOMA, T_CAMELCASE_CLASE, T_CAMELCASE_METODO, T_GUIONBAJO
    }
    
}
