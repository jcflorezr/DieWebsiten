package com.diewebsiten.core.almacenamiento;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Proveedores.class)
public class ProveedoresTest {

    private static final String CASSANDRA = "CASSANDRA";
    private static final String MYSQL = "MYSQL";

    private Supplier<Stream<Map<String, Object>>> resultado;

    @Mock
    private ProveedorAlmacenamiento proveedorAlmacenamiento;

    @Test
    public void ejecutarTransaccionCassandra() throws Exception {

        ejecutarTransaccion(CASSANDRA, "dsdsd", new Object[]{""});

    }

    private void ejecutarTransaccion(String nombreBaseDeDatos, String sentencia, Object[] parametros) throws Exception {
//        spy(Proveedores.class);
//        doReturn(proveedorAlmacenamiento).when(Proveedores.class, "obtenerProveedorAlmacenamiento", anyObject());
        // TODO como mockear el metodo void conectar() ???
        doNothing().when(proveedorAlmacenamiento).conectar();
        when(proveedorAlmacenamiento.ejecutarTransaccion(sentencia, parametros)).thenReturn(resultado);
        Proveedores.ejecutarTransaccion(nombreBaseDeDatos, sentencia, parametros);
    }

}