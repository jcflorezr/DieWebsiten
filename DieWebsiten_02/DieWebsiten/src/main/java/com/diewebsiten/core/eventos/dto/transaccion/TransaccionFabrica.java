package com.diewebsiten.core.eventos.dto.transaccion;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Type;
import java.util.List;

import com.diewebsiten.core.almacenamiento.AlmacenamientoFabrica.MotoresAlmacenamiento;
import com.diewebsiten.core.eventos.util.Constantes;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TransaccionFabrica {
    
    public static Transaccion obtenerTransaccion(JsonObject datosTransaccion) throws Exception {
    	return retornarTransaccion(datosTransaccion, null);
    }
    
    public static Transaccion obtenerTransaccion(JsonObject datosTransaccion, String nombreEvento) throws Exception {
    	return retornarTransaccion(datosTransaccion, nombreEvento);
    }
	
	private static Transaccion retornarTransaccion(JsonObject datosTransaccion, String nombreEvento) throws Exception {
		
		String motorAlmacenamiento = datosTransaccion.get(Constantes.Transacciones.MOTOR_ALMACENAMIENTO.get()).getAsString();
		JsonElement transaccionDeSistema = datosTransaccion.get(Constantes.Transacciones.TRANSACCION_DE_SISTEMA.get());
		Boolean esTransaccionDeSistema = transaccionDeSistema != null ? transaccionDeSistema.getAsBoolean() : false;
		Transaccion transaccion;
		
		switch (MotoresAlmacenamiento.valueOf(motorAlmacenamiento)) {
			case CASSANDRA:
				transaccion = new TransaccionCassandra();
				poblarTransaccion(transaccion, datosTransaccion);
				if (esTransaccionDeSistema) return transaccion;
				break;
			default:
				throw new Exception("El motor de almacenamiento '" + motorAlmacenamiento + "' no está soportado.");
		}
		
		return transaccion.obtenerTransaccion(transaccion, nombreEvento);
		
	}
	
	private static void poblarTransaccion (Transaccion transaccion, JsonObject datosTransaccion) throws Exception {
		String nombreTransaccion = datosTransaccion.get(Constantes.Transacciones.NOMBRE_TRANSACCION.get()).getAsString();
		String sentencia = datosTransaccion.get(Constantes.Transacciones.SENTENCIA.get()).getAsString();
		
		if (isBlank(nombreTransaccion) || isBlank(sentencia)) {
			throw new Exception("Ninguno de estos dos valores puede ser nulo: Nombre Transaccion: " + nombreTransaccion + ", Sentencia: " + sentencia);
		}
		
		Type arrayObjectType = new TypeToken<Object[]>(){private static final long serialVersionUID = 1L;}.getType();
		Type listStringType = new TypeToken<List<String>>(){private static final long serialVersionUID = 1L;}.getType();
		Gson gson = new Gson();
		
		transaccion.setNombre(nombreTransaccion)
		.setSentencia(sentencia)
		.setMotorAlmacenamiento(MotoresAlmacenamiento.valueOf(datosTransaccion.get(Constantes.Transacciones.MOTOR_ALMACENAMIENTO.get()).getAsString()))
		.setNombresFiltrosSentencia(gson.fromJson(datosTransaccion.get(Constantes.Transacciones.FILTROS_SENTENCIA.get()).getAsJsonArray(), listStringType));

		JsonElement parametrosTransaccion = datosTransaccion.get(Constantes.Transacciones.PARAMETROS_TRANSACCION.get());
		transaccion.setParametrosTransaccion((null != parametrosTransaccion) ? gson.fromJson(parametrosTransaccion, arrayObjectType) : null);	
	}
	
}
