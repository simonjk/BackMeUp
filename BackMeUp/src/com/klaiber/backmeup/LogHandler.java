package com.klaiber.backmeup;

import java.io.IOException;
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

	    try {  

	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler("run_"+System.currentTimeMillis()+".log");  
	        log.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  

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
