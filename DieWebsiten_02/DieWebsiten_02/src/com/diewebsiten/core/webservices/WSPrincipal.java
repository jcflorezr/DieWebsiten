
package com.diewebsiten.core.webservices;

import javax.jws.WebService;

/**
 *  Interface para ser implementada por el Web Service WSPrincipalImpl
 */

@WebService
public interface WSPrincipal {
    
    public String puntoEntrada(String idSesion, String cliente, String a, String parametros);
    
}
