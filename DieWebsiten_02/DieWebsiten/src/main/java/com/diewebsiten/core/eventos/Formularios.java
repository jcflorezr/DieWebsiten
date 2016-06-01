package com.diewebsiten.core.eventos;

import static com.diewebsiten.core.almacenamiento.cassandra.util.UtilidadCassandra.validarTipoColumna;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.encriptarCadena;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.maximizar;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.minimizar;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.transformarCamelCaseTipoClase;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.transformarCamelCaseTipoMetodo;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.transformarEmailCassandra;
import static com.diewebsiten.core.eventos.util.UtilidadTransformaciones.trasformarFechaHora;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esAlfanumerico;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esAlfanumericoConEspacios;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esCaracter;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esCaracterConEspacios;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esDireccionUrl;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esDominioSitioWeb;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esEmailValido;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esFechaHora;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esNumerico;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esNumericoConEspacios;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esNumericoConPuntos;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esVacio;

import java.util.concurrent.Callable;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.eventos.dto.Campo;
import com.diewebsiten.core.eventos.dto.Evento;
import com.diewebsiten.core.eventos.dto.Validacion;
import com.diewebsiten.core.util.Constantes;

class Formularios implements Callable<Void> {
    
    private final Campo campo;
    private final Evento evento;
    
    
    public Formularios(Campo campo, Evento evento) {            
        this.campo = campo;
        this.evento = evento;
    } 
    
    @Override
    public Void call() throws Exception {
    	try {			
    		return procesarFormulario();
		} catch (Exception e) {
			Throwable excepcionReal = e.getCause();
			if (excepcionReal != null) {
				throw (Exception) excepcionReal;
			} else {
				throw e;
			}
		}
    }
    
    /**
     * Recibir los valores de los parámetros de un formulario, luego obtener de
     * la base de datos la validación de cada parámetro y por último validar cada parámetro.
     *
     * @param camposFormulario
     * @param validacionesCampos
     * @param parametros
     * @throws com.diewebsiten.core.excepciones.ExcepcionGenerica
     */
    private Void procesarFormulario() throws Exception {
    	
    	String nombreCampo = campo.getColumnName();
        String grupoValidacionCampo = campo.getGrupoValidacion();
        
        StringBuilder sentencia = new StringBuilder("SELECT grupo_validacion, tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupo_validacion = '").append(grupoValidacionCampo).append("'");
        campo.setValidaciones(Eventos.ejecutarTransaccion(sentencia.toString()));

        // Validar que sí existan las validaciones del grupo.
        if (!campo.poseeValidaciones()) {
			throw new ExcepcionGenerica(Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje((String[]) evento.getInformacionEvento()));
		}
        
        for (Validacion validacion : campo.getValidaciones()) {
        	
            Object valorParametroActual = evento.getFormulario().getParametro(nombreCampo);
            
            if (Constantes.VALIDACION.getString().equals(validacion.getTipo())) {
            	String resultadoValidacion = validarParametro(validacion.getValidacion(), valorParametroActual);
            	if (!evento.getFormulario().isValidacionExitosa()) { 
            		evento.getFormulario().setParametros(nombreCampo, resultadoValidacion);
            	}
            } else if (evento.getFormulario().isValidacionExitosa() && Constantes.TRANSFORMACION.getString().equals(validacion.getTipo())) {
                Object resTrans = transformarParametro(validacion.getValidacion(), valorParametroActual);
                if (null == resTrans) {
                    throw new ExcepcionGenerica(Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampo, evento.getNombreEvento(), (String) valorParametroActual, validacion.getValidacion()));
                }
                evento.getFormulario().setParametrosTransformados(nombreCampo, resTrans);
            }
            
        }
        
        // Es necesario retornar null debido a que este método es de tipo Void en vez de void.
        // El resultado de la validacion ya se está guardando en el objeto new Evento().new Formulario().validacionExitosa
        return null;
    	
    }
    
    
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
    private String validarParametro(String nombreValidacion, Object valor) throws Exception {
    	
       	if (esVacio(nombreValidacion)) {
       		throw new Exception("El nombre de la validación ha llegado nulo.");
       	}

       	if (esVacio(valor) && !Constantes.V_OPCIONAL.equals(nombreValidacion)) {
       		if (evento.getFormulario().isValidacionExitosa()) {    		
       			evento.getFormulario().setValidacionExitosa(false);
   	    	}
       		return "Campo obligatorio";
       	}
       
       	switch(Constantes.valueOf(nombreValidacion)) {
       
           	case V_ALFANUMERICO_CON_ESPACIOS:
           		if (!esAlfanumerico((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Campo alfanumerico sin espacios en blanco. Ejemplo: JuaN123";
           		}
           		break;        
           	case V_ALFANUMERICO_SIN_ESPACIOS:
           		if (!esAlfanumericoConEspacios((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Campo alfanumerico con posibles espacios en blanco. Ejemplo: JuaN 123 456";
           		}
           		break;        
           	case V_NUMERICO_SIN_ESPACIOS:
           		if (!esNumerico((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Campo numérico sin espacios en blanco. Ejemplo: 123456";
           		}
           		break;                
           	case V_NUMERICO_CON_ESPACIOS:
           		if (!esNumericoConEspacios((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Campo numérico con posibles espacios en blanco. Ejemplo: 123 456 789";
           		}
           		break;        
           	case V_CARACTER_SIN_ESPACIOS:
           		if (!esCaracter((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Campo caracter sin espacios en blanco. Ejemplo: abcDEF";
           		}
           		break;                    
           	case V_CARACTER_CON_ESPACIOS:
           		if (!esCaracterConEspacios((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Campo caracter con posibles espacios en blanco. Ejemplo: abc DEF hg";
           		}
           		break;        
           	case V_EMAIL:
           		if (!esEmailValido((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Dirección de correo electrónico no válida";
           		}
           		break;                    
           	case V_FECHAHORA:
           		if (!esFechaHora((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "Formato de fecha y hora no válido. Formato esperado: aaaa-MM-dd HH:mm:ss";
           		}
           		break;                    
           	case V_URL:
               	if (!esDireccionUrl((String) valor)) {
               		if (evento.getFormulario().isValidacionExitosa()) {    		
               			evento.getFormulario().setValidacionExitosa(false);
                	}
            	   return "Dirección url no válida";
               	}
               	break;            
           	case V_DOMINIO:
           		if (!esDominioSitioWeb((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "El campo sólo acepta números (0-9), letras en minúscula (a-z), puntos (.) o guiones (_-)";
           		}
           		break;
           	case V_PUNTO:
           		if (!esNumericoConPuntos((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
           			return "El campo sólo debe tener números (0-9) y puntos. Ejemplo: 1.5.8";
           		}
           		break;
           	case V_TIPO_DATO_CASSANDRA:
           		if (!validarTipoColumna((String) valor)) {
           			if (evento.getFormulario().isValidacionExitosa()) {    		
           				evento.getFormulario().setValidacionExitosa(false);
           	    	}
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
    private Object transformarParametro (String nombreTransformacion, Object valor) throws Exception {
        //Thread.sleep(1000);
        if (esVacio(valor) || esVacio(nombreTransformacion)) {
            throw new Exception("No se puede hacer la ransformación de un valor nulo. Nombre Validación: " + nombreTransformacion + ". Parámetro : " + valor);
        }
                
        switch(Constantes.valueOf(nombreTransformacion)) {
        
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
    
}
