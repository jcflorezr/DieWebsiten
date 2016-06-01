package com.diewebsiten.core.eventos;

import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.contienePalabra;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.diewebsiten.core.eventos.dto.Campo;
import com.diewebsiten.core.eventos.dto.Evento;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.util.Constantes;

class Transacciones implements Callable<Void> {
    
    private final Transaccion transaccion;
    private final Evento evento;
    
    Transacciones (Transaccion transaccion, Evento evento) {
        this.transaccion = transaccion;
        this.evento = evento;
    }

    /** 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Void call() throws Exception {
    	try {
    		return ejecutarTransaccion();			
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
     * 
     * @return
     * @throws Exception
     */
    private Void ejecutarTransaccion() throws Exception {
      
        // Validar que la sentencia CQL sea de tipo válido.
        if (!contienePalabra(transaccion.getTipo(), Constantes.TRANSACCIONES_SOPORTADAS.getString())) {
			throw new ExcepcionGenerica(Constantes.Mensajes.SENTENCIACQL_NO_SOPORTADA.getMensaje(transaccion.getNombreTransaccion(), evento.getNombreEvento(), evento.getPagina(), evento.getSitioWeb(), transaccion.getTipo()));
		}
        
        // Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
        // y guardarlos en una lista para enviarlos a la sentencia preparada
        List<Object> valoresSentencia = new ArrayList<>();
        
        for (String columnaFiltro : transaccion.getColumnasFiltroSentenciaCql()) {
        	
        	boolean existenFiltros = false;
            
        	for (Campo campo : evento.getFormulario().getCampos()) {
            	
            	if (columnaFiltro.equals(campo.getColumnName())) {
                    if (isNotBlank(campo.getValorPorDefecto())) {
                        valoresSentencia.add(campo.getValorPorDefecto());
                    } else {
                        valoresSentencia.add(evento.getFormulario().getParametro(campo.getColumnName()));
                    }
                    existenFiltros = true;
            	}
            	
            }
            
            // Validar que existan los filtros necesarios para la sentencia CQL que ejecuta la transacción.
            if (!existenFiltros) {
				throw new ExcepcionGenerica(Constantes.Mensajes.FILTRO_NO_EXISTE.getMensaje(columnaFiltro, transaccion.getNombreTransaccion(), transaccion.getTipo(), evento.getNombreEvento(), evento.getPagina(), evento.getSitioWeb()));
			}
        }
        
        // Guardar los detalles de la sentencia de Base de Datos que contiene la transacción
        transaccion.setDetallesSentencia(transaccion.getSentenciaCql(), transaccion.getNombreTransaccion(), valoresSentencia.toArray());
        
        // Guardar los resultados de esta transacción dentro del resultado final de todo el evento
        Eventos.ejecutarTransaccion(transaccion, evento.getResultadoFinal());                  
             
        // Es necesario retornar null debido a que este método es de tipo Void en vez de void. Esto es debido a que 
        // este metodo se ejecuta por varios hilos al mismo tiempo
        return null;
    }
    
}
