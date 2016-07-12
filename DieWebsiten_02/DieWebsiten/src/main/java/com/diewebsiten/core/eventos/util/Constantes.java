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
    
    public enum Transacciones {
    	
    	NOMBRE_TRANSACCION("transaccion"),
    	SENTENCIA("sentencia"),
    	FILTROS_SENTENCIA("filtrossentencia"),
    	MOTOR_ALMACENAMIENTO("motoralmacenamiento"),
    	PARAMETROS_TRANSACCION("parametrostransaccion"),
    	RESULTADO_EN_JERARQUIA("resultadoenjerarquia");
    	
    	private String constante;
    	
    	private Transacciones(String constante) {
    		this.constante = constante;
    	}
    	
    	public String get() {
    		return this.constante;
    	}
    	
    }
    
}
