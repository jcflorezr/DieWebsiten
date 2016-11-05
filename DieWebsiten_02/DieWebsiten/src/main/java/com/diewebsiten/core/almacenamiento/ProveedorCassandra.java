package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.exceptions.DriverException;
import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencias;
import com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra.Cassandra;
import com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra.CassandraFactory;
import com.diewebsiten.core.eventos.dto.Transaccion;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia.TiposResultado;
import static com.diewebsiten.core.almacenamiento.util.Sentencias.LLAVES_PRIMARIAS;
import static com.diewebsiten.core.util.Transformaciones.agruparValores;
import static com.diewebsiten.core.util.Transformaciones.ponerObjeto;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
    public static Function<String, ResultSet> obtenerResultSet = (sentencia) -> sesion.execute(sentencia);
    public static BiFunction<Cassandra, Object[], ResultSet> obtenerResultSetParametros = (sentencia, parametros) -> sesion.execute(sentencia.getSentenciaPreparada().bind(parametros));

    private static final String CASSANDRA_URL = "127.0.0.1";
    private static final int CASSANDRA_PORT = 9042;
    private static final ObjectMapper MAPPER = new ObjectMapper();

	ProveedorCassandra(){}

	@Override
	void conectar() {
		try {
			cluster = Optional.ofNullable(Cluster.builder().addContactPoint(CASSANDRA_URL).withPort(CASSANDRA_PORT).build());
			sesion = cluster.get().connect();
			Sentencias.obtenerSentencia(new CassandraFactory(LLAVES_PRIMARIAS.sentencia(), true));
		} catch (DriverException e) {
			throw new ExcepcionGenerica(e);
		}
    }
    
	@Override
	void desconectar() {
		cluster.ifPresent(Cluster::close);
    }
	
    @Override
	JsonNode ejecutarTransaccion(Transaccion transaccion) throws ExcepcionGenerica {

		String nombreTransaccion = transaccion.getNombre();
		String sentenciaCQL = transaccion.getSentencia();
    	Object[] parametros = Optional.ofNullable(transaccion.getParametrosTransaccion()).orElse(new Object[]{});
    	TiposResultado tipoResultado = transaccion.getTipoResultado();

    	if (isBlank(nombreTransaccion)) {
    		throw new ExcepcionGenerica("El nombre de la sentencia no puede venir vacío.");
    	}

    	try {

			Cassandra sentencia = (Cassandra) Sentencias.obtenerSentencia(new CassandraFactory(sentenciaCQL, false));

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
    		ResultSet resultadoEjecucion = isEmpty(parametros) ? obtenerResultSet.apply(sentenciaCQL)
															   : obtenerResultSetParametros.apply(sentencia, parametros);

    		if (resultadoEjecucion.isExhausted()) {
    			if (TiposResultado.PLANO.equals(tipoResultado)) return MAPPER.createArrayNode();
    			else return MAPPER.createObjectNode();
    		}

			return new Estructura(resultadoEjecucion, sentencia).obtenerResultado(tipoResultado);

		} catch (Throwable e) {
			// ES NECESARIO IMPRIMIR LOS PARAMETROS??? PUESTO QUE YA SE IMPRIMIRÁN DESDE LOS EVENTOS
			throw new ExcepcionGenerica("Error al ejecutar la sentencia CQL --> " + sentenciaCQL + "'. Parámetros: " + (parametros != null ? Arrays.asList(parametros).toString() : "{}") + ". Mensaje original --> " + Throwables.getStackTraceAsString(e));
		}
    }

    private class Estructura {
    	
    	private ObjectNode coleccionActual, resultado = MAPPER.createObjectNode();
    	private ResultSet resultadoEjecucion;
    	private Cassandra sentencia;

		private Estructura(ResultSet resultadoEjecucion, Cassandra sentencia) {
			this.resultadoEjecucion = resultadoEjecucion;
//			synchronized (this) { sentencia.enriquecerSentencia(sesion, resultadoEjecucion, sentencia); }
			this.sentencia = sentencia;
		}

		private Supplier<Stream<Map<String, Object>>> transformarResultadoEjecucion() {
			Supplier<Stream<Row>> filasStream = () -> StreamSupport.stream(resultadoEjecucion.spliterator(), false);
			Function<Row, Stream<Definition>> columnasStream = (fila) -> StreamSupport.stream(fila.getColumnDefinitions().spliterator(), false);
			return () -> filasStream.get().parallel().map(fila -> columnasStream.apply(fila)
										  			 .collect(toMap(Definition::getName, columna -> obtenerValorColumnaActual(fila, columna))));
		}

		private JsonNode obtenerResultado(TiposResultado tipoResultado) {
			switch (tipoResultado) {
				case PLANO: return obtenerResultadoPlano();
				case JERARQUÍA: return obtenerResultadoConJerarquia(false);
				case JERARQUÍA_CON_NOMBRES_DE_COLUMNAS: return obtenerResultadoConJerarquia(true);
				default: throw new ExcepcionGenerica("El tipo de resultado: '" + tipoResultado + "', no es válido.");
			}
		}

        private ArrayNode obtenerResultadoPlano() {
			List<Map<String, Object>> resultSet = transformarResultadoEjecucion().get().collect(toList());
			return MAPPER.convertValue(resultSet, ArrayNode.class);
        }

    	private ObjectNode obtenerResultadoConJerarquia (boolean incluirNombresColumnasPrimarias) {
			Supplier<Stream<Map<String, Object>>> resultSet = transformarResultadoEjecucion();
    		coleccionActual = resultado;
			resultSet.get().parallel().forEach(fila -> {
				sentencia.getColumnasIntermedias().get()
						.forEach(columnaIntermedia -> {
							if (incluirNombresColumnasPrimarias) coleccionActual = ponerObjeto.apply(coleccionActual, columnaIntermedia);
							coleccionActual = ponerObjeto.apply(coleccionActual, fila.get(columnaIntermedia).toString());
						});
				sentencia.getColumnasRegulares().get()
						.forEach(columnaRegular -> agruparValores(coleccionActual, columnaRegular, MAPPER.valueToTree(fila.get(columnaRegular))));
				coleccionActual = resultado;
			});
    		return resultado;
    	}
    	
    	private Object obtenerValorColumnaActual(Row fila, Definition columnaActual) {
			return Optional.ofNullable(fila.getBytesUnsafe(columnaActual.getName()))
    					   .map(buffer -> new CodecRegistry().codecFor(columnaActual.getType()).deserialize(buffer, ProtocolVersion.NEWEST_SUPPORTED))
    					   .orElse(obtenerValorVacio(columnaActual));
    	}
    	
    	/**
    	 * Si el valor de la columna actual es nulo se guardará como valor un "valor vacío"
    	 * dependiendo del tipo de variable al que pertenezca la columna actual
    	 *
    	 * @param columnaActual
    	 * @return
    	 */
    	private Object obtenerValorVacio(ColumnDefinitions.Definition columnaActual) {
    		String tipoColumna = new CodecRegistry().codecFor(columnaActual.getType()).getJavaType().getRawType().getSimpleName();
    		if (columnaActual.getType().isCollection()) {
    			return "List".equals(tipoColumna) ? new ArrayList<String>() : new HashMap<>();
    		} else {    							
    			if ("String".equals(tipoColumna))
    				return "";
    			else if ("Integer,Long,Float,Double,BigDecimal,BigInteger".indexOf(tipoColumna) > -1)
    				return 0;
    			else
    				return null;
    		}
    	}

    }

}
