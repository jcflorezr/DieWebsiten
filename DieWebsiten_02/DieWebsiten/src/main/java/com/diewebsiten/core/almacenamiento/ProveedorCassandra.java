package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.exceptions.DriverException;
import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.cassandra.Cassandra;
import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.cassandra.CassandraFactory;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.diewebsiten.core.almacenamiento.ResultadoTransaccion.*;
import static com.diewebsiten.core.almacenamiento.ResultadoTransaccion.TiposResultado.PLANO;
import static com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencias.obtenerSentencia;
import static com.diewebsiten.core.almacenamiento.util.Sentencias.LLAVES_PRIMARIAS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Administrar las conexiones y transacciones que se realizan al
 * motor de base de datos Cassandra.
 *
 * @author Juan Camilo Flórez Román (www.diewebsiten.com)
 */
public class ProveedorCassandra extends ProveedorAlmacenamiento {

    private static Optional<Cluster> cluster;
    private static Session sesion;

	public static Function<String, PreparedStatement> prepararSentencia = (queryString) -> sesion.prepare(queryString);

    private static final String CASSANDRA_URL = "127.0.0.1";
    private static final int CASSANDRA_PORT = 9042;
	private static final List<String> TIPOS_NUMERICOS = asList("Integer","Long","Float","Double","BigDecimal","BigInteger");

	ProveedorCassandra(){}

	@Override
	void conectar() {
		try {
			cluster = Optional.ofNullable(Cluster.builder().addContactPoint(CASSANDRA_URL).withPort(CASSANDRA_PORT).build());
			sesion = cluster.get().connect();
            // Inicializar la sentencia de LLaves Primarias para obtener las llaves primarias de las
            // sentencias que se ejecutaran en las futuras transacciones
			obtenerSentencia(new CassandraFactory(LLAVES_PRIMARIAS.sentencia(), true));
		} catch (DriverException e) {
			throw new ExcepcionGenerica(e);
		}
    }
    
	@Override
	void desconectar() {
		cluster.ifPresent(Cluster::close);
    }
	
    @Override
	JsonNode ejecutarTransaccion(Transaccion transaccion) {

		String sentenciaCQL = transaccion.getSentencia();
    	Object[] parametros = Optional.ofNullable(transaccion.getParametrosTransaccion()).orElse(new Object[]{});
    	TiposResultado tipoResultado = obtenerTipoResultado(transaccion.getTipoResultado());

    	try {

			boolean esSentenciaSimple = tipoResultado == PLANO ? true : false;
			Cassandra sentencia = (Cassandra) obtenerSentencia(new CassandraFactory(sentenciaCQL, esSentenciaSimple));

    		if (parametros.length != sentencia.numParametrosSentencia()) {
    			throw new ExcepcionGenerica("La sentencia necesita " + sentencia.numParametrosSentencia() + " parámetros para ser ejecutada.");
    		}

    		// TODO Aqui hay un error de concurrencia
//			[SITIO WEB: 'localhost'. PÁGINA: 'eventos'. EVENTO: 'ConsultarInfoSitioWeb']
//			[PARÁMETROS] --> {"sitioweb":"miradorhumadea.com","tipo":"SW","basededatos":"diewebsiten","tipotransaccion":"seLECT"}
//			[EXCEPCIÓN]  --> com.diewebsiten.core.excepciones.ExcepcionGenerica
//					[MENSAJE]    --> Error al ejecutar la sentencia CQL --> SELECT tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupovalidacion = ?;'. Parámetros: [null]. Mensaje original --> com.datastax.driver.core.exceptions.InvalidQueryException: Invalid null value for partition key part grupovalidacion
//			at com.datastax.driver.core.exceptions.InvalidQueryException.copy(InvalidQueryException.java:50)
//			at com.datastax.driver.core.DriverThrowables.propagateCause(DriverThrowables.java:37)
//			at com.datastax.driver.core.DefaultResultSetFuture.getUninterruptibly(DefaultResultSetFuture.java:245)
//			at com.datastax.driver.core.AbstractSession.execute(AbstractSession.java:63)
//			at com.diewebsiten.core.almacenamiento.ProveedorCassandra.lambda$new$1(ProveedorCassandra.java:47)
//			at com.diewebsiten.core.almacenamiento.ProveedorCassandra$$Lambda$2/433287555.apply(Unknown Source)
//			at com.diewebsiten.core.almacenamiento.ProveedorCassandra.ejecutarTransaccion(ProveedorCassandra.java:125)
//			at com.diewebsiten.core.almacenamiento.Proveedores.ejecutarTransaccion(Proveedores.java:29)
//			at com.diewebsiten.core.eventos.FachadaEventos.ejecutarTransaccion(FachadaEventos.java:162)
//			at com.diewebsiten.core.eventos.Eventos.ejecutarTransaccion(Eventos.java:136)
//			at com.diewebsiten.core.eventos.Eventos$ValidacionFormularios.procesarFormulario(Eventos.java:176)
//			at com.diewebsiten.core.eventos.Eventos$ValidacionFormularios.call(Eventos.java:157)
//			at com.diewebsiten.core.eventos.Eventos$ValidacionFormularios.call(Eventos.java:144)
			// Esto al parecer solo pasa cuando ya se ejecuta desde las transacciones de un evento
			// sin embargo.. hay que crear unit tests antes de encontrar los errores y seguir refactorizando

    		ResultSet resultadoEjecucion = obtenerResultSet(sentencia, parametros);

    		if (resultadoEjecucion.isExhausted()) {
    			return (tipoResultado == PLANO) ? arrayNodeVacio() : objectNodeVacio();
    		}

			Stream<Map<String, Object>> resultadoTransformado = transformarResultadoEjecucion(resultadoEjecucion);
			return new ResultadoTransaccion(resultadoTransformado, sentencia, tipoResultado).obtenerResultado();

		} catch (Throwable e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }

	public static ResultSet obtenerResultSet(Cassandra sentencia, Object[] parametros) {
		return isEmpty(parametros) ? sesion.execute(sentencia.getQueryString())
                                   : sesion.execute(sentencia.getSentenciaPreparada().bind(parametros));
	}

	private Stream<Map<String, Object>> transformarResultadoEjecucion(ResultSet resultadoEjecucion) {
		return resultadoEjecucion.all().stream()
				.map(fila -> fila.getColumnDefinitions().asList().stream()
						.collect(toMap(Definition::getName, columna -> obtenerValorColumnaActual(fila, columna)))
				);
	}

	private Object obtenerValorColumnaActual(Row fila, Definition columnaActual) {
		ByteBuffer byteBuffer = fila.getBytesUnsafe(columnaActual.getName());
		TypeCodec tipoValor = new CodecRegistry().codecFor(columnaActual.getType());
		Optional valorColumnaActual = Optional.ofNullable(tipoValor.deserialize(byteBuffer, ProtocolVersion.NEWEST_SUPPORTED));
		return valorColumnaActual.orElseGet(() -> obtenerValorVacio(columnaActual.getType(), tipoValor.getJavaType()));
	}

	private Object obtenerValorVacio(DataType cassandraType, TypeToken javaType) {
		String tipoColumna = javaType.getRawType().getSimpleName();
		if (cassandraType.isCollection()) {
			return "List".equals(tipoColumna) ? new ArrayList<>() : new HashMap();
		} else {
			if ("String".equals(tipoColumna)) return "";
			if (TIPOS_NUMERICOS.contains(tipoColumna)) return 0;
			return null;
		}
	}

}
