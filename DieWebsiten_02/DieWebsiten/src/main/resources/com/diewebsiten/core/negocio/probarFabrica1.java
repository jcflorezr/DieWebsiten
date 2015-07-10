package com.diewebsiten.core.negocio;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class probarFabrica1 extends Fabrica {

    public static void main(String[] args) throws Exception {

        //new probarFabrica1().poblarTransaccionesPorTabla();
        //new probarFabrica1().modificarComentarios();
        new probarFabrica1().poblarDiccionarioTablas();

            
    }
    
    
    private void modificarComentarios() {
        
        try {
            Session ses = conectar();

            List<Row> comentarios = ses.execute(ses.prepare("SELECT keyspace_name, columnfamily_name, comment FROM system.schema_columnfamilies WHERE keyspace_name IN (?, ?)").bind("system", "system_traces")).all();
            List<Object> comentariosModificados = new ArrayList<Object>();
            
            String batch = "BEGIN BATCH ";
            PreparedStatement p;
            
            for (Row comentario : comentarios) {
                
                // ====== Modificar comentario actual ====== //
                batch += "UPDATE system.schema_columnfamilies SET comment = ? WHERE keyspace_name = ? AND columnfamily_name = ?;";
                comentariosModificados.add("{\"transacciones\" : [\"SELECT\"], \"comentario\" : \"" + comentario.getString("comment") + "\"}");
                comentariosModificados.add(comentario.getString("keyspace_name"));
                comentariosModificados.add(comentario.getString("columnfamily_name"));
                
            }
            
            batch += "APPLY BATCH;";
            p = ses.prepare(batch);
            ses.execute(p.bind(comentariosModificados.toArray()));
            
            cerrarConexion();

        } catch (Exception e) {
            Log.getInstance().imprimirErrorEnLog(e);
        }
        
    }
    
    private void poblarTransaccionesPorTabla() {
        
        try {
            Session ses = conectar();

            List<Row> comentarios = ses.execute(ses.prepare("SELECT keyspace_name, columnfamily_name, comment FROM system.schema_columnfamilies WHERE keyspace_name IN (?, ?, ?)").bind("system", "system_traces", "diewebsiten")).all();
            List<Object> comentariosModificados = new ArrayList<Object>();
            JSONArray transaccionesDisponibles;
            
            String batch = "BEGIN BATCH ";
            PreparedStatement p;
            
            for (Row comentario : comentarios) {                
                
                // ====== Insertar transacción para la tabla actual ====== //
                if (!comentario.getString("columnfamily_name").equals("IndexInfo") && !comentario.getString("columnfamily_name").equals("NodeIdInfo")) {
                    transaccionesDisponibles = new JSONObject(comentario.getString("comment")).getJSONArray("transacciones");
                    for (int i = 0; i < transaccionesDisponibles.length(); i++) {

                            batch += "INSERT INTO diewebsiten.transacciones_por_tabla (tipotransaccion, basededatos, tabla) VALUES (?, ?, ?);";
                            comentariosModificados.add(transaccionesDisponibles.getString(i));
                            comentariosModificados.add(comentario.getString("keyspace_name"));
                            comentariosModificados.add(comentario.getString("columnfamily_name"));
                    }                    
                } 
                
            }
            
            batch += "APPLY BATCH;";
            p = ses.prepare(batch);
            ses.execute(p.bind(comentariosModificados.toArray()));
            
            cerrarConexion();

        } catch (Exception e) {
            Log.getInstance().imprimirErrorEnLog(e);
        }
        
    }
    
    
    private void poblarDiccionarioTablas() {
        
        try {
            Session ses = conectar();

            List<Row> tablas = ses.execute("SELECT keyspace_name, columnfamily_name, column_name, type FROM system.schema_columns").all();
            
            List<Object> diccionarioTabla = new ArrayList<Object>();
            JSONArray transaccionesDisponibles;
            
            String batch = "BEGIN BATCH ";
            PreparedStatement p;
            
            List<String> transacciones = new ArrayList<String>();
            Map<String, String> campos = new HashMap<String, String>();
            
            // ====== Insertar diccionario para la tabla actual ====== //
            
            for (Row tabla : tablas) {
                
                if (!tabla.getString("columnfamily_name").equals("IndexInfo") && !tabla.getString("columnfamily_name").equals("NodeIdInfo")) {
                    
                    batch += "INSERT INTO diewebsiten.diccionario_columnas (basededatos, columna, tabla, tipo) VALUES (?, ?, ?, ?);";
                    
                    // Base de datos
                    diccionarioTabla.add(tabla.getString("keyspace_name"));
                    
                    // columna
                    diccionarioTabla.add(tabla.getString("column_name"));
                    
                    // Campos
                    /*List<Row> columnas = ses.execute(ses.prepare("SELECT keyspace_name, columnfamily_name, comment FROM system.schema_columns WHERE keyspace_name = ? AND columnfamily_name = ?").bind(tabla.getString("keyspace_name"), tabla.getString("columnfamily_name"))).all();
                    for (Row columna : columnas) {
                        
                            
                            //diccionarioTabla.add(transaccionesDisponibles.getString(i));
                            diccionarioTabla.add(tabla.getString("keyspace_name"));
                            diccionarioTabla.add(tabla.getString("columnfamily_name"));
                    } */
                    
                    // Comentario tabla
                    //diccionarioTabla.add("");
                    
                    // Tabla
                    diccionarioTabla.add(tabla.getString("columnfamily_name"));
                    
                    // Tipo
                    diccionarioTabla.add(tabla.getString("type"));
                    
                    // Transacciones
                    /*if (tabla.getString("keyspace_name").equals("system") || tabla.getString("keyspace_name").equals("system")) {
                        transacciones.add("SELECT");
                    } else {
                        transacciones.add("SELECT");
                        transacciones.add("INSERT");
                        transacciones.add("UPDATE");
                        transacciones.add("DELETE");
                    }
                    diccionarioTabla.add(transacciones); */                  
                } 
                
            }
            
            batch += "APPLY BATCH;";
            p = ses.prepare(batch);
            ses.execute(p.bind(diccionarioTabla.toArray()));
            
            cerrarConexion();

        } catch (Exception e) {
            Log.getInstance().imprimirErrorEnLog(e);
        }
        
    }

    

}
