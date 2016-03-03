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
    	
    	String nombreCampoActual = campo.getString("column_name");
        String grupoValidacionesCampoActual = campo.getString("grupovalidacion");
        StringBuilder sentencia = new StringBuilder("SELECT grupo, tipo, validacion FROM diewebsiten.grupos_validaciones WHERE grupo = '").append(grupoValidacionesCampoActual).append("'");
        
        arreglar esto
        
        
        
        List<Row> grupoValidacion = getEvento().getProveedorCassandra().consultar(sentencia.toString());

        // Validar que existen las validaciones del grupo.
        if (grupoValidacion.isEmpty())
            throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje(getEvento().getSitioWeb(), getEvento().getPagina(), getEvento().getNombreEvento()));            
        
        UtilidadValidaciones utilVal = new UtilidadValidaciones();
        UtilidadTransformaciones utilTrans = new UtilidadTransformaciones();
        
        List<String> resultadoValidacion = new ArrayList<>();
        
        for (Row grupo : grupoValidacion) {
            Object valorParametroActual = getEvento().getParametros().get(nombreCampoActual);
            if (grupo.getString("tipo").equals(Constantes.VALIDACION.getString())) {                
                resultadoValidacion.add(utilVal.validarParametro(grupo.getString("validacion"), valorParametroActual));
            } else {                
                Object resTrans = utilTrans.transformarParametro(grupo.getString("transformacion"), valorParametroActual);
                if (null == resTrans)
                    throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, getEvento().getNombreEvento(), (String)valorParametroActual, grupo.getString("validacion")));
                getEvento().setParametros(nombreCampoActual, resTrans);
            }
        }
        
        if (!resultadoValidacion.isEmpty()) {
        	getEvento().setParametros(nombreCampoActual, resultadoValidacion);
            return false;
        } else {
        	return true;        	
        }
    	
    }
    
    
    // =============================
    // ==== Getters and Setters ====
    // =============================
    
    private Evento getEvento() {
		return this.evento;
	}
    
}
