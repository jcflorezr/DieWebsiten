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
    
}
