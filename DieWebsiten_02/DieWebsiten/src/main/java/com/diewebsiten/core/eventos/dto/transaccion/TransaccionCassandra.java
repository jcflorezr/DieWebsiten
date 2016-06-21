package com.diewebsiten.core.eventos.dto.transaccion;

import static com.diewebsiten.core.util.Validaciones.contienePalabra;

import java.lang.reflect.Type;
import java.util.List;

import com.diewebsiten.core.almacenamiento.AlmacenamientoFabrica.MotoresAlmacenamiento;
import com.diewebsiten.core.eventos.Eventos;
import com.diewebsiten.core.eventos.util.Constantes;
import com.diewebsiten.core.eventos.util.Mensajes;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class TransaccionCassandra extends Transaccion {
	
	private String tipoTransaccion;
	private String keyspaceName;
	private String columnfamilyName;
	private List<String> columnasConsultaSentenciaCql;
	private List<String> columnasIntermediasSentenciaCql;
	
	public String getTipoTransaccion() {
		return tipoTransaccion;
	}
	
	public TransaccionCassandra setTipoTransaccion(String tipoTransaccion) {
		this.tipoTransaccion = tipoTransaccion;
		return this;
	}
	
	public String getKeyspaceName() {
		return keyspaceName;
	}

	public TransaccionCassandra setKeyspaceName(String keyspaceName) {
		this.keyspaceName = keyspaceName;
		return this;
	}

	public String getColumnfamilyName() {
		return columnfamilyName;
	}
	
	public TransaccionCassandra setColumnfamilyName(String columnfamilyName) {
		this.columnfamilyName = columnfamilyName;
		return this;
	}
	
	public List<String> getColumnasConsultaSentenciaCql() {
		return columnasConsultaSentenciaCql;
	}
	
	public TransaccionCassandra setColumnasConsultaSentenciaCql(List<String> columnasConsultaSentenciaCql) {
		this.columnasConsultaSentenciaCql = columnasConsultaSentenciaCql;
		return this;
	}
	
	public List<String> getColumnasIntermediasSentenciaCql() {
		return columnasIntermediasSentenciaCql;
	}
	
	public TransaccionCassandra setColumnasIntermediasSentenciaCql(List<String> columnasIntermediasSentenciaCql) {
		this.columnasIntermediasSentenciaCql = columnasIntermediasSentenciaCql;
		return this;
	}
	
	private static final String TRANSACCIONES_SOPORTADAS = "SELECT,UPDATE,INSERT,DELETE";
	private static final String TIPO_TRANSACCION = "tipotransaccion";
	private static final String KEYSPACE_NAME = "keyspace_name";
    private static final String COLUMNFAMILY_NAME = "columnfamily_name";
    private static final String COLUMNAS_CONSULTA_SENTENCIA_CQL = "columnasconsultasentenciacql";
    private static final String COLUMNAS_INTERMEDIAS_SENTENCIA_CQL = "columnasintermediassentenciacql";
	
	@Override
    public TransaccionCassandra obtenerTransaccion(Transaccion transaccion, String nombreEvento) throws Exception {
		
		try {	
			TransaccionCassandra transaccionCassandra = (TransaccionCassandra) transaccion;
			Type listStringType = new TypeToken<List<String>>(){private static final long serialVersionUID = 1L;}.getType();
			
			// Obtener desde la base de datos la información de la transacción actual, la cual es de tipo Cassandra
			Transaccion datosTransaccionCassandra = new TransaccionCassandra()
			.setSentencia(Constantes.Transacciones.Cassandra.SNT_TRANSACCIONES_CASSANDRA.get())
			.setNombre(Constantes.Transacciones.Cassandra.NMBR_SNT_TRANSACCIONES_CASSANDRA.get())
			.setParametrosTransaccion(new Object[]{nombreEvento, transaccion.getNombre()})
			.setMotorAlmacenamiento(MotoresAlmacenamiento.CASSANDRA);
			JsonObject datosTransaccion = Eventos.ejecutarTransaccion(datosTransaccionCassandra).getAsJsonArray().get(0).getAsJsonObject();
			
			// Validar que la sentencia CQL sea de tipo válido.
			String tipoTransaccion = datosTransaccion.get(TIPO_TRANSACCION).getAsString();
			if (!contienePalabra(tipoTransaccion, TRANSACCIONES_SOPORTADAS)) {
				throw new ExcepcionGenerica(Mensajes.Evento.Transaccion.SENTENCIACQL_NO_SOPORTADA.get(transaccion.getNombre(), tipoTransaccion));
			}
			
			Gson gson = new Gson();
			
			transaccionCassandra.setTipoTransaccion(tipoTransaccion)
			.setKeyspaceName(datosTransaccion.get(KEYSPACE_NAME).getAsString())
			.setColumnfamilyName(datosTransaccion.get(COLUMNFAMILY_NAME).getAsString())
			.setColumnasConsultaSentenciaCql(gson.fromJson(datosTransaccion.get(COLUMNAS_CONSULTA_SENTENCIA_CQL).getAsJsonArray(), listStringType))
			.setColumnasIntermediasSentenciaCql(gson.fromJson(datosTransaccion.get(COLUMNAS_INTERMEDIAS_SENTENCIA_CQL).getAsJsonArray(), listStringType));
			
			return transaccionCassandra;
		} catch (ClassCastException e) {
			throw new ExcepcionGenerica("La transacción no es de tipo Cassandra, es de tipo: " + transaccion.getClass().getName());
		}
		
	}

}
