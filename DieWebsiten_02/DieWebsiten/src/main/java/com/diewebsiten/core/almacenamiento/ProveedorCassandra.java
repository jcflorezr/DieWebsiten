package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.exceptions.DriverException;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.common.reflect.TypeToken;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.diewebsiten.core.almacenamiento.Proveedores.guardarNuevaSentencia;
import static com.diewebsiten.core.almacenamiento.Proveedores.obtenerSentenciaExistente;
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

    private static final String CASSANDRA_URL = "127.0.0.1";
    private static final int CASSANDRA_PORT = 9042;
	private static final List<String> TIPOS_NUMERICOS = asList("Integer","Long","Float","Double","BigDecimal","BigInteger");

	ProveedorCassandra(){}

	@Override
	void conectar() {
		try {
			cluster = Optional.ofNullable(Cluster.builder().addContactPoint(CASSANDRA_URL).withPort(CASSANDRA_PORT).build());
			sesion = cluster.get().connect();
		} catch (DriverException e) {
			throw new ExcepcionGenerica(e);
		}
    }
    
	@Override
	void desconectar() {
		cluster.ifPresent(Cluster::close);
    }
	
    @Override
	Supplier<Stream<Map<String, Object>>> ejecutarTransaccion(String sentencia, Object[] parametros) {



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

		PreparedStatement sentenciaPreparada = prepararSentencia(sentencia);

		int numFiltrosSentencia = sentenciaPreparada.getVariables().size();
		parametros = Optional.ofNullable(parametros).orElse(new Object[]{});

		if (parametros.length != numFiltrosSentencia) throw new ExcepcionGenerica("La sentencia necesita " + numFiltrosSentencia + " parámetros para ser ejecutada.");

		ResultSet resultadoEjecucion = obtenerResultSet(sentenciaPreparada, parametros);
		return () -> resultadoEjecucion.isExhausted() ? Stream.empty()
													  : transformarResultadoEjecucion(resultadoEjecucion);
    }

    private PreparedStatement prepararSentencia(String sentencia) {
		PreparedStatement sentenciaPreparada = (PreparedStatement) obtenerSentenciaExistente(sentencia);
		if (sentenciaPreparada == null) {
			sentenciaPreparada = sesion.prepare(sentencia);
			guardarNuevaSentencia(sentencia, sentenciaPreparada);
		}
		return sentenciaPreparada;
	}

	private ResultSet obtenerResultSet(PreparedStatement sentenciaPreparada, Object[] parametros) {
		if (isEmpty(parametros))
			return sesion.execute(sentenciaPreparada.getQueryString());
		return sesion.execute(sentenciaPreparada.bind(parametros));
	}

	private Stream<Map<String, Object>> transformarResultadoEjecucion(ResultSet resultadoEjecucion) {
		return resultadoEjecucion.all().stream()
				.map(fila ->
						fila.getColumnDefinitions().asList().stream()
						.collect(toMap(Definition::getName, columna -> obtenerValorColumnaActual(fila, columna)))
				);
	}

	private Object obtenerValorColumnaActual(Row fila, Definition columnaActual) {
		ByteBuffer byteBuffer = fila.getBytesUnsafe(columnaActual.getName());
		CodecRegistry codec = new CodecRegistry();
		TypeCodec tipoValor = codec.codecFor(columnaActual.getType());
		// TODO como mockear esto pa que no devuelva un ""
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
