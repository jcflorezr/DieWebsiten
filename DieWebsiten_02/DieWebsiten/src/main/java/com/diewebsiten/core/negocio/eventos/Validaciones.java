package com.diewebsiten.core.negocio.eventos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
    
    public Validaciones(Row campo, Evento evento) {            
        this.campo = campo;
        this.evento = evento;
    } 
    
    @Override
    public Boolean call() throws Exception {
        return ejecutarValidacion();
    }
    
    /**
     * Recibir los valores de los campos de un formulario, luego consultar la información
     * de dichos campos en la base de datos para después validarlos con respecto a dicha
     * información consultada.
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
        
        Boolean validacionExitosa = true;
        
        UtilidadValidaciones utilVal = new UtilidadValidaciones(validacionExitosa);
        UtilidadTransformaciones utilTrans = new UtilidadTransformaciones();
        
        
        List<String> resultadoValidacion = new ArrayList<>();
        
        for (Row grupo : grupoValidacion) {
            Object valorParametroActual = getEvento().getParametros().get(nombreCampoActual);
            String tipoGrupoValidacion = grupo.getString(TIPO);
            if (tipoGrupoValidacion.equals(Constantes.VALIDACION.getString())) {
            	
            	if (validacionExitosa) {
            		
            		como pasar objetos por referencia?
            		resultadoValidacion.add(utilVal.validarParametro(validacionExitosa, grupo.getString(VALIDACION), valorParametroActual));
            	} else {
            		resultadoValidacion.add(utilVal.validarParametro(grupo.getString(VALIDACION), valorParametroActual));
            	}
            	
                
            } else if (validacionExitosa && tipoGrupoValidacion.equals(Constantes.TRANSFORMACION.getString())) {
            	
                Object resTrans = utilTrans.transformarParametro(grupo.getString(VALIDACION), valorParametroActual);
                if (null == resTrans) {
                    throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, getEvento().getNombreEvento(), (String)valorParametroActual, grupo.getString("validacion")));
                }
                getEvento().setParametros(nombreCampoActual, resTrans);
            }
        }
        
        return validacionExitosa;
    	
    }
    
    
    // =============================
    // ==== Getters and Setters ====
    // =============================
    
    private Evento getEvento() {
		return this.evento;
	}
    
}
