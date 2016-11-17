package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Cluster.class, CodecRegistry.class})
@PowerMockIgnore("javax.management.*")
public class ProveedorCassandraTest {

    @Mock
    private Session sesion;
    @Mock
    private PreparedStatement sentenciaPreparada;
    @Mock
    private ResultSet resultadoEjecucion;

    private List<Row> resultadoEjecucionList = new ArrayList<>();
    private ProveedorCassandra proveedorCassandra;
    private String sentencia = "";
    private Object[] parametros = {""};

    @Before
    public void setUp() throws Exception {
        Constructor<ProveedorCassandra> proveedorCassandraConstructor = ProveedorCassandra.class.getDeclaredConstructor(new Class[0]);
        proveedorCassandraConstructor.setAccessible(true);
        proveedorCassandra = proveedorCassandraConstructor.newInstance();
        mockearClusterYSesion();
    }

    private void mockearClusterYSesion() {
        // Cluster
        Cluster cluster = mock(Cluster.class);
        mockStatic(Cluster.class);
        Cluster.Builder builder = mock(Cluster.Builder.class);
        when(Cluster.builder()).thenReturn(builder);
        when(builder.addContactPoint(anyString())).thenReturn(builder);
        when(builder.withPort(anyInt())).thenReturn(builder);
        when(builder.build()).thenReturn(cluster);
        // Sesión
        when(cluster.connect()).thenReturn(sesion);
        // Conectar
        proveedorCassandra.conectar();
    }

    private void mockearSentenciaPreparada(int numFiltrosSentencia) {
        when(sesion.prepare(sentencia)).thenReturn(sentenciaPreparada);
        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        when(sentenciaPreparada.getVariables()).thenReturn(columnDefinitions);
        when(columnDefinitions.size()).thenReturn(numFiltrosSentencia);
    }

    private void mockearResultSet() throws Exception {
        listRow();
        BoundStatement boundStatement = mock(BoundStatement.class);
        when(sentenciaPreparada.bind(parametros)).thenReturn(boundStatement);
        when(sesion.execute(boundStatement)).thenReturn(resultadoEjecucion);
    }

    private void listRow() throws Exception {
        /** Column Definitions **/
        Constructor<Definition> definitionC = (Constructor<Definition>) Definition.class.getDeclaredConstructors()[0];
        definitionC.setAccessible(true);

        Constructor<DataType.NativeType> nativeTypeC = (Constructor<DataType.NativeType>) DataType.NativeType.class.getDeclaredConstructors()[0];
        nativeTypeC.setAccessible(true);

        Definition[] defs = {
                definitionC.newInstance(null, null, "name1", nativeTypeC.newInstance(DataType.Name.VARCHAR, null)),
                definitionC.newInstance(null, null, "name2", nativeTypeC.newInstance(DataType.Name.VARCHAR, null)),
                definitionC.newInstance(null, null, "name3", nativeTypeC.newInstance(DataType.Name.VARCHAR, null))
        };

        Constructor<ColumnDefinitions> columnDefinitionsC = (Constructor<ColumnDefinitions>) ColumnDefinitions.class.getDeclaredConstructors()[0];
        columnDefinitionsC.setAccessible(true);

        Row fila = mock(Row.class);
        resultadoEjecucionList.add(fila);

        when(resultadoEjecucion.all()).thenReturn(resultadoEjecucionList);

        when(fila.getColumnDefinitions()).thenReturn(columnDefinitionsC.newInstance(defs, null));
        ByteBuffer byteBuffer = mock(ByteBuffer.class);
        when(fila.getBytesUnsafe(anyString())).thenReturn(byteBuffer);
        // TODO como mockear a TypeCodec y a CodecRegistry, puesto que son classes final
        TypeCodec typeCodec = mock(TypeCodec.class);
        CodecRegistry codecRegistry = mock(CodecRegistry.class);
        when(codecRegistry.codecFor(anyObject())).thenReturn(typeCodec);
        when(typeCodec.deserialize(byteBuffer, ProtocolVersion.NEWEST_SUPPORTED)).thenReturn(null);

    }

    @Test
    public void ejecutarTransaccion() throws Exception {
        mockearSentenciaPreparada(parametros.length);
        mockearResultSet();
        System.out.println(proveedorCassandra.ejecutarTransaccion(sentencia, parametros).get().collect(toList()));


    }

}