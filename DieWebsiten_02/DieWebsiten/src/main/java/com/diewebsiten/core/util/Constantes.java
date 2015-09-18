package com.diewebsiten.core.util;

public enum Constantes {
    
    CASSANDRA_URL("localhost"),
    CASSANDRA_PORT(9042),
    RUTA_LOG("\\users\\juancamiloroman\\Desktop\\Log"),
    NOMBRE_LOG("LogDieWebsiten"),
    ERROR("Por favor intente más tarde."),
    EXCEPCION_GENERICA("com.diewebsiten.modelo.Excepciones.ExcepcionGenerica"),
    NOMBRE_EVENTO_VACIO("No se ha especificado un evento."),    
    
    SENTENCIA_TRANSACCIONES("SELECT tipotransaccion, transaccion, tabla, sentenciacql, filtrossentenciacql FROM diewebsiten.eventos WHERE sitioweb = ? AND pagina = ? AND evento = ?"),
    //SENTENCIA_FORMULARIOS("SELECT campo, alias FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND tipotransaccion = ? AND transaccion = ?"),
    SENTENCIA_VALIDACIONES_EVENTO("SELECT columna, grupovalidacion, formaingreso, valorpordefecto FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND evento = ?"),
    //SENTENCIA_SENTENCIAS_CQL("SELECT clausula, campo, valorpordefecto FROM diewebsiten.sentencias_cql WHERE sitioweb = ? AND pagina = ? AND tipotransaccion = ? AND transaccion = ?"),
    
    COLLECTIONS_CASSANDRA("SetType,ListType,MapType"),
    TIPOS_LISTAS_SENTENCIAS_SELECT("simple,compuesta,parValoresSimple,parValoresCompuesta,parAgrupadaSimple,parAgrupadaCompuesta"),
    
    VALIDACION("Validación"),
    TRANSFORMACION("Transformación"),
    
    V_ALFANUMERICO_SIN_ESPACIOS,
    V_ALFANUMERICO_CON_ESPACIOS,
    V_NUMERICO_SIN_ESPACIOS,
    V_NUMERICO_CON_ESPACIOS,
    V_CARACTER_SIN_ESPACIOS,
    V_CARACTER_CON_ESPACIOS,
    V_EMAIL,
    V_FECHAHORA,
    V_URL,
    V_DOMINIO,
    V_PUNTO,
    V_OPCIONAL,
    
    T_EMAIL,
    T_CIFRADO,
    T_FECHAHORA,
    T_MINUSCULAS,
    T_MAYUSCULAS,
    T_IDIOMA,
    T_CAMELCASE_CLASE,
    T_CAMELCASE_METODO,
    T_GUIONBAJO;    
    
    private int constanteInt;
    private String constanteString;
    
    Constantes(){}
    
    Constantes(int constante) {
        this.constanteInt = constante;
    }
    
    Constantes(String constante) {
        this.constanteString = constante;
    }
    
    public int getInt(){
        return this.constanteInt;
    }
    
    public String getString(){
        return this.constanteString;
    }
    
    public boolean equals(String s) {
        return null == s ? false : s.equalsIgnoreCase(constanteString);
    }
    
    public enum Mensajes {
    
        EVENTO_NO_EXISTE {
            @Override
            public String getMensaje(String... valores) {
                return "El evento '" + valores[2] + "' de la página '" + valores[1] + "' del sitio web '" + valores[0] + "' no existe.";
            }
        },
        CAMPOS_FORMULARIO_NO_EXISTEN {
            @Override
            public String getMensaje(String... valores) {            
                return "No hay parámetros para la ejecución de los formularios del evento '" + valores[0] + "'.";
            }
        },
        VALIDACIONES_NO_EXISTEN {
            @Override
            public String getMensaje(String... valores) {            
                return "No se encontraron las validaciones o las transformaciones "
                     + "necesarias para los campos del formulario del evento '" + valores[2] + "'"
                     + " de la página '" + valores[1] + "' del sitio web '" + valores[0] + "'.";
            }
        },
        CAMPOSCQL_NO_EXISTEN {
            @Override
            public String getMensaje(String... valores) {            
                return "No se encontraron los filtros de la sentencia CQL de la transacción '" + valores[0] + "'" 
                     + "de tipo '" + valores[1] + "' que corresponde al evento '" + valores[2] + "' "
                     + "de la página '" + valores[3] + "' del sitio web '" + valores[4] + "'.";
            }
        },
        SENTENCIACQL_NO_SOPORTADA {
            @Override
            public String getMensaje(String... valores) {            
                return "La transacción '" + valores[0] + "' que corresponde al evento '" 
                      + valores[1] + "' de la página '" + valores[2] + "' del sitio web '" + valores[3] + "' " 
                      + "tiene un tipo de transacción no válido: '" + valores[4] + "'. "
                      + "Los tipos de transacción soportados son: SELECT, UPDATE, INSERT o DELETE.";
            }
        },        
        TRANSFORMACION_FALLIDA {
            @Override
            public String getMensaje(String... valores) {            
                return "No se pudo transformar el campo '" + valores[0] + "'" + " del evento '" + valores[1] + "'. "
                     + "Valor del campo: " + valores[2] + ". Tipo de transformación: " + valores[3];
            }
        };
        
        public abstract String getMensaje(String... valores);

    }
    
}
