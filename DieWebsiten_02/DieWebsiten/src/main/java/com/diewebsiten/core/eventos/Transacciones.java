package com.diewebsiten.core.eventos;

import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.contienePalabra;
import static com.diewebsiten.core.eventos.util.UtilidadValidaciones.esVacio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.eventos.dto.Campo;
import com.diewebsiten.core.eventos.dto.Evento;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.util.Constantes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
     */
    private Void ejecutarTransaccion() throws Exception {
      
        // Validar que la sentencia CQL sea de tipo válido.
        if (!contienePalabra(transaccion.getTipo(), Constantes.TRANSACCIONES_SOPORTADAS.getString())) {
			throw new ExcepcionGenerica(Constantes.Mensajes.SENTENCIACQL_NO_SOPORTADA.getMensaje(transaccion.getNombreTransaccion(), evento.getNombreEvento(), evento.getPagina(), evento.getSitioWeb(), transaccion.getTipo()));
		}
    
            
        //Thread.sleep(1000);        
        
        
        // Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
        // y guardarlos en una lista para enviarlos a la sentencia preparada
        List<Object> valoresSentencia = new ArrayList<Object>();
        
        for (String columnaFiltro : transaccion.getColumnasFiltroSentenciaCql()) {
        	boolean existenFiltros = false;
            for (Campo campo : evento.getFormulario().getCampos()) {
            	
            	if (columnaFiltro.equals(campo.getColumnName())) {
                    if (!esVacio(campo.getValorPorDefecto())) {
                        valoresSentencia.add(campo.getValorPorDefecto());
                    } else {
                        valoresSentencia.add(evento.getFormulario().getParametro(campo.getColumnName()));
                    }
                    existenFiltros = true;
                    break;
                    
            	}
            }
            
            // Validar que los filtros necesarios para la sentencia CQL que ejecuta la transacción existen.
            if (!existenFiltros) {
				throw new ExcepcionGenerica(Constantes.Mensajes.FILTRO_NO_EXISTE.getMensaje(columnaFiltro, transaccion.getNombreTransaccion(), transaccion.getTipo(), evento.getNombreEvento(), evento.getPagina(), evento.getSitioWeb()));
			}
        }
        
        // Obtener los resultados de la transacción.
        List<JsonObject> resultadoTransaccionActual;
        if (!valoresSentencia.isEmpty()) {
			resultadoTransaccionActual = Eventos.consultar(transaccion.getSentenciaCql(), transaccion.getNombreTransaccion(), valoresSentencia.toArray()); 
		} else {
			resultadoTransaccionActual = Eventos.consultar(transaccion.getSentenciaCql()); 
		}
      
        if (!resultadoTransaccionActual.isEmpty() && "SELECT".equals(transaccion.getTipo())) {
            
        	// Obtener las nombres de las columnas
        	Set<Entry<String, JsonElement>> columnas = resultadoTransaccionActual.get(0).entrySet();

            transformarResultSet(evento.getResultadoFinal(), resultadoTransaccionActual, columnas, transaccion.getColumnasFiltroSentenciaCql(), transaccion.getColumnasIntermediasSentenciaCql());
            
        }                    
             
        // Es necesario retornar null debido a que este método es de tipo Void en vez de void. Esto es debido a que 
        // este metodo se ejecuta por varios hilos al mismo tiempo
        return null;
    }
    
    /**
     * 
     * @param resultSet
     * @param columnas
     * @param columnasFiltros
     * @param columnasIntermedias
     * @throws Exception
     */
    private void transformarResultSet (JsonObject resultadoEvento, List<JsonObject> resultSet, Set<Entry<String, JsonElement>> columnas, List<String> columnasFiltros, List<String>columnasIntermedias) throws Exception {
		
		if (null == resultadoEvento) {
			throw new Exception("La colección donde se va a crear la estructura del resultado del evento debe estar inicializada");
		}

		
		// EL ORDEN DE LOS FILTROS YA VIENE ESTABLECIDO DESDE 
		// LA BASE DE DATOS (TABLA EVENTOS) SEGÚN EL ORDEN EN QUE SE CREARON EN LA TABLA
		
		JsonObject coleccionFiltros = resultadoEvento;
		int i = 0;
		
		columnasFiltros.addAll(columnasIntermedias);
		
		for (String filtro : columnasFiltros) {
			
			JsonObject coleccionFiltroActual = coleccionFiltros.getAsJsonObject(filtro);			
			
			if (null == coleccionFiltroActual) {
				
				if (i == 0)
					coleccionFiltros = resultadoEvento;
				
				// Agregar por primera vez los filtros o columnas intermedias faltantes
				for (;i<columnasFiltros.size();i++) {
					coleccionFiltros.add(columnasFiltros.get(i), new JsonObject());
					coleccionFiltros = coleccionFiltros.getAsJsonObject(columnasFiltros.get(i));
				}
				
				break;
				
			} else {
				coleccionFiltros = coleccionFiltroActual;
			}
			
			i++;

		}
		
		
		// EL ORDEN DE LAS COLUMNAS INTERMEDIAS Y DE LAS COLUMNAS DE CONSULTA
		// YA VIENE ESTABLECIDO DESDE LA BASE DE DATOS (TABLA EVENTOS) SEGÚN EL ORDEN EN QUE
		// SE CREARON EN LA TABLA
		
		for (JsonObject fila : resultSet) {
			
			JsonObject coleccionColumnaActual = null;
			JsonObject posicion = null;
			
			i = 0;
			
            for (Map.Entry<String, JsonElement> columnaActual : columnas) {
            	
            	String nombreColumnaActual = columnaActual.getKey();
            	Object valorColumnaActual = fila.get(nombreColumnaActual).getAsString();
            	
            	
            	
            	if (columnasIntermedias != null && i < columnasIntermedias.size()) {
            		
            		
            		
            		/**
            		 * ESTE INCONVENIENTE DE OBTENER EL NOMBRE DE LA TABLA Y DE LA BASE DE DATOS SE PUEDE SOLUCIONAR CONSULTANDO LA INFO DE LA TABLA DENTRO DE SYSTEM.SCHEMA_COLUMNFAMILIES
            		 */
            		if (!columnasIntermedias.get(i).equals(nombreColumnaActual)) {
            			//throw new Exception("El orden de las columnas de consulta en la cláusula SELECT no coincide con el orden de las columnas como están creadas en la tabla '" + columnaActual.getKeyspace() + "." + columnaActual.getTable() +"'");
            			throw new Exception("El orden de las columnas de consulta en la cláusula SELECT no coincide con el orden de las columnas como están creadas en la tabla.");
            		}
            		
            		coleccionColumnaActual = coleccionFiltros.getAsJsonObject(valorColumnaActual.toString()); 
            		if (null == coleccionColumnaActual) {
            			coleccionFiltros.add(valorColumnaActual.toString(), new JsonObject());
            			coleccionColumnaActual = coleccionFiltros.getAsJsonObject(valorColumnaActual.toString());
            		}
            		
            	} else {
            		
            		if (null == posicion) 
            			posicion = null != coleccionColumnaActual ? coleccionColumnaActual : null != coleccionFiltros ? coleccionFiltros : resultadoEvento;
            			
            		JsonElement valorColumnaExistente = posicion.get(nombreColumnaActual);
            			
            		// Verificar si esta columna ya tiene un valor. Si es así se le añade el valor actual con una coma (,) por delante.												    	   
            		if (null == valorColumnaExistente)
            			posicion.addProperty(nombreColumnaActual, valorColumnaActual.toString());
            		else
            			posicion.addProperty(nombreColumnaActual, valorColumnaExistente.getAsString() + "," + valorColumnaActual.toString());
            		
            	}
            	
            	i++;
            	
            }
            
        }
        
    }
    
}
