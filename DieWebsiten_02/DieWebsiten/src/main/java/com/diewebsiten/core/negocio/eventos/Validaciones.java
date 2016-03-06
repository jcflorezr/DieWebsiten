package com.diewebsiten.core.negocio.eventos;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.lang3.StringUtils.*;

import com.datastax.driver.core.Row;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.UtilidadTransformaciones;
import com.diewebsiten.core.util.UtilidadValidaciones;

class Validaciones implements Callable<Boolean> {
    
    private final Row campo;
    private Evento evento;
    
    private static final String COLUMN_NAME = "column_name";
    private static final String GRUPOVALIDACION = "grupovalidacion";
    private static final String TIPO = "tipo";
    private static final String VALIDACION = "validacion";
    private static final String SEPARADOR_TRANS = "||";
    
    public Validaciones(Row campo, Evento evento) {            
        this.campo = campo;
        this.evento = evento;
    } 
    
    @Override
    public Boolean call() throws Exception {
        return ejecutarValidacion();
    }
    
    /**
     * Recibir los valores de los parámetros de un formulario, luego obtener de
     * la base de datos la validación de cada parámetro y por último validar cada parámetro.
     *
     * @param camposFormulario
     * @param validacionesCampos
     * @param parametros
     * @return Un Map que contiene los detalles de la validación de cada campo.
     * @throws com.diewebsiten.core.excepciones.ExcepcionGenerica
     */
    private Boolean ejecutarValidacion() throws Exception {
    	
    	String nombreCampoActual = campo.getString(COLUMN_NAME);
        String grupoValidacionesCampoActual = campo.getString(GRUPOVALIDACION);
        StringBuilder sentencia = new StringBuilder("SELECT grupo, tipo, validacion FROM diewebsiten.grupos_validaciones WHERE grupo = '").append(grupoValidacionesCampoActual).append("'");
        
        List<Row> grupoValidacion = getEvento().getProveedorCassandra().consultar(sentencia.toString());

        // Validar que sí existan las validaciones del grupo.
        if (grupoValidacion.isEmpty()) {
			throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje(getEvento().getSitioWeb(), getEvento().getPagina(), getEvento().getNombreEvento()));
		}            
        
        AtomicBoolean validacionExitosa = new AtomicBoolean(true);
        
        UtilidadValidaciones utilVal = new UtilidadValidaciones(validacionExitosa);
        UtilidadTransformaciones utilTrans = new UtilidadTransformaciones();
        
        for (Row grupo : grupoValidacion) {
            Object valorParametroActual = getEvento().getParametros().get(nombreCampoActual);
            String tipoGrupoValidacion = grupo.getString(TIPO);
            if (Constantes.VALIDACION.getString().equals(tipoGrupoValidacion)) {
            	String resultadoValidacion = utilVal.validarParametro(grupo.getString(VALIDACION), valorParametroActual);
            	if (!validacionExitosa.get()) { 
            		getEvento().setParametros(nombreCampoActual, resultadoValidacion);
            		descartarParametrosTransformados();
            	}
            } else if (validacionExitosa.get() && Constantes.TRANSFORMACION.getString().equals(tipoGrupoValidacion)) {
                Object resTrans = utilTrans.transformarParametro(grupo.getString(VALIDACION), valorParametroActual);
                if (null == resTrans) {
                    throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, getEvento().getNombreEvento(), (String)valorParametroActual, grupo.getString("validacion")));
                }
                getEvento().setParametros(nombreCampoActual, ((String) getEvento().getParametros().get(nombreCampoActual)) + SEPARADOR_TRANS + resTrans);
            }
        }
        
        if (validacionExitosa.get()) {
        	separarParametrosTransformados();
        }
        
        return validacionExitosa.get();
    	
    }
    
    /**
     * Retirar los parametros del campo "Evento.parametros"
     * que fueron transformados cuando la validación aun era
     * exitosa
     */
    private void descartarParametrosTransformados() {
    	Map<String, Object> parametros = getEvento().getParametros();
    	for (Map.Entry<String, Object> parametro : parametros.entrySet()) {
			parametros.put(parametro.getKey(), substringBefore((String) parametro.getValue(), SEPARADOR_TRANS));
    	}
    }
    
    /**
     * Separar los valores transformados del campo "Evento.parametros".
     * Este método solo se ejecuta si la validación fue exitosa.
     */
    private void separarParametrosTransformados() {
    	Map<String, Object> parametros = getEvento().getParametros();
    	for (Map.Entry<String, Object> parametro : parametros.entrySet()) {
    		String valorFinalParametro = substringAfter((String) parametro.getValue(), SEPARADOR_TRANS);
			parametros.put(parametro.getKey(), !isBlank(valorFinalParametro) ? valorFinalParametro : (String) parametro.getValue());
    	}
    }
    
    
    // =============================
    // ==== Getters and Setters ====
    // =============================
    
    private Evento getEvento() {
		return this.evento;
	}
    
}
