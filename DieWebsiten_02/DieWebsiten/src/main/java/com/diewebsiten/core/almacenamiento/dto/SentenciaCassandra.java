package com.diewebsiten.core.almacenamiento.dto;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.almacenamiento.util.Sentencias;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.diewebsiten.core.util.Transformaciones.stringToList;

public class SentenciaCassandra extends Sentencia {

	private static Object obj = new Object();

	private Optional<String> keyspaceName;
	private Optional<String> columnfamilyName;
	private PreparedStatement sentenciaPreparada;
	private List<String> parametrosSentencia;
	private List<Definition> columnasIntermedias;
	private List<Definition> columnasRegulares;
	
	private SentenciaCassandra init(PreparedStatement sentenciaPreparada, List<String> parametrosSentencia) {
		this.sentenciaPreparada = sentenciaPreparada;
		this.parametrosSentencia = Optional.ofNullable(parametrosSentencia).orElse(new ArrayList<>());
		this.keyspaceName = Optional.empty();
		this.columnfamilyName = Optional.empty();
		this.columnasIntermedias = new ArrayList<>();
		this.columnasRegulares = new ArrayList<>();
		return this;
	}

	
	public Optional<String> getKeyspaceName() {
		return keyspaceName;
	}

	public void setKeyspaceName(String keyspaceName) {
		this.keyspaceName = Optional.ofNullable(keyspaceName);
	}
	
	public PreparedStatement getSentenciaPreparada() {
		return sentenciaPreparada;
	}

	public void setSentenciaPreparada(PreparedStatement sentenciaPreparada) {
		this.sentenciaPreparada = sentenciaPreparada;
	}

	public Supplier<Stream<String>> getParametrosSentencia() {
		return () -> parametrosSentencia.stream();
	}

	public int getNumeroParametrosSentencia() {
		return parametrosSentencia.size();
	}

	public Optional<String> getColumnfamilyName() {
		return columnfamilyName;
	}

	public void setColumnfamilyName(String columnfamilyName) {
		this.columnfamilyName = Optional.ofNullable(columnfamilyName);
	}
	
	public Supplier<Stream<Definition>> getColumnasIntermedias() {
		return () -> columnasIntermedias.stream();
	}
	
	public void setColumnasIntermedias(List<Definition> columnasIntermedias) {
		this.columnasIntermedias = Optional.ofNullable(columnasIntermedias).orElse(new ArrayList<>());
	}
	
	public int getNumeroColumnasIntermedias() {
		return columnasIntermedias.size();
	}
	
	public Supplier<Stream<Definition>> getColumnasRegulares() {
		return () -> columnasRegulares.stream();
	}

	public void setColumnasRegulares(List<Definition> columnasRegulares) {
		this.columnasRegulares = Optional.ofNullable(columnasRegulares).orElse(new ArrayList<>());
	}
	
	public int getNumeroColumnasRegulares() {
		return columnasRegulares.size();
	}

	/**
	 * ESTE METODO DEBERIA ESTAR EN LA CLASE SentenciaCassandra
	 * Tener sentencias preparadas con el fin de reusarlas
	 * @param sentenciaCQL
	 * @param nombreTransaccion
	 * @return
	 */
	 public static SentenciaCassandra obtenerSentencia(Session sesion, String sentenciaCQL, String nombreTransaccion) {
		try {
			Optional<Sentencia> sentencia = Sentencia.getSentenciaPreparada(nombreTransaccion);
			if (!sentencia.isPresent()) {
				synchronized (obj) {
					sentencia =  Sentencia.getSentenciaPreparada(nombreTransaccion);
					if (!sentencia.isPresent()) {
						PreparedStatement sentenciaPreparada = sesion.prepare(sentenciaCQL);
						List<String> parametrosSentencia = StreamSupport.stream(sentenciaPreparada.getVariables().spliterator(), false)
								.map(Definition::getName)
								.collect(Collectors.toList());
						sentencia = Optional.of(new SentenciaCassandra().init(sentenciaPreparada, parametrosSentencia));
						Sentencia.setSentenciaPreparada(nombreTransaccion, sentencia.get());
					}
				}
			}
			return (SentenciaCassandra) sentencia.get();
		} catch (ClassCastException e) {
			throw new ExcepcionGenerica("La sentencia de la transacción '" + nombreTransaccion + "' no es de tipo Cassandra");
		}
	}

	public static void enriquecerSentencia(Session sesion, Supplier<Stream<Definition>> columnasResultado, SentenciaCassandra sentencia) {

		//LOS SIGUIENTES TRES CONDICIONALES SE PUEDEN OPTIMIZAR A UNO SOLO.. Y DEBERIAN ESTAR DENTRO DE LA CLASE SentenciaCassandra
		if (!sentencia.getKeyspaceName().isPresent()) sentencia.setKeyspaceName(columnasResultado.get().map(Definition::getKeyspace).findAny().get());
		if (!sentencia.getColumnfamilyName().isPresent()) sentencia.setColumnfamilyName(columnasResultado.get().map(Definition::getTable).findAny().get());

		//			ESTE HAY QUE MODIFICARLO
		if (sentencia.getNumeroColumnasIntermedias() == 0 && sentencia.getNumeroColumnasRegulares() == 0) {
			categorizarColumnas(sesion, columnasResultado, sentencia);
		}




	}

	private static void categorizarColumnas(Session sesion, Supplier<Stream<Definition>> columnasResultado, SentenciaCassandra sentencia) {
		SentenciaCassandra sentenciaLlavesPrimarias = obtenerSentencia(sesion, Sentencias.LLAVES_PRIMARIAS.sentencia(), Sentencias.LLAVES_PRIMARIAS.nombre());
		Row llavesPrimarias = sesion.execute(sentenciaLlavesPrimarias.getSentenciaPreparada().bind(sentencia.getKeyspaceName().get(), sentencia.getColumnfamilyName().get())).one();
		boolean noEsUnicaColumna = columnasResultado.get().count() > 1;
		if (noEsUnicaColumna) {
			// Columnas intermedias
			sentencia.setColumnasIntermedias(Stream.of(stringToList(llavesPrimarias.getString("key_aliases"), String.class),
													   stringToList(llavesPrimarias.getString("column_aliases"), String.class))
											.flatMap(List::stream)
											.filter(llavePrimaria -> sentencia.getParametrosSentencia().get().noneMatch(parametro -> llavePrimaria.equals(parametro)))
											.map(columnaIntermedia -> columnasResultado.get()
											.filter(columna -> columna.getName().equals(columnaIntermedia)).limit(1).findAny().get())
											.collect(Collectors.toList()));
			// Columnas regulares
			sentencia.setColumnasRegulares(
					columnasResultado.get().filter(columna -> sentencia.getColumnasIntermedias().get().noneMatch(columnaIntermedia -> columnaIntermedia.getName().equals(columna.getName())))
							.collect(Collectors.toList()));
		} else {
			// Columnas regulares (en caso de que haya una única columna en el resultSet)
			sentencia.setColumnasRegulares(columnasResultado.get().collect(Collectors.toList()));
		}
	}

}
