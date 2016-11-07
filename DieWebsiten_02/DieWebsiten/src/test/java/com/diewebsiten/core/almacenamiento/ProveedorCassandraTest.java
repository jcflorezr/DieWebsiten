package com.diewebsiten.core.almacenamiento;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.diewebsiten.core.almacenamiento.dto.sentencias.columnares.cassandra.Cassandra;
import com.diewebsiten.core.eventos.dto.Transaccion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.BiFunction;
import java.util.function.Function;

@RunWith(MockitoJUnitRunner.class)
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ProveedorCassandra.class, Cassandra.class})
public class ProveedorCassandraTest {

    @Mock
    private Transaccion transaccion;
    @Mock
    private Session sesion;
    @Mock
    private ResultSet resultadoEjecucion;

    @Mock
    private Cassandra sentencia;

    @Mock
    private Function<String, ResultSet> obtenerResultSet;
    @Mock
    private BiFunction<Cassandra, Object[], ResultSet> obtenerResultSetParametros;

    @InjectMocks
    private ProveedorCassandra proveedorCassandra;


//    @Before
//    public void setUp() {
//        mockStatic(ProveedorCassandra.class);
//    }

    @Test
    public void transaccionExitosa() {
//        when(transaccion.getQueryString()).thenReturn("sentencia");
//        when(transaccion.getNombre()).thenReturn("nombreSentencia");
//        when(transaccion.getParametrosTransaccion()).thenReturn(new Object[]{});
//        when(transaccion.getTipoResultado()).thenReturn(JERARQUÍA_CON_NOMBRES_DE_COLUMNAS);
//        mockStatic(Cassandra.class);
////        when(Cassandra.obtenerSentencia(any(Session.class), anyString(), anyString())).thenReturn(sentencia);
//
//        // NO HE ENCONTRADO LA FORMA DE MOCKEAR EL Function Y EL BiFunction
//        when(false ? obtenerResultSet.apply(anyString()) : obtenerResultSetParametros.apply(eq(sentencia), anyObject())).thenReturn(resultadoEjecucion);
//
//        proveedorCassandra.ejecutarTransaccion(transaccion);

    }

    @Test
    public void ejecutarTransaccion() throws Exception {
//        Transaccion t = obtenerDatosTransaccionEventos("SELECT tipo_transaccion FROM diewebsiten.tipos_de_transacciones;", "consultarTiposTransacciones", new Object[]{})
//        .setTipoResultado(JERARQUÍA_CON_NOMBRES_DE_COLUMNAS);
//        Transaccion t2 = obtenerDatosTransaccionEventos("SELECT column_name, type FROM system.schema_columns WHERE keyspace_name = ? AND columnfamily_name = ?;", "consultarTiposColumnas", new Object[]{})
//                .setParametrosTransaccion(new Object[]{"diewebsiten", "eventos"})
//                .setTipoResultado(JERARQUÍA_CON_NOMBRES_DE_COLUMNAS);
//        Transaccion t3 = obtenerDatosTransaccionEventos("SELECT column_name, type FROM system.schema_columns WHERE keyspace_name = ? AND columnfamily_name = ?;", "consultarTiposColumnas", new Object[]{})
//                .setParametrosTransaccion(new Object[]{"diewebsiten", "eventos"})
//                .setTipoResultado(JERARQUÍA);
//        Transaccion t4 = obtenerDatosTransaccionEventos("SELECT tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupovalidacion = ?;", "SentenciaValidacionesEvento", new Object[]{})
//                .setParametrosTransaccion(new Object[]{"CampoTipoCaracterConEspaciosMinusculasGuionBajo"})
//                .setTipoResultado(JERARQUÍA);


//        try(Proveedores almacenamiento = new Proveedores()) {
//
//            System.out.println(Proveedores.obtenerProveedorAlmacenamiento(t2.getMotorAlmacenamiento()).ejecutarTransaccion(t2));
//            System.out.println(Proveedores.obtenerProveedorAlmacenamiento(t3.getMotorAlmacenamiento()).ejecutarTransaccion(t3));
//            System.out.println(Proveedores.obtenerProveedorAlmacenamiento(t4.getMotorAlmacenamiento()).ejecutarTransaccion(t4));
//            String json = "{\"tipo_transaccion\":[\"SELECT\",\"DELETE\",\"UPDATE\",\"INSERT\"]}";
////            assertEquals(json,Proveedores.obtenerProveedorAlmacenamiento(t.getMotorAlmacenamiento()).ejecutarTransaccion(t).toString());
//            System.out.println(Proveedores.obtenerProveedorAlmacenamiento(t.getMotorAlmacenamiento()).ejecutarTransaccion(t));
//        }

    }


}