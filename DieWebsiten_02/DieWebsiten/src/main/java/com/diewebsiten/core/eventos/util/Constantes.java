package com.diewebsiten.core.eventos.util;

public enum Constantes {
    
    NOMBRE_EVENTO_VACIO("No se ha especificado un evento."),    
    
    NMBR_SNT_TRANSACCIONES("SentenciaTransacciones"),
    SNT_TRANSACCIONES("SELECT transaccion, tipotransaccion, columnfamily_name, sentenciacql, filtrossentenciacql, columnasconsultasentenciacql, columnasintermediassentenciacql FROM diewebsiten.eventos WHERE sitioweb = ? AND pagina = ? AND evento = ?"),
    
    NMBR_SNT_VALIDACIONES_EVENTO("SentenciaValidacionesEvento"),
    SNT_VALIDACIONES_EVENTO("SELECT column_name, grupovalidacion, formaingreso, valorpordefecto FROM diewebsiten.formularios WHERE sitioweb = ? AND pagina = ? AND evento = ?");
    
    private String constanteString;
    
    Constantes(String constante) {
        this.constanteString = constante;
    }
    
    public String get(){
        return this.constanteString;
    }
    
    public boolean equals(String s) {
        return null == s ? false : s.equalsIgnoreCase(constanteString);
    }
    
}
