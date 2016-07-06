
package com.diewebsiten.core.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.diewebsiten.core.excepciones.ExcepcionDeLog;
import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.google.common.base.Throwables;


public class Log {
    
    private Logger logger;
    private static Log log;
    private static Object obj = new Object();

    /**
     * Devuelve una instancia única de la clase que implementa el Log de la aplicación
     * @return instancia única del Log de la aplicación
     */
    public static Log getInstance() throws ExcepcionDeLog {
        if (log == null) {
        	synchronized (obj) {
				if (log == null) {					
					log = new Log();
				}
			}
        }
        return log;
    }


    /**
     * Constructor privado siguiendo el patrón de diseño 'Singleton'
     * @throws ExcepcionGenerica 
     */
    private Log() throws ExcepcionDeLog {
        	
    	String directorioYArchivoLog = "";
    	File directorioLog;
    	
    	Set<String> posiblesDirectoriosLog = new HashSet<>();
    	posiblesDirectoriosLog.add("/Users/juancamiloroman/");
    	posiblesDirectoriosLog.add("/Users/juaflore/");
    	
    	boolean esDirectorio = false;
    	for (String posibleDirectorio : posiblesDirectoriosLog) {
    		directorioLog = new File(posibleDirectorio);
    		if (directorioLog.isDirectory()) {
    			if (!directorioLog.canWrite()) {
    				throw new ExcepcionDeLog("No se puede crear el archivo de log en el directorio '" + posibleDirectorio + "' debido a que no tiene permisos de escritura.");
    			}
    			esDirectorio = true;
    			directorioYArchivoLog = posibleDirectorio + "logsdw/";
    			break;
    		}
    	}
    	
    	if (!esDirectorio) {
    		throw new ExcepcionDeLog("No se encontró un directorio válido para la creación de los logs.");
    	}
    	
        logger = Logger.getLogger(directorioYArchivoLog);        
            
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("[%p] [%d{MM-dd-yyyy HH:mm:ss}] [%t%r%x%X] %m%n");
        
        directorioYArchivoLog += "log_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".log";     
        
        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
        rollingAppender.setFile(directorioYArchivoLog);
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();
        
        //ConsoleAppender consoleAppender = new ConsoleAppender();
        //consoleAppender.setLayout(layout);
        //consoleAppender.activateOptions();
 
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(rollingAppender);
        //rootLogger.addAppender(consoleAppender);
            
    }
    
    /**
     * 
     * @param error
     * @param trazaError
     * @throws ExcepcionDeLog
     */
    public void imprimirErrorEnLog(Throwable error) throws ExcepcionDeLog {
    	imprimirErrorEnLog(error, new StringBuilder());
    }
    
    /**
     * Imprimir la traza del error dentro de un archivo de log
     * @param error el contenido de la excepción.
     */
    public void imprimirErrorEnLog(Throwable error, StringBuilder trazaError) throws ExcepcionDeLog {
    	
        try {
            
        	trazaError = trazaError == null ? new StringBuilder() : trazaError;
            Class<? extends Throwable> clase = error.getClass();
            String tipoExcepcion = clase.toString().trim().replace("class ", "");
            StackTraceElement[] elementos = error.getStackTrace();

            trazaError.append("\n").append("[EXCEPCIÓN]  --> ").append(tipoExcepcion).append("\n");
            trazaError.append("[MENSAJE]    --> ").append(error.getMessage()).append("\n");

            if (null != elementos) {
                
            	trazaError.append(" ").append("------------------------------ TRAZA DEL ERROR ------------------------------------").append("\n");

                // Obtener la traza y hacerla "legible" antes de escribirla en el log.
                int i = 0;
                for (StackTraceElement elemento : elementos) {
                    i++;
                    trazaError.append(" ").append(i).append(" --> CLASE: '").append(elemento.getClassName()).append("'. ");
                    trazaError.append("MÉTODO: '").append(elemento.getMethodName()).append("'. ");
                    trazaError.append("LÍNEA: ").append(elemento.getLineNumber()).append("\n");
                }

            }

            trazaError.append("---------------------------------------------------------------------------------");
            logger.error(trazaError.toString());
            
        } catch (Exception e) {
        	trazaError = new StringBuilder("No fue posible imprimir el error en el log: \n");
        	trazaError.append(Throwables.getStackTraceAsString(e));
        	trazaError.append("Error que se iba a imprimir en el log: \n");
        	trazaError.append(Throwables.getStackTraceAsString(error));
        	throw new ExcepcionDeLog(trazaError.toString());
        }        
        
    }

}