package com.klaiber.backmeup;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHandler {

	static Logger log = null;
	
	/**
	 * @param args
	 */
	public static void setUpLogger() {
		log = Logger.getLogger("main");
		FileHandler fh;  
		ConsoleHandler ch;

	    try {  

	        // This block configure the logger with handler and formatter  
	        ch = new ConsoleHandler();
	        log.addHandler(ch);
	    	fh = new FileHandler("run_"+System.currentTimeMillis()+".log");  	        
	    	log.addHandler(fh);	        
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);
	        ch.setFormatter(formatter);  

	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	}

	
	public static Logger getLogger(){
		if (log == null) setUpLogger();
		return log;
	}
}
