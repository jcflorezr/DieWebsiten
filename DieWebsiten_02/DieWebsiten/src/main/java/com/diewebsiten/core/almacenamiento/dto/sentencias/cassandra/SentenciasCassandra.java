package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.datastax.driver.core.*;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactory;
import com.diewebsiten.core.eventos.dto.Transaccion;

public class SentenciasCassandra extends SentenciasFactory {

    private Session sesion;
    private Transaccion transaccion;

    private static Object obj = new Object();

    public SentenciasCassandra(Session sesion, Transaccion transaccion) {
        this.sesion = sesion;
        this.transaccion = transaccion;
    }

    @Override
    public Sentencia crearSentencia() {
        String sentenciaCQL = transaccion.getSentencia();
//        try {
            Sentencia sentencia = super.obtenerSentenciaExistente(sentenciaCQL);
            if (sentencia == null) {
                synchronized (obj) {
                    sentencia =  super.obtenerSentenciaExistente(sentenciaCQL);
                    if (sentencia == null) {
                        sentencia = new SentenciaCassandra(sentenciaCQL, sesion);
                        super.guardarNuevaSentencia(sentencia);
                    }
                }
            }
            return sentencia;
//        } catch (ClassCastException e) {
//            throw new ExcepcionGenerica("La sentencia de la transacción '" + nombreTransaccion + "' no es de tipo Cassandra");
//        }
    }

}
