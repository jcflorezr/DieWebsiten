package com.diewebsiten.core.excepciones;

public class ExcepcionDeLog extends Exception {
	
	public ExcepcionDeLog() {
        super();
    }
    
    public ExcepcionDeLog(String mensaje) {
        super(mensaje);        
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
	
}
