package com.klaiber.backmeup;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;

public class TestRunstrap1 {

	public static Logger log;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log = LogHandler.getLogger();
	    
		DBConnector con = new H2EmbeddedConnector();
		if ( con.open("jdbc:h2:backup;IFEXISTS=TRUE") || con.open("jdbc:h2:backup;INIT=RUNSCRIPT FROM 'backupdb_create.ddl'")){
			int group = 3;
			String internalDrivePath = "I:/";
			String externalDrivePath = "H:/";
			int externalOffset = 3;
			long spaceReserve = 15728640L;
			long maxIgnoredSpace = 1572864L;
			int run = con.createRun(group);
			log.info("Created Run ["+run+"] for Backupgroup "+ group );					
			DirectoryCrawlerController dcc = new DirectoryCrawlerController(con, 20);
			for (String d : con.getDirectories(group)){
				//System.out.print(d);
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
			
			log.info("Staring to save Items for group ["+group+"]");
			LinkedHashSet<BackupItem> internalItems = new LinkedHashSet<BackupItem>(con.getUnsavedItems(run, false));
			LinkedHashSet<BackupItem> externalItems = new LinkedHashSet<BackupItem>(con.getUnsavedItems(run, true));
			LinkedHashSet<String> internalDrives = new LinkedHashSet<String>(con.getUsableDrives(group, false));
			LinkedHashSet<String> externalDrives = new LinkedHashSet<String>(con.getUsableDrives(group, true));
		
			FileCopyController fcc = new FileCopyController(con, internalItems, externalItems, internalDrives, externalDrives, internalDrivePath, externalDrivePath, externalOffset, spaceReserve, maxIgnoredSpace);
			int saveStatus = fcc.saveItems();
			boolean success = saveStatus==0?true:false;
			log.info("Finishing run ["+run+"] ["+success+"]");
			con.finishRun(run, success);
			String xml = con.getXmlRepresentation(run);
			//System.out.println(xml);
			Run r = con.getRun(run);
			try{
				FileUtils.writeStringToFile(new File(internalDrivePath+"/run_"+r.getFinished().getTime()+".xml"), xml);
				FileUtils.writeStringToFile(new File(externalDrivePath+"/run_"+r.getFinished().getTime()+".xml"), xml);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			log.severe("Database could not be opened");
		}

	}

}

