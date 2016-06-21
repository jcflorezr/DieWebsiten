package com.diewebsiten.core.eventos.util;

public enum Constantes {
    
    NOMBRE_EVENTO_VACIO("No se ha especificado un evento.");
    
    private String constante;
    
    Constantes(String constante) {
        this.constante = constante;
    }
    
    public String get(){
        return this.constante;
    }
    
    public boolean equals(String s) {
        return null == s ? false : s.equalsIgnoreCase(constante);
    }
    
    public enum Formulario {
    	
    	NMBR_SNT_VALIDACIONES_EVENTO("SentenciaValidacionesEvento"),
        SNT_VALIDACIONES_EVENTO("SELECT column_name, grupovalidacion, formaingreso, valorpordefecto FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND evento = ?;"),
        
        NMBR_SNT_GRUPO_VALIDACIONES("SentenciaGrupoValidaciones"),
        SNT_GRUPO_VALIDACIONES("SELECT grupo_validacion, tipo, validacion FROM diewebsiten.grupos_de_validaciones WHERE grupo_validacion = ?;");
    	
    	private String constante;
    	
    	private Formulario(String constante) {
    		this.constante = constante;
    	}
    	
    	public String get() {
    		return this.constante;
    	}
    }
    
    public enum Transacciones {
    	
    	NOMBRE_TRANSACCION("transaccion"),
    	SENTENCIA("sentencia"),
    	FILTROS_SENTENCIA("filtrossentencia"),
    	MOTOR_ALMACENAMIENTO("motoralmacenamiento"),
    	PARAMETROS_TRANSACCION("parametrostransaccion"),
    	TRANSACCION_DE_SISTEMA("transacciondesistema"),
    	
    	NMBR_SNT_TRANSACCIONES("SentenciaTransacciones"),
    	SNT_TRANSACCIONES("SELECT transaccion, motoralmacenamiento, sentencia, filtrossentencia FROM diewebsiten.eventos WHERE sitioweb = ? AND pagina = ? AND evento = ?;");
    	
    	private String constante;
    	
    	private Transacciones(String constante) {
    		this.constante = constante;
    	}
    	
    	public String get() {
    		return this.constante;
    	}
    	
    	public enum Cassandra {
    		
    		NMBR_SNT_TRANSACCIONES_CASSANDRA("TransaccionesCassandra"),
    	    SNT_TRANSACCIONES_CASSANDRA("SELECT tipotransaccion, keyspace_name, columnfamily_name, columnasconsultasentenciacql, columnasintermediassentenciacql FROM diewebsiten.transaccionescassandra WHERE evento = ? AND transaccion = ?;");
    	    
    		private String constante;
    		
    		private Cassandra(String constante) {
    			this.constante = constante;
    		}
    		
    		public String get() {
    			return this.constante;
    		}
    	}
    	
    	public enum MySql {
    		
    		NMBR_SNT_TRANSACCIONES_MYSQL("TransaccionesMySql"),
    	    SNT_TRANSACCIONES_MYSQL("?");
    	    
    		private String constante;
    		
    		private MySql(String constante) {
    			this.constante = constante;
    		}
    		
    		public String get() {
    			return this.constante;
    		}
    	}
    	
    }
    
}
