package com.diewebsiten.core.almacenamiento;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Proveedores.class)
public class ProveedoresTest {

    private static final String CASSANDRA = "CASSANDRA";
    private static final String MYSQL = "MYSQL";
    private static final String INVALIDO = "INVÁLIDO";

    @Mock
    private ProveedorAlmacenamiento proveedorAlmacenamiento;

    @Test
    public void ejecutarTransaccionCassandra() throws Exception {
        String sentencia = "SELECT transaccion, filtrossentencia, orden FROM diewebsiten.eventos;";
        ejecutarTransaccion(CASSANDRA, sentencia, new Object[]{""});
    }

    @Test(expected = ExcepcionGenerica.class)
    public void transaccionSinSentencia() throws Exception {
        ejecutarTransaccion(CASSANDRA, "", new Object[]{""});
    }

    @Test(expected = ExcepcionGenerica.class)
    public void transaccionConNombreDeMotorAlmacenamientoInvalido() throws Exception {
        ejecutarTransaccion(INVALIDO, "", new Object[]{""});
    }

    private void ejecutarTransaccion(String nombreBaseDeDatos, String sentencia, Object[] parametros) throws Exception {
        spy(Proveedores.class);
        doReturn(proveedorAlmacenamiento).when(Proveedores.class, "obtenerProveedorAlmacenamiento", anyObject());
        Proveedores.ejecutarTransaccion(nombreBaseDeDatos, sentencia, parametros);
    }

}