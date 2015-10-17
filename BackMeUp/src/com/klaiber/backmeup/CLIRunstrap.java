package com.klaiber.backmeup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class CLIRunstrap {

	public static Logger log;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log = LogHandler.getLogger();
	    if (args.length == 0) return;
		/*DBConnector con = new H2EmbeddedConnector();
		if ( con.open("jdbc:h2:backup;IFEXISTS=TRUE") || con.open("jdbc:h2:backup;INIT=RUNSCRIPT FROM 'backupdb_create.ddl'")){
		*/
	    DBConnector con = new MySQLConnector();
		if ( con.open("jdbc:mysql://127.0.0.1:3306/bmu?zeroDateTimeBehavior=convertToNull")){
			boolean skipExternal = false;
			int group = Integer.parseInt(args[0]);
			String internalDrivePath = "G:/";
			if (args.length >= 2) internalDrivePath = args[1];
			String externalDrivePath = "none";
			if (args.length >= 3) externalDrivePath = args[2];				
			if (externalDrivePath.equalsIgnoreCase("none")) skipExternal = true;			
			int externalOffset = 3;
			if (args.length >= 4) externalOffset = Integer.parseInt(args[3]);
			long spaceReserve = 15728640L;
			if (args.length >= 5) spaceReserve = Long.parseLong(args[4]);
			long maxIgnoredSpace = 1572864L;
			if (args.length >= 6) maxIgnoredSpace = Long.parseLong(args[5]);
			
			int run = con.createRun(group);
			log.info("Created Run ["+run+"] for Backupgroup "+ group );
			
			//int run = 1685;
			
			DirectoryCrawler dc = new DirectoryCrawler(con, run,group);
			
			for (String d : con.getDirectories(group)){
				//System.out.print(d);
				try {
					dc.crawl(Paths.get(d), true);
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
			
			/*
		    DirectoryCrawlerController dcc = new DirectoryCrawlerController(con, 20);
			 
			for (String d : con.getDirectories(group)){
				//System.out.print(d);
				dcc.crawl(d, true, run);
			}
			*/
			
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
			log.info("Internal Items to save: ["+internalItems.size()+"]");
			
			LinkedHashSet<BackupItem> externalItems = new LinkedHashSet<BackupItem>();
			if (!skipExternal){
				externalItems = new LinkedHashSet<BackupItem>(con.getUnsavedItems(run, true));
				log.info("External Items to save: ["+externalItems.size()+"]");			
			}
			
			LinkedHashSet<String> internalDrives = new LinkedHashSet<String>(con.getUsableDrives(group, false));
			log.info("Internal Drives: ["+internalDrives.size()+"]");
			
			LinkedHashSet<String> externalDrives = new LinkedHashSet<String>();
			if (!skipExternal){
				externalDrives = new LinkedHashSet<String>(con.getUsableDrives(group, true));
				log.info("External Drives: ["+externalDrives.size()+"]");
			}
			
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
				if (!skipExternal){
					FileUtils.writeStringToFile(new File(externalDrivePath+"/run_"+r.getFinished().getTime()+".xml"), xml);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.severe("Error writing XML: ["+e.toString()+"]");
			}
			
		} else {
			log.severe("Database could not be opened");
		}

	}

}

