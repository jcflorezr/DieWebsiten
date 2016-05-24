package com.diewebsiten.core.negocio.eventos;

import static com.diewebsiten.core.almacenamiento.util.UtilidadCassandra.validarTipoColumna;
import static com.diewebsiten.core.util.UtilidadTransformaciones.encriptarCadena;
import static com.diewebsiten.core.util.UtilidadTransformaciones.maximizar;
import static com.diewebsiten.core.util.UtilidadTransformaciones.minimizar;
import static com.diewebsiten.core.util.UtilidadTransformaciones.transformarCamelCaseTipoClase;
import static com.diewebsiten.core.util.UtilidadTransformaciones.transformarCamelCaseTipoMetodo;
import static com.diewebsiten.core.util.UtilidadTransformaciones.transformarEmailCassandra;
import static com.diewebsiten.core.util.UtilidadTransformaciones.trasformarFechaHora;
import static com.diewebsiten.core.util.UtilidadValidaciones.esAlfanumerico;
import static com.diewebsiten.core.util.UtilidadValidaciones.esAlfanumericoConEspacios;
import static com.diewebsiten.core.util.UtilidadValidaciones.esCaracter;
import static com.diewebsiten.core.util.UtilidadValidaciones.esCaracterConEspacios;
import static com.diewebsiten.core.util.UtilidadValidaciones.esDireccionUrl;
import static com.diewebsiten.core.util.UtilidadValidaciones.esDominioSitioWeb;
import static com.diewebsiten.core.util.UtilidadValidaciones.esEmailValido;
import static com.diewebsiten.core.util.UtilidadValidaciones.esFechaHora;
import static com.diewebsiten.core.util.UtilidadValidaciones.esNumerico;
import static com.diewebsiten.core.util.UtilidadValidaciones.esNumericoConEspacios;
import static com.diewebsiten.core.util.UtilidadValidaciones.esNumericoConPuntos;
import static com.diewebsiten.core.util.UtilidadValidaciones.esVacio;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.datastax.driver.core.Row;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;

class Validaciones implements Callable<Boolean> {
    
    private final Row campo;
    private final Evento evento;
    private AtomicBoolean validacionExitosa;
    
    private static final String COLUMN_NAME = "column_name";
    private static final String GRUPO_VALIDACION = "grupovalidacion";
    private static final String TIPO = "tipo";
    private static final String VALIDACION = "validacion";
    
    public Validaciones(Row campo, Evento evento) {            
        this.campo = campo;
        this.evento = evento;
        this.validacionExitosa = new AtomicBoolean(true);
    } 
    
    @Override
    public Boolean call() throws Exception {
    	try {			
    		return ejecutarValidacion();
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
    private Boolean ejecutarValidacion() throws Exception {
    	
    	String nombreCampoActual = campo.getString(COLUMN_NAME);
        String grupoValidacionesCampoActual = campo.getString(GRUPO_VALIDACION);
        StringBuilder sentencia = new StringBuilder("SELECT grupo_validacion, tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupo_validacion = '").append(grupoValidacionesCampoActual).append("'");
        
        List<Row> grupoValidacion = evento.getProveedorCassandra().consultar(sentencia.toString());

        // Validar que sí existan las validaciones del grupo.
        if (grupoValidacion.isEmpty()) {
			throw new ExcepcionGenerica(Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje(evento.getNombreEvento(), evento.getPagina(), evento.getSitioWeb()));
		}
        
        for (Row grupo : grupoValidacion) {
            Object valorParametroActual = evento.getParametro(nombreCampoActual);
            String tipoGrupoValidacion = grupo.getString(TIPO);
            if (Constantes.VALIDACION.getString().equals(tipoGrupoValidacion)) {
            	String resultadoValidacion = validarParametro(grupo.getString(VALIDACION), valorParametroActual);
            	if (!validacionExitosa.get()) { 
            		evento.setParametros(nombreCampoActual, resultadoValidacion);
            	}
            } else if (validacionExitosa.get() && Constantes.TRANSFORMACION.getString().equals(tipoGrupoValidacion)) {
                Object resTrans = transformarParametro(grupo.getString(VALIDACION), valorParametroActual);
                if (null == resTrans) {
                    throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, evento.getNombreEvento(), (String)valorParametroActual, grupo.getString("validacion")));
                }
                evento.setParametrosTransformados(nombreCampoActual, resTrans);
            }
        }
        
        return validacionExitosa.get();
    	
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
       		if (getValidacionExitosa().get()) {    		
   	    		getValidacionExitosa().set(false);
   	    	}
       		return "Campo obligatorio";
       	}
       
       	switch(Constantes.valueOf(nombreValidacion)) {
       
           	case V_ALFANUMERICO_CON_ESPACIOS:
           		if (!esAlfanumerico((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Campo alfanumerico sin espacios en blanco. Ejemplo: JuaN123";
           		}
           		break;        
           	case V_ALFANUMERICO_SIN_ESPACIOS:
           		if (!esAlfanumericoConEspacios((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Campo alfanumerico con posibles espacios en blanco. Ejemplo: JuaN 123 456";
           		}
           		break;        
           	case V_NUMERICO_SIN_ESPACIOS:
           		if (!esNumerico((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Campo numérico sin espacios en blanco. Ejemplo: 123456";
           		}
           		break;                
           	case V_NUMERICO_CON_ESPACIOS:
           		if (!esNumericoConEspacios((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Campo numérico con posibles espacios en blanco. Ejemplo: 123 456 789";
           		}
           		break;        
           	case V_CARACTER_SIN_ESPACIOS:
           		if (!esCaracter((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Campo caracter sin espacios en blanco. Ejemplo: abcDEF";
           		}
           		break;                    
           	case V_CARACTER_CON_ESPACIOS:
           		if (!esCaracterConEspacios((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Campo caracter con posibles espacios en blanco. Ejemplo: abc DEF hg";
           		}
           		break;        
           	case V_EMAIL:
           		if (!esEmailValido((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Dirección de correo electrónico no válida";
           		}
           		break;                    
           	case V_FECHAHORA:
           		if (!esFechaHora((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "Formato de fecha y hora no válido. Formato esperado: aaaa-MM-dd HH:mm:ss";
           		}
           		break;                    
           	case V_URL:
               	if (!esDireccionUrl((String) valor)) {
               		if (getValidacionExitosa().get()) {    		
                		getValidacionExitosa().set(false);
                	}
            	   return "Dirección url no válida";
               	}
               	break;            
           	case V_DOMINIO:
           		if (!esDominioSitioWeb((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "El campo sólo acepta números (0-9), letras en minúscula (a-z), puntos (.) o guiones (_-)";
           		}
           		break;
           	case V_PUNTO:
           		if (!esNumericoConPuntos((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
           	    	}
           			return "El campo sólo debe tener números (0-9) y puntos. Ejemplo: 1.5.8";
           		}
           		break;
           	case V_TIPO_DATO_CASSANDRA:
           		if (!validarTipoColumna((String) valor)) {
           			if (getValidacionExitosa().get()) {    		
           	    		getValidacionExitosa().set(false);
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
    
    
    // =============================
    // ==== Getters and Setters ====
    // =============================
    
    private AtomicBoolean getValidacionExitosa() {
		return this.validacionExitosa;
	}
    
}
