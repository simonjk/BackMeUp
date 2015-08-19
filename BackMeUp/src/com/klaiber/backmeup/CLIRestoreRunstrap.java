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

public class CLIRestoreRunstrap {

	public static Logger log;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log = LogHandler.getLogger();
	    if (args.length == 0) return;
		DBConnector con = new H2EmbeddedConnector();
		if ( con.open("jdbc:h2:D:/Apps/BackMeUp/backup;IFEXISTS=TRUE") || con.open("jdbc:h2:backup;INIT=RUNSCRIPT FROM 'backupdb_create.ddl'")){
			int run = Integer.parseInt(args[0]);
			String internalDrivePath = "G:/";
			if (args.length >= 2) internalDrivePath = args[1];
			String externalDrivePath = "J:/";
			if (args.length >= 3) externalDrivePath = args[2];

			log.info("Staring to restore Items for run ["+run+"]");
			LinkedHashSet<BackupItem> internalItems = new LinkedHashSet<BackupItem>(con.getSavedItems(run, false));

			LinkedHashSet<BackupItem> errors = new LinkedHashSet<BackupItem>();
			
			for (BackupItem bi:internalItems){
				if( !FileRestoreWorker.restoreFile(internalDrivePath, con.getDriveName(bi.getDrive1()), bi.getHash(), bi.getPath(), false)){
					errors.add(bi);
				}				
			}
			if (errors.size()>0){
				log.info("Restored run ["+run+"] execept folloeing files:");
				for (BackupItem bi:errors){
					log.info(bi.getPath() +" / " + bi.getHash());
				}
			}
			

			
		} else {
			log.severe("Database could not be opened");
		}

	}

}

