package com.klaiber.backmeup;

import java.util.logging.Logger;

public class CrawTest {
	public static Logger log;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log = LogHandler.getLogger();	    
		DBConnector con = new H2EmbeddedConnector();
		if ( con.open("jdbc:h2:backup;IFEXISTS=TRUE") || con.open("jdbc:h2:backup;INIT=RUNSCRIPT FROM 'backupdb_create.ddl'")){
			DirectoryCrawlerController dcc = new DirectoryCrawlerController(con, 20);
			System.out.print("crawling");
			dcc.crawl("D:/Apps/Grails/springsource/ggts-3.2.0.RELEASE/plugins/org.eclipse.pde.build_3.8.2.v20121114-140810/data/30/plugin", true, 4);			
		} else {
			System.out.print("error");
			log.severe("Database could not be opened");
		}
	}

}
