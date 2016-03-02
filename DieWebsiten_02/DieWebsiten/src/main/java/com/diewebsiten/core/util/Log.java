
package com.diewebsiten.core.util;

import com.diewebsiten.core.negocio.eventos.Evento;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
     */
    private Log() {
        try {
//          File carpetaLog = new File("/opt/apache-tomcat-8.0.23/logs/logsdw");
            File carpetaLog = new File("/Users/juaflore/logsdw");
//        	File carpetaLog = new File("/Users/juancamiloroman/logsdw");
//          File carpetaLog = new File(Constantes.RUTA_LOG);
            if (!carpetaLog.exists())
                carpetaLog.mkdirs();
            
            //String rutaYNombreLog = "/opt/apache-tomcat-8.0.23/logs/logsdw" + "/promociones";
            String rutaYNombreLog = "/Users/juaflore/logsdw/log";
//            String rutaYNombreLog = "/Users/juancamiloroman/logsdw/log";
            //String rutaYNombreLog = Constantes.RUTA_LOG + Constantes.NOMBRE_LOG;
            logger = Logger.getLogger(rutaYNombreLog);        
                
            PatternLayout layout = new PatternLayout();
            layout.setConversionPattern("[%p] [%d]  %m%n");
            
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
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            
            log.error("----------------------------------- ERROR -----------------------------------------");
            
            if (null != log.evento) {
                log.error("SITIO WEB: " + log.evento.getSitioWeb());
                log.error("PÁGINA: " + log.evento.getPagina());
                log.error("EVENTO: " + log.evento.getNombreEvento());
            }
            log.error("EXCEPCIÓN --> " + tipoExcepcion);
            log.error("MENSAJE   --> " + t.getMessage());

            if (null != elementos) {
                
                log.error("------------------------------ TRAZA DEL ERROR ------------------------------------");

                // Obtener la traza y hacerla "legible" antes de escribirla en el log.
                int i = 0;
                for (StackTraceElement elemento : elementos) {
                    i++;
                    log.error(i + " --> CLASE: '" + elemento.getClassName() + "'. MÉTODO: '" + elemento.getMethodName() + "'. LÍNEA: " + elemento.getLineNumber());
                }

            }

            log.error("-------------------------------------- FIN -----------------------------------------");
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
    }// encontrarError

}