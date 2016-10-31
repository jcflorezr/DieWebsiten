package com.diewebsiten.core.almacenamiento.dto.sentencias;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(SentenciasFactory.class)
public class SentenciasFactoryTest {

    @Test
    public OngoingStubbing<Sentencia> obtenerSentenciaTest() {
        mockStatic(SentenciasFactory.class);
        return when(SentenciasFactory.obtenerSentenciaExistente(anyString()));
    }

}