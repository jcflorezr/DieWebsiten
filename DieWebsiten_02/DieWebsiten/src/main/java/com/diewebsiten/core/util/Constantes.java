package com.diewebsiten.core.util;

public enum Constantes {
    
    CASSANDRA_URL("localhost"),
    CASSANDRA_PORT(9042),
    RUTA_LOG("\\users\\juancamiloroman\\Desktop\\Log"),
    NOMBRE_LOG("LogDieWebsiten"),
    ERROR("Por favor intente más tarde."),
    EXCEPCION_GENERICA("com.diewebsiten.modelo.Excepciones.ExcepcionGenerica"),
    NOMBRE_EVENTO_VACIO("No se ha especificado un evento."),    
    
    NMBR_SNT_TRANSACCIONES("SentenciaTransacciones"),
    SNT_TRANSACCIONES("SELECT tipotransaccion, transaccion, columnfamily_name, sentenciacql, filtrossentenciacql, columnasconsultasentenciacql, columnasintermediassentenciacql FROM diewebsiten.eventos WHERE sitioweb = ? AND pagina = ? AND evento = ?"),
    SNT_VALIDACIONES_EVENTO("SELECT column_name, grupovalidacion, formaingreso, valorpordefecto FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND evento = ?"),
    NMBR_SNT_VALIDACIONES_EVENTO("SentenciaValidacionesEvento"),
    
    COLLECTIONS_CASSANDRA("SetType,ListType,MapType"),
    TIPOS_LISTAS_SENTENCIAS_SELECT("simple,compuesta,parValoresSimple,parValoresCompuesta,parAgrupadaSimple,parAgrupadaCompuesta"),
    
    VALIDACION("Validación"),
    TRANSFORMACION("Transformación"),
    
    TRANSACCIONES_SOPORTADAS("SELECT,UPDATE,INSERT,DELETE"),
    
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
    V_TIPO_DATO_CASSANDRA,
    
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
                return "El evento '" + valores[0] + "' de la página '" + valores[1] + "' del sitio web '" + valores[2] + "' no existe.";
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
                     + "necesarias para los campos del formulario del evento '" + valores[0] + "'"
                     + " de la página '" + valores[1] + "' del sitio web '" + valores[2] + "'.";
            }
        },
        FILTRO_NO_EXISTE {
            @Override
            public String getMensaje(String... valores) {            
                return "No se encontró el filtro '" + valores[0] + "' de la sentencia CQL de tipo " + valores[2] + " de la transacción '" + valores[1] + "', " 
                     + "en los campos que contiene el formulario del evento '" + valores[3] + "' "
                     + "de la página '" + valores[4] + "' del sitio web '" + valores[5] + "'.";
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
