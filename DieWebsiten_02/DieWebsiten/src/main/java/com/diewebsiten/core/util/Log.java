
package com.diewebsiten.core.util;

import com.diewebsiten.core.excepciones.ExcepcionGenerica;
import com.diewebsiten.core.negocio.eventos.Evento;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


public class Log {
    
    private Logger logger;
    private static Log log;
    private Evento evento;

    /**
     * Devuelve una instancia única de la clase que implementa el Log de la aplicación
     * @return instancia única del Log de la aplicación
     */
    public static synchronized Log getInstance() {
        if (log == null)
            log = new Log();
        return log;
    } //getInstance
    
    public static synchronized Log getInstance(Evento t) {
        if (log == null) 
            log = new Log();            
        log.evento = t;
        return log;
    }

    /**
     * Constructor privado siguiendo el patrón de diseño 'Singleton'
     * @throws ExcepcionGenerica 
     */
    private Log() throws ExcepcionGenerica {
        	
    	String rutaYNombreLog = "";
    	File carpetaLog;
    	
    	Set<String> posiblesRutas = new HashSet<>();
    	posiblesRutas.add("/Users/juaflore/");
    	posiblesRutas.add("/Users/juancamiloroman/");
    	
    	boolean esDirectorio = false;
    	for (String ruta : posiblesRutas) {
    		carpetaLog = new File(ruta);
    		if (carpetaLog.isDirectory()) {
    			esDirectorio = true;
    			rutaYNombreLog = ruta + "logsdw";
    			break;
    		}
    	}
    	
    	if (!esDirectorio) {
    		throw new ExcepcionGenerica("No se pudo encontrar ninguna de las posibles rutas que hay por defecto.");
    	}

    	
    	
    	
    	HAY QUE SEGUIR MIRANDO COMO CAPTURAR LAS EXCEPCIONES LANZADAS DESDE ESTA CLASE DE LOG
    	
    	
    	
    	
//          File carpetaLog = new File(Constantes.RUTA_LOG);
        
        
        //String rutaYNombreLog = "/opt/apache-tomcat-8.0.23/logs/logsdw" + "/promociones";
//            String rutaYNombreLog = "/Users/juaflore/logsdw/log";
        
        //String rutaYNombreLog = Constantes.RUTA_LOG + Constantes.NOMBRE_LOG;
        rutaYNombreLog += "/log";
        logger = Logger.getLogger(rutaYNombreLog);        
            
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("[%p] [%d{MM-dd-yyyy HH:mm:ss}] [%t%r%x%X] %m%n");
        
        rutaYNombreLog += "_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".log";     
        
        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
        rollingAppender.setFile(rutaYNombreLog);
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();
        
        //ConsoleAppender consoleAppender = new ConsoleAppender();
        //consoleAppender.setLayout(layout);
        //consoleAppender.activateOptions();
 
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(rollingAppender);
        //rootLogger.addAppender(consoleAppender);
            
    } //Logs

    //--------------------------------------------------------------------------
    // Métodos implementados
    //--------------------------------------------------------------------------

    /**
     * Adiciona un mensaje informativo al log
     * @param mensaje Mensaje informativo que se adicionará al log
     */
    public void info(String mensaje) {
        logger.info(mensaje);
    } //info
    
    /**
     * Adiciona un mensaje de advertencia al log
     * @param mensaje Mensaje de advertencia que se adicionará al log
     */
    public void warn(String mensaje) {
        logger.warn(mensaje);
    } //info

    /**
     * Adiciona un mensaje de error al log
     * @param mensaje Mensaje de error que se adicionará al log
     */
    public void error(String mensaje) {
        logger.error(mensaje);
    } //error 
    
    /**
     * Imprimir la traza del error dentro de un archivo de log
     * @param t el contenido de la excepción.
     */
    public void imprimirErrorEnLog(Throwable t) {
        
        try {
            
            Class clase = t.getClass();
            String tipoExcepcion = clase.toString().trim().replace("class ", "");
            StackTraceElement[] elementos = t.getStackTrace();
            
            if (Constantes.EXCEPCION_GENERICA.equals(tipoExcepcion)) {
                log.warn(t.getMessage());
                return;
            }
            
            StringBuilder trazaError = new StringBuilder();
//            
//            if (null != log.evento) {
//                log.error("SITIO WEB: " + log.evento.getSitioWeb());
//                log.error("PÁGINA: " + log.evento.getPagina());
//                log.error("EVENTO: " + log.evento.getNombreEvento());
//            }
            trazaError.append("\n").append("[EXCEPCIÓN] --> ").append(tipoExcepcion).append("\n");
            trazaError.append("[MENSAJE]   --> ").append(t.getMessage()).append("\n");

            if (null != elementos) {
                
            	trazaError.append(" ").append("------------------------------ TRAZA DEL ERROR ------------------------------------").append("\n");

                // Obtener la traza y hacerla "legible" antes de escribirla en el log.
                int i = 0;
                for (StackTraceElement elemento : elementos) {
                    i++;
                    trazaError.append(" ").append(i).append(" --> CLASE: '").append(elemento.getClassName()).append("'. ");
                    trazaError.append("MÉTODO: '").append(elemento.getMethodName()).append("'.");
                    trazaError.append("LÍNEA: ").append(elemento.getLineNumber()).append("\n");
                }

            }

            trazaError.append("---------------------------------------------------------------------------------");
            log.error(trazaError.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
    }// encontrarError

}