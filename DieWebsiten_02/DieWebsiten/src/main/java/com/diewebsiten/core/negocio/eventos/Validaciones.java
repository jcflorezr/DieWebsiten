package com.diewebsiten.core.negocio.eventos;

import java.util.List;
import java.util.concurrent.Callable;

import com.datastax.driver.core.Row;
import com.diewebsiten.core.almacenamiento.ProveedorCassandra;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;

class Validaciones implements Callable<Boolean> {
    
    private final Row campo;
    private ProveedorCassandra proveedorCassandra;
    
    public Validaciones(Row campo) {            
        this.campo = campo;
        this.proveedorCassandra = ProveedorCassandra.getInstance();
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
        
        List<Row> grupoValidacion = getProveedorCassandra().obtenerDataSet(sentencia.toString());

        // Validar que existen las validaciones del grupo.
        if (grupoValidacion.isEmpty())
            throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.VALIDACIONES_NO_EXISTEN.getMensaje(getSitioWeb(), getPagina(), getNombreEvento()));            
        
        for (Row grupo : grupoValidacion) {
            Object valorParametroActual = getParametros().get(nombreCampoActual);
            if (grupo.getString("tipo").equals(Constantes.VALIDACION.getString())) {                
                //for (String validacion : grupoValidacion.get(0).getSet("validaciones", String.class)) {
                    List<String> resVal = validarParametro(grupo.getString("validacion"), valorParametroActual);
                    if (!resVal.isEmpty()) {
                        setParametros(nombreCampoActual, resVal);
                        return false;
                    }
                //}
            } else {                
                //if (!grupoValidacion.get(0).isNull("transformaciones")) {
                    //for (String transformacion : grupoValidacion.get(0).getSet("transformaciones", String.class)) {
                        Object resTrans = transformarParametro(grupo.getString("validacion"), valorParametroActual);
                        if (null == resTrans)
                            throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.TRANSFORMACION_FALLIDA.getMensaje(nombreCampoActual, getNombreEvento(), (String)valorParametroActual, grupo.getString("validacion")));
                        setParametros(nombreCampoActual, resTrans);
                    //}
                //}
            }
        }

        return true;
        
    }
    
    
    
    
    private ProveedorCassandra getProveedorCassandra() {
		return this.proveedorCassandra;
	}
    
}
