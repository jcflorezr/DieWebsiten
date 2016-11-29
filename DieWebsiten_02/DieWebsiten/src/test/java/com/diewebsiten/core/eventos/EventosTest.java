package com.diewebsiten.core.eventos;

import com.diewebsiten.core.eventos.dto.transaccion.Transaccion;
import com.diewebsiten.core.eventos.dto.transaccion.Transacciones;
import com.diewebsiten.core.eventos.dto.transaccion.columnar.Cassandra;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.JsonUtils;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Transacciones.class})
public class EventosTest {

    private static final String RUTA_RAIZ_EVENTOS = "com/diewebsiten/core/eventos/";

    @Mock
    private Cassandra transaccionCassandra;
    private JsonUtils jsonUtils = new JsonUtils();
//    private Cassandra transaccionCassandraSpy;

    @Test
    public void ejecutarEvento() {

        String sentencia = "";
        Object parametros = new Object[]{""};
//        transaccionCassandra = nuevaTransaccionCassandra(sentencia, parametros);
//        transaccionCassandraSpy = spy(transaccionCassandra);
        mockStatic(Transacciones.class);
        when(Transacciones.nuevaTransaccionCassandra(anyString(), anyObject())).thenReturn(transaccionCassandra);
        ObjectNode formularioJson = (ObjectNode) obtenerJsonDesdeArchivo(RUTA_RAIZ_EVENTOS + "Formulario.json");
        when(transaccionCassandra.enJerarquiaConNombres()).thenReturn(formularioJson);
        when(transaccionCassandra.plana()).thenReturn(formularioJson);

    }

//    private Cassandra nuevaTransaccionCassandra(String sentencia, Object... parametros) {
//        return Transacciones.nuevaTransaccionCassandra(sentencia, parametros);
//    }

    private JsonNode obtenerJsonDesdeArchivo(String nombreArchivo) {
        return jsonUtils.obtenerJsonDesdeArchivo(nombreArchivo);
    }

}