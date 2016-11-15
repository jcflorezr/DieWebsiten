package com.diewebsiten.core.almacenamiento;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({Sentencias.class, CassandraFactory.class, ProveedorCassandra.class})
public class ProveedorCassandraTest {

//    private Transaccion transaccion;
//    private ProveedorCassandra proveedorCassandra;
//
//    private static final ObjectMapper MAPPER = new ObjectMapper();
//    private ObjectNode objetoVacio = MAPPER.createObjectNode();
//    private ArrayNode arrayVacio = MAPPER.createArrayNode();
//
//    @Mock
//    private Cassandra cassandra;
//    @Mock
//    private CassandraFactory cassandraFactory;
//    @Mock
//    private ResultSet resultadoEjecucion;
//    @Mock
//    private Row fila;
//    @Mock
//    private ByteBuffer byteBuffer;
//
//    private List<Row> resultadoEjecucionList = new ArrayList<>();
//
//    @Before
//    public void setUp() throws Exception {
//        transaccion = new Transaccion();
//        transaccion.setSentencia("s");
//
//        Constructor<ProveedorCassandra> proveedorCassandraConstructor = ProveedorCassandra.class.getDeclaredConstructor(new Class[0]);
//        proveedorCassandraConstructor.setAccessible(true);
//        proveedorCassandra = proveedorCassandraConstructor.newInstance();
//
//        whenNew(CassandraFactory.class).withArguments(anyObject(), anyBoolean()).thenReturn(cassandraFactory);
//
//        mockStatic(Sentencias.class);
//        when(Sentencias.obtenerSentencia(cassandraFactory)).thenReturn(cassandra);
//
//        mockStatic(ProveedorCassandra.class);
//        when(ProveedorCassandra.obtenerResultSet(anyObject(), anyObject())).thenReturn(resultadoEjecucion);
//
//    }
//
//    @Test
//    public void ejecutarTransaccion() throws Exception {
//
//
//        crearTransaccion("JERARQUÍA", null);
//
//        /** Column Definitions **/
//        Constructor<Definition> c = (Constructor<Definition>) Definition.class.getDeclaredConstructors()[0];
//        c.setAccessible(true);
////
////
////        Constructor<DataType> d1 = DataType.class.getDeclaredConstructor(DataType.Name.class);
////        d1.setAccessible(true);
////
////
//        Constructor<DataType.NativeType> d = (Constructor<DataType.NativeType>) DataType.NativeType.class.getDeclaredConstructors()[0];
//        d.setAccessible(true);
//
//
//
//
//
//        Definition[] defs = {
//                c.newInstance(null, null, "name1", d.newInstance(DataType.Name.VARCHAR, null)),
//                c.newInstance(null, null, "name2", d.newInstance(DataType.Name.VARCHAR, null)),
//                c.newInstance(null, null, "name3", d.newInstance(DataType.Name.VARCHAR, null))
//        };
//
//        Constructor<ColumnDefinitions> c2 = (Constructor<ColumnDefinitions>) ColumnDefinitions.class.getDeclaredConstructors()[0];
//        c2.setAccessible(true);
//
//
//        when(fila.getColumnDefinitions()).thenReturn(c2.newInstance(defs, null));
//        when(fila.getBytesUnsafe(anyString())).thenReturn(byteBuffer);
//
//        resultadoEjecucionList.add(fila);
//
//        when(resultadoEjecucion.all()).thenReturn(resultadoEjecucionList);
//
////        Optional valorColumnaActual = Optional.ofNullable(tipoValor.deserialize(byteBuffer, ProtocolVersion.NEWEST_SUPPORTED));
//
//
//        proveedorCassandra.ejecutarTransaccion(transaccion);
//
//
//    }
//
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