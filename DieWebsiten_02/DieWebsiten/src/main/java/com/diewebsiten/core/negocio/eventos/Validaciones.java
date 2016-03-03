package com.diewebsiten.core.negocio.eventos;

import java.util.List;
import java.util.concurrent.Callable;

import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.ProveedorCassandra;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;
import com.diewebsiten.core.util.Utilidades;

class Validaciones implements Callable<Boolean> {
    
    private final Row campo;
    private Evento evento;
    
    public Validaciones(Row campo, Evento evento) {            
        this.campo = campo;
        this.evento = evento;
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
    @Override
    public Boolean call() throws Exception {  
    	
        String nombreCampoActual = campo.getString("column_name");
        String grupoValidacionesCampoActual = campo.getString("grupovalidacion");
        StringBuilder sentencia = new StringBuilder("SELECT grupo, tipo, validacion FROM diewebsiten.grupos_validaciones WHERE grupo = '").append(grupoValidacionesCampoActual).append("'");
        
        List<Row> grupoValidacion = getEvento().getProveedorCassandra().consultar(sentencia.toString());

        // Validar que existen las validaciones del grupo.
        if (grupoValidacion.isEmpty())
            throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje(getEvento().getSitioWeb(), getEvento().getPagina(), getEvento().getNombreEvento()));            
        
        Utilidades util = new Utilidades();
        
        for (Row grupo : grupoValidacion) {
            Object valorParametroActual = getEvento().getParametros().get(nombreCampoActual);
            if (grupo.getString("tipo").equals(Constantes.VALIDACION.getString())) {                
                List<String> resVal = util.validarParametro(grupo.getString("validacion"), valorParametroActual);
                if (!resVal.isEmpty()) {
                	getEvento().setParametros(nombreCampoActual, resVal);
                    return false;
                }
            } else {                
                Object resTrans = util.transformarParametro(grupo.getString("validacion"), valorParametroActual);
                if (null == resTrans)
                    throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, getEvento().getNombreEvento(), (String)valorParametroActual, grupo.getString("validacion")));
                getEvento().setParametros(nombreCampoActual, resTrans);
            }
        }

        return true;
        
    }
    
    
    
    
    private Evento getEvento() {
		return this.evento;
	}
    
}
