package com.klaiber.backmeup;

import java.util.Set;
import java.util.logging.Logger;

public class TestRunstrap1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = Logger.getLogger("main");
		DBConnector con = new H2EmbeddedConnector();
		if ( con.open("jdbc:h2:backup;IFEXISTS=TRUE") || con.open("jdbc:h2:backup;INIT=RUNSCRIPT FROM 'backupdb_create.ddl'")){
			int run = con.createRun(1);
			log.info("Created Run ["+run+"] for Backupgroup 1" );					
			DirectoryCrawlerController dcc = new DirectoryCrawlerController(con, 20);
			for (String d : con.getDirectories(1)){
				dcc.crawl(d, true, run);
			}
			long unchanged = con.checkForUnmodifiedFiles(run);
			log.info("Checked for unchanged Files in Run ["+run+"]. Found ["+unchanged+"] ");
			
			
			Set<BackupItem> unhashed = con.getUnhashedItems(run);
			log.info("Unhashed found [" + unhashed.size() + "]");
			for(BackupItem bi : unhashed){
				log.info("Hashing [" + bi.getPath() + "]");
				bi.generateHash();
				log.info("Hash created [" + bi.getHash() + "]");
			}
			log.info("Matching Hashes for Run ["+run+"]");
			long matched = con.matchHashes(run);
			log.info("Matched ["+matched+"] Hashes for Run ["+run+"]");
			
			log.info("Test");
			//int run=67; // Test PLEASE DELETE
		} else {
			log.severe("Database could not be opened");
		}

	}

}

