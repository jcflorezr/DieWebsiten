package com.diewebsiten.core.almacenamiento.dto.sentencias.cassandra;

import com.diewebsiten.core.almacenamiento.dto.sentencias.Sentencia;
import com.diewebsiten.core.almacenamiento.dto.sentencias.SentenciasFactoryTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CassandraFactory.class)
public class CassandraFactoryTest {

    private String queryString;
    private boolean sentenciaSimple;
    @Mock
    Sentencia sentencia;
    @Mock
    private Cassandra cassandra;
    @InjectMocks
    private CassandraFactory cassandraFactory;

    @Before
    public void setUp() {
        queryString = "SELECT key_aliases, column_aliases FROM system.schema_columnfamilies WHERE keyspace_name = ? AND columnfamily_name = ?;";
        sentenciaSimple = false;
    }

    @Test
    public void crearSentenciaCassandra() throws Exception {
        OngoingStubbing<Sentencia> sentencia = new SentenciasFactoryTest().obtenerSentenciaTest();
        mockStatic(CassandraFactory.class);
//        cassandra = PowerMockito.mock(Cassandra.class);
//        when(new Cassandra(queryString, sentenciaSimple)).thenReturn(cassandra);
//        Sentencia sentencia = mock(Sentencia.class);
//        whenNew(Cassandra.class).withArguments(anyString(), anyBoolean()).thenReturn(cassandra);
        whenNew(CassandraFactory.class).withArguments(queryString, sentenciaSimple).thenReturn(cassandraFactory);
//        when(cassandraFactory.crearSentencia()).thenReturn(sentencia);
//        new CassandraFactory(queryString, sentenciaSimple).crearSentencia();
        when(cassandraFactory.crearSentencia()).thenReturn(cassandra);

    }

//    private Sentencia establecerSentenciaMock() {
//
//    }

}