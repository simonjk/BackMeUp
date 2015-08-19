package com.klaiber.backmeup;

import java.io.File;
import java.util.logging.Logger;

public class DirectoryCrawlerWorker implements Runnable {

	private File dir;
	private boolean recur = true;
	private DirectoryCrawlerController control;
	private DBConnector connect;
	private int run;
	private Logger log = log = LogHandler.getLogger();
	
	@Override
	public void run() {
		long added = 0;
		try{			
			log.info("Crawling directory ["+dir.getAbsolutePath()+"]" );
			if (dir.exists()) {
				if (dir.isFile()) {
					if(!isOnFilterList(dir)){
						// Sonderbehandlung verweis auf File wurder übergeben
						connect.addBackupItem(run, dir.getAbsolutePath(), dir.length(), dir.lastModified());
						added++;
					}
				} else {
					for( File c : dir.listFiles()){
						if (c.isFile()){
							if(!isOnFilterList(c)){
								connect.addBackupItem(run, c.getAbsolutePath(), c.length(), c.lastModified());
								added++;
							}
						} else {
							if(!isOnDirFilterList(c)){
								if (recur) control.addDir(c.getAbsolutePath());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.warning(e.getMessage());
			control.returnThread(added, dir.getAbsolutePath());
		}
		
		control.returnThread(added, dir.getAbsolutePath());
		
	}
	
	private boolean isOnFilterList(File f){
		
		//TODO: Creat real ignore list Handling
		if(f.getName().equalsIgnoreCase("owncloud.log")) return true;
		if(f.getName().equalsIgnoreCase(".csync_journal.db-wal")) return true;
		if(f.getName().equalsIgnoreCase(".csync_journal.db")) return true;
		if(f.getName().equalsIgnoreCase(".owncloudsync.log")) return true;
		if(f.getName().equalsIgnoreCase(".csync_journal.db-shm")) return true;
		if(f.getName().equalsIgnoreCase("segments_qb]")) return true;		
	
		
		
		return false;
	}
	
private boolean isOnDirFilterList(File f){
		
		//TODO: Creat real ignore list Handling
		if(f.getName().equalsIgnoreCase(".dropbox.cache")) return true;
		
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/SendTo")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Lokale Einstellungen")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/AppData/Local/Anwendungsdaten")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Documents/Eigene Videos")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Eigene Dateien")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Cookies")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Documents/Eigene Bilder")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/AppData/Roaming/Microsoft/Windows/Start Menu/Programme")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Startmenü")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Recent")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Netzwerkumgebung")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Druckumgebung")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Anwendungsdaten")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/AppData/Local/Verlauf")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Documents/Eigene Musik")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/Vorlagen")) return true;
		if(f.getAbsolutePath().equalsIgnoreCase("C:/Users/simon/AppData/Local/Temporary Internet Files")) return true;
	
		
		
		
		return false;
	}
	
	DirectoryCrawlerWorker(int run, String directory, boolean recursive, DirectoryCrawlerController controller, DBConnector dbConnector){
		control = controller;
		recur = recursive;
		dir = new File(directory);
		connect = dbConnector;
		this.run = run;
	}

}
