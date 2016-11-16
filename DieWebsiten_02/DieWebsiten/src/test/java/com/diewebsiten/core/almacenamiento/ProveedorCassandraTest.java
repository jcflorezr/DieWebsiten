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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Cluster.class})
@PowerMockIgnore("javax.management.*")
public class ProveedorCassandraTest {

    private ProveedorCassandra proveedorCassandra;

    @Mock
    private Cluster.Builder builder;
    @Mock
    private Optional<Cluster> clusterOptional;
    @Mock
    private Cluster cluster;
    @Mock
    private Session sesion;
    @Mock
    private PreparedStatement sentenciaPreparada;
    @Mock
    ColumnDefinitions columnDefinitions;
    @Mock
    private ResultSet resultadoEjecucion;
    @Mock
    private Row fila;
    @Mock
    private ByteBuffer byteBuffer;

    private List<Row> resultadoEjecucionList = new ArrayList<>();

    private String sentencia = "";
    private Object[] parametros = {""};

    @Before
    public void setUp() throws Exception {

        Constructor<ProveedorCassandra> proveedorCassandraConstructor = ProveedorCassandra.class.getDeclaredConstructor(new Class[0]);
        proveedorCassandraConstructor.setAccessible(true);
        proveedorCassandra = proveedorCassandraConstructor.newInstance();

        mockStatic(Cluster.class);
        when(Cluster.builder()).thenReturn(builder);
        when(builder.addContactPoint(anyString())).thenReturn(builder);
        when(builder.withPort(anyInt())).thenReturn(builder);
        when(builder.build()).thenReturn(cluster);

        when(cluster.connect()).thenReturn(sesion);

        proveedorCassandra.conectar();

        when(sesion.prepare(sentencia)).thenReturn(sentenciaPreparada);
        when(sentenciaPreparada.getVariables()).thenReturn(columnDefinitions);
        when(columnDefinitions.size()).thenReturn(parametros.length);



        when(sesion.execute(sentencia, parametros)).thenReturn(resultadoEjecucion);





//        mockStatic(ProveedorCassandra.class);
//        when(ProveedorCassandra.obtenerResultSet(anyObject(), anyObject())).thenReturn(resultadoEjecucion);

    }

    @Test
    public void ejecutarTransaccion() throws Exception {



        /** Column Definitions **/
        Constructor<Definition> c = (Constructor<Definition>) Definition.class.getDeclaredConstructors()[0];
        c.setAccessible(true);
//
//
//        Constructor<DataType> d1 = DataType.class.getDeclaredConstructor(DataType.Name.class);
//        d1.setAccessible(true);
//
//
        Constructor<DataType.NativeType> d = (Constructor<DataType.NativeType>) DataType.NativeType.class.getDeclaredConstructors()[0];
        d.setAccessible(true);





        Definition[] defs = {
                c.newInstance(null, null, "name1", d.newInstance(DataType.Name.VARCHAR, null)),
                c.newInstance(null, null, "name2", d.newInstance(DataType.Name.VARCHAR, null)),
                c.newInstance(null, null, "name3", d.newInstance(DataType.Name.VARCHAR, null))
        };

        Constructor<ColumnDefinitions> c2 = (Constructor<ColumnDefinitions>) ColumnDefinitions.class.getDeclaredConstructors()[0];
        c2.setAccessible(true);


        when(fila.getColumnDefinitions()).thenReturn(c2.newInstance(defs, null));
        when(fila.getBytesUnsafe(anyString())).thenReturn(byteBuffer);

        resultadoEjecucionList.add(fila);

        when(resultadoEjecucion.all()).thenReturn(resultadoEjecucionList);

//        Optional valorColumnaActual = Optional.ofNullable(tipoValor.deserialize(byteBuffer, ProtocolVersion.NEWEST_SUPPORTED));


        // TODO voy aqui
        // hay que mockear un resultSet con datos de verdad

        proveedorCassandra.ejecutarTransaccion(sentencia, parametros);


    }

//    @Test
//    public void ejecutarTransaccionConResultadoVacio() {
//        crearTransaccion("PLANO", null);
//        assertEquals(arrayVacio, mockearResultadoVacio());
//    }
//
//    @Test
//    public void ejecutarTransaccionConResultadoVacioEnJerarquia() {
//        crearTransaccion("JERARQUÍA", null);
//        assertEquals(objetoVacio, mockearResultadoVacio());
//    }
//
//    @Test(expected = ExcepcionGenerica.class)
//    public void tipoResultadoInvalido() {
//        crearTransaccion("INVÁLIDO", null);
//        proveedorCassandra.ejecutarTransaccion(transaccion);
//    }
//
//    @Test(expected = ExcepcionGenerica.class)
//    public void parametrosNoCoinciden() throws Exception {
//        crearTransaccion("PLANO", null);
//        when(cassandra.numParametrosSentencia()).thenReturn(1);
//        proveedorCassandra.ejecutarTransaccion(transaccion);
//    }
//
//    private void crearTransaccion(String tipoResultado, Object... parametrosTransaccion) {
//        transaccion.setTipoResultado(tipoResultado);
//        transaccion.setParametrosTransaccion(parametrosTransaccion);
//    }
//
//    private JsonNode mockearResultadoVacio() {
//        when(cassandra.numParametrosSentencia()).thenReturn(0);
//        when(resultadoEjecucion.isExhausted()).thenReturn(true);
//        return proveedorCassandra.ejecutarTransaccion(transaccion);
//    }


}