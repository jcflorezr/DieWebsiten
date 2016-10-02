package com.diewebsiten.core.almacenamiento.dto;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
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
import static java.util.stream.Collectors.toList;

public class SentenciaCassandra extends Sentencia {

	private static Object obj = new Object();

	private Optional<String> keyspaceName;
	private Optional<String> columnfamilyName;
	private PreparedStatement sentenciaPreparada;
	private List<String> parametrosSentencia;
	private List<String> columnasIntermedias;
	private List<String> columnasRegulares;

	public SentenciaCassandra() {}

	private SentenciaCassandra(PreparedStatement sentenciaPreparada, List<String> parametrosSentencia) {
		this.sentenciaPreparada = sentenciaPreparada;
		this.parametrosSentencia = Optional.ofNullable(parametrosSentencia).orElse(new ArrayList<>());
		this.keyspaceName = Optional.empty();
		this.columnfamilyName = Optional.empty();
		this.columnasIntermedias = new ArrayList<>();
		this.columnasRegulares = new ArrayList<>();
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
	
	public Supplier<Stream<String>> getColumnasIntermedias() {
		return () -> columnasIntermedias.stream();
	}
	
	public void setColumnasIntermedias(List<String> columnasIntermedias) {
		this.columnasIntermedias = Optional.ofNullable(columnasIntermedias).orElse(new ArrayList<>());
	}
	
	public int getNumeroColumnasIntermedias() {
		return columnasIntermedias.size();
	}
	
	public Supplier<Stream<String>> getColumnasRegulares() {
		return () -> columnasRegulares.stream();
	}

	public void setColumnasRegulares(List<String> columnasRegulares) {
		this.columnasRegulares = Optional.ofNullable(columnasRegulares).orElse(new ArrayList<>());
	}
	
	public int getNumeroColumnasRegulares() {
		return columnasRegulares.size();
	}

	private boolean columnasEstanCategorizadas(SentenciaCassandra sentencia) {
		return sentencia.getNumeroColumnasIntermedias() == 0 && sentencia.getNumeroColumnasRegulares() == 0;
	}

	/**
	 * Tener sentencias preparadas con el fin de reusarlas
	 * @param sentenciaCQL
	 * @param nombreTransaccion
	 * @return
	 */
	 public SentenciaCassandra obtenerSentencia(Session sesion, String sentenciaCQL, String nombreTransaccion) {
		try {
			Optional<Sentencia> sentencia = Sentencia.getSentenciaPreparada(nombreTransaccion);
			if (!sentencia.isPresent()) {
				synchronized (obj) {
					sentencia =  Sentencia.getSentenciaPreparada(nombreTransaccion);
					if (!sentencia.isPresent()) {
						PreparedStatement sentenciaPreparada = sesion.prepare(sentenciaCQL);
						List<String> parametrosSentencia = StreamSupport.stream(sentenciaPreparada.getVariables().spliterator(), false)
								.map(Definition::getName).collect(toList());
						sentencia = Optional.of(new SentenciaCassandra(sentenciaPreparada, parametrosSentencia));
						Sentencia.setSentenciaPreparada(nombreTransaccion, sentencia.get());
					}
				}
			}
			return (SentenciaCassandra) sentencia.get();
		} catch (ClassCastException e) {
			throw new ExcepcionGenerica("La sentencia de la transacción '" + nombreTransaccion + "' no es de tipo Cassandra");
		}
	}

	public void enriquecerSentencia(Session sesion, ResultSet resultadoEjecucion, SentenciaCassandra sentencia) {
		Supplier<Stream<Definition>> columnasResultado = () -> StreamSupport.stream(resultadoEjecucion.getColumnDefinitions().spliterator(), false);
		columnasResultado.get().limit(1).forEach(columna -> {sentencia.setKeyspaceName(columna.getKeyspace());
															 sentencia.setColumnfamilyName(columna.getTable());});
		if (columnasEstanCategorizadas(sentencia)) {
			categorizarColumnas(sesion, () -> columnasResultado.get().map(Definition::getName), sentencia);
		}
	}

	private void categorizarColumnas(Session sesion, Supplier<Stream<String>> columnasResultado, SentenciaCassandra sentencia) {
		SentenciaCassandra sentenciaLlavesPrimarias = obtenerSentencia(sesion, Sentencias.LLAVES_PRIMARIAS.sentencia(), Sentencias.LLAVES_PRIMARIAS.nombre());
		Row llavesPrimarias = sesion.execute(sentenciaLlavesPrimarias.getSentenciaPreparada().bind(sentencia.getKeyspaceName().get(), sentencia.getColumnfamilyName().get())).one();
		int numColumnasResultado = (int) columnasResultado.get().count();
		if (numColumnasResultado == 1) {
			sentencia.setColumnasRegulares(columnasResultado.get().collect(toList()));
		}
		// Columnas intermedias
		sentencia.setColumnasIntermedias(Stream.of(stringToList(llavesPrimarias.getString("key_aliases"), String.class),
												   stringToList(llavesPrimarias.getString("column_aliases"), String.class))
				.flatMap(List::stream)
				.filter(llavePrimaria -> sentencia.getParametrosSentencia().get().noneMatch(parametro -> llavePrimaria.equals(parametro)))
				.map(columnaIntermedia -> columnasResultado.get().filter(columna -> columna.equals(columnaIntermedia)).findFirst().get())
				.collect(toList()));
		if (getNumeroColumnasIntermedias() > 0 && numColumnasResultado == getNumeroColumnasIntermedias()) {
			columnasIntermedias.remove(getNumeroColumnasIntermedias() - 1);
		}
		// Columnas regulares
		sentencia.setColumnasRegulares(
				columnasResultado.get().filter(columna -> sentencia.getColumnasIntermedias().get().noneMatch(columnaIntermedia -> columnaIntermedia.equals(columna)))
						.collect(toList()));
	}

}
