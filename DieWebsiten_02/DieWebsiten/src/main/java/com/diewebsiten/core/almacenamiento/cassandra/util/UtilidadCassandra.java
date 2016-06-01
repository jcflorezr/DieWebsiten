package com.diewebsiten.core.almacenamiento.cassandra.util;

public class UtilidadCassandra {
	
	
	/*java.lang.String
    			java.lang.Long
    			java.nio.ByteBuffer
    			java.lang.Boolean
    			java.lang.Double
    			java.lang.Float
    			java.lang.Integer
    			java.lang.Long
    			java.math.BigDecimal
    			java.net.InetAddress
    			java.lang.String
    			java.util.Date
    			java.util.UUID
    			java.util.UUID
    			java.lang.String
    			java.math.BigInteger*/
	
	public static boolean validarTipoColumna(String tipoColumna) {
		
		try {
			Class.forName(tipoColumna);
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}

}
