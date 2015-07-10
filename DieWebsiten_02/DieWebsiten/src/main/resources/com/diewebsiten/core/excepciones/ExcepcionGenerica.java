
package com.diewebsiten.core.excepciones;

/**
 *  Esta clase se crea con el fin de capturar las excepciones inducidas.
 * 
 * @author Juan Camilo Flórez Román (www.diewebsiten.com)
 */
public class ExcepcionGenerica extends Exception {
    
    public ExcepcionGenerica() {
        super();
    }
    
    public ExcepcionGenerica(String mensaje) {
        super(mensaje);        
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
}
