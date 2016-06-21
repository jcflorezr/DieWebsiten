package com.diewebsiten.core.eventos.util;

public enum Mensajes {
	
	ERROR { 
		public String get() { return "Por favor intente más tarde."; }
	};
    public abstract String get();
	
    
	public enum Evento {
		
		NOMBRE_EVENTO_VACIO {
			@Override
			public String get() { return "El nombre del evento está vacío."; }
		},
		EVENTO_NO_EXISTE {
			@Override
			public String get() { return "El evento no existe."; }
		};
		public abstract String get();
        
		
        public enum Formulario {
        	
        	PARAMETROS_FORMULARIO_NO_EXISTEN {
                @Override
                public String get(String... valores) {            
                    return "No hay parámetros para la ejecución del formulario.";
                }
            },
            CAMPOS_FORMULARIO_NO_EXISTEN {
                @Override
                public String get() {            
                    return "No hay campos para la ejecución del formulario.";
                }
            },
    		VALIDACIONES_NO_EXISTEN {
                @Override
                public String get() {            
                    return "No se encontraron las validaciones o las transformaciones "
                         + "necesarias para los campos del formulario.";
                }
            },
            TRANSFORMACION_FALLIDA {
                @Override
                public String get(String... valores) {            
                    return "Error en el formulario. No se pudo transformar el campo '" + valores[0] + "'. Tipo de transformación: " + valores[1];
                }
            };
            
            public String get() {return "";}
            public String get(String... valores) {return "";}            
            
        }
        
        
        public enum Transaccion {
        	
        	FILTRO_NO_EXISTE {
                @Override
                public String get(String... valores) {            
                    return "No se encontró el filtro '" + valores[0] + "' de la sentencia de la transacción '" + valores[1] + "', " 
                         + "en los campos que contiene el formulario.";
                }
            },        
            
            // =========== CASSANDRA =========== //
            SENTENCIACQL_NO_SOPORTADA {
                @Override
                public String get(String... valores) {            
                    return "La transacción '" + valores[0] + "tiene un tipo de transacción no válido: '" + valores[1] + "'. "
                          + "Los tipos de transacción soportados son: SELECT, UPDATE, INSERT o DELETE.";
                }
            };
            
            public abstract String get(String... valores);
        	
        }

    }

}
