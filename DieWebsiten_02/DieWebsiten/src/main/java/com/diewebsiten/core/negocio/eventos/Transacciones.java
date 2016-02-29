package com.diewebsiten.core.negocio.eventos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Transacciones implements Callable<Void> {
    
    private final Row transaccion;
    private JsonObject resultadoEvento; 
    
    Transacciones (Row transaccion, JsonObject resultadoEvento) {
        this.transaccion = transaccion;
        this.resultadoEvento = resultadoEvento;
    }

    @Override
    public Void call() throws Exception {
        
    	// Nombre de la transacción.
    	String nombreTransaccion = transaccion.getString("transaccion");
        
        // Tipo de transacción (SELECT, UPDATE, DELETE, INSERT).
        String tipoTransaccion = transaccion.getString("tipotransaccion");
        
        // Sentencia CQL de la transacción.
        String sentenciaCQL = transaccion.getString("sentenciacql");
        
        // Filtros que se necesitan para ejecutar la transacción.
        List<String> filtrosSentenciaCQL = new ArrayList<String>(transaccion.getList("filtrossentenciacql", String.class));
        
        //PreparedStatement sentenciaCql = getSesionBD().prepare(transaccion.getString("sentenciacql"));
      
        // Validar que la sentencia CQL sea de tipo válido.
        if (!getUtil().contienePalabra(tipoTransaccion, "SELECT,UPDATE,INSERT,DELETE"))
            throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.SENTENCIACQL_NO_SOPORTADA.getMensaje(nombreTransaccion, getNombreEvento(), getPagina(), getSitioWeb(), tipoTransaccion));
    
            
        //Thread.sleep(1000);        
        
        // Agregar las supercolumnas al Map de parámetros del formulario, debido a que
        // están presentes en casi todas las transacciones que componen un evento
        // NOTA: solo se agregan estas supercolumnas en caso de que el Map de parámetros
        //       no las contenga.
        if (null == getParametros().get("sitioweb")) setParametros("sitioweb", getSitioWeb());
        if (null == getParametros().get("pagina")) setParametros("pagina", getPagina());
        if (null == getParametros().get("idioma")) setParametros("idioma", getIdioma());
        
        
        // Extraer los valores recibidos desde el cliente (navegador web, dispositivo móvil)
        // y guardarlos en una lista para enviarlos a la sentencia preparada
        List<Object> valoresSentencia = new ArrayList<Object>();
        
        for (String filtro : filtrosSentenciaCQL) {
        	boolean existe = false;
            for (Row campo : getCamposFormularioEvento()) {
            	
            	if (filtro.equals(campo.getString("column_name"))) {
                // Solo se extraen los valores para las cláusulas SET de los UPDATES, WHERE, y VALUES de los INSERTS.
                //if (getUtil().contienePalabra(campo.getString("clausula"), "SET,WHERE,VALUES")) {
                    // Si el campo de la sentencia tiene un valor por defecto se guardará este valor
                    // en vez de guardar el valor que viene en el Map de parámetros.
                    if (!getUtil().esVacio(campo.getString("valorpordefecto"))) {
                        valoresSentencia.add(campo.getString("valorpordefecto"));
                    } else {
                        valoresSentencia.add(getParametros().get(campo.getString("column_name")));
                    }
                    existe = true;
                    break;
                    
            	}
            }
            
            // Validar que los filtros necesarios para la sentencia CQL que ejecuta la transacción existen.
            if (!existe)
                throw new ExcepcionGenerica(com.diewebsiten.core.util.Constantes.Mensajes.FILTRO_NO_EXISTE.getMensaje(filtro, nombreTransaccion, tipoTransaccion, getNombreEvento(), getPagina(), getSitioWeb()));
        }
        
        
        synchronized (Eventos.class) {
            if (null == getSentenciasPreparadas().get(nombreTransaccion)) {
                getSentenciasPreparadas().put(nombreTransaccion, getSesionBD().prepare(sentenciaCQL));
            }
        }                       

        // Ejecutar la sentencia CQL de la transacción.
        ResultSet rs = getSesionBD().execute(getSentenciasPreparadas().get(nombreTransaccion).bind(valoresSentencia.toArray()));
        
        // Obtener los resultados de la transacción.
        List<Row> resultadoTransaccionActual = rs.all();
        
        // Obtener los nombres de las columnas que contiene la transacción.
        List<ColumnDefinitions.Definition> columnas = rs.getColumnDefinitions().asList();
        
        if (tipoTransaccion.equals("SELECT")) {
        	
        	// Columnas de consulta que contiene la transacción.
            //List<String> columnasConsultaSentenciaCQL = transaccion.getList("columnasconsultasentenciacql", String.class);
            
            // Filtros que se necesitan para ejecutar la transacción.
            List<String> columnasIntermediasSentenciaCQL = new ArrayList<String> (transaccion.getList("columnasintermediassentenciacql", String.class));

            transformarResultSet(this.resultadoEvento, resultadoTransaccionActual, columnas, filtrosSentenciaCQL, columnasIntermediasSentenciaCQL);
            
        }                    
             
        //return resultadoTransaccion; 
        return null;
        
    }
    
    /**
     * 
     * @param resultSet
     * @param columnas
     * @param filtros
     * @param columnasIntermedias
     * @throws Exception
     */
    private void transformarResultSet (JsonObject resultadoEvento, List<Row> resultSet, List<ColumnDefinitions.Definition> columnas, List<String> filtros, List<String> columnasIntermedias) throws Exception {
		
		if (null == resultadoEvento)
			throw new Exception("La colección donde se va a crear la estructura del resultado del evento debe estar inicializada");

		
		// EL ORDEN DE LOS FILTROS YA VIENE ESTABLECIDO DESDE 
		// LA BASE DE DATOS (TABLA EVENTOS) SEGÚN EL ORDEN EN QUE SE CREARON EN LA TABLA
		
		JsonObject coleccionFiltros = resultadoEvento;
		int i = 0;
		
		filtros.addAll(columnasIntermedias);
		
		for (String filtro : filtros) {

			JsonObject coleccionFiltroActual = coleccionFiltros.getAsJsonObject(filtro);			
			
			if (null == coleccionFiltroActual) {
				
				if (i == 0)
					coleccionFiltros = resultadoEvento;
				
				// Agregar por primera vez los filtros o columnas intermedias faltantes
				for (;i<filtros.size();i++) {
					coleccionFiltros.add(filtros.get(i), new JsonObject());
					coleccionFiltros = coleccionFiltros.getAsJsonObject(filtros.get(i));
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
		
		for (Row fila : resultSet) {
			
			JsonObject coleccionColumnaActual = null;
			JsonObject posicion = null;
			
			i = 0;
			
            for (ColumnDefinitions.Definition columnaActual : columnas) {
            	
            	String nombreColumnaActual = columnaActual.getName();
            	
            	Object valorColumnaActual = columnaActual.getType().deserialize(fila.getBytesUnsafe(columnaActual.getName()), ProtocolVersion.NEWEST_SUPPORTED);
            	
            	if (!columnasIntermedias.isEmpty() && i < columnasIntermedias.size()) {
            		
            		if (!columnasIntermedias.get(i).equals(columnaActual.getName()))
            			throw new Exception("El orden de las columnas de consulta en la cláusula SELECT no coincide con el orden de las columnas como están creadas en la tabla '" + columnaActual.getKeyspace() + "." + columnaActual.getTable() +"'");
            		
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
