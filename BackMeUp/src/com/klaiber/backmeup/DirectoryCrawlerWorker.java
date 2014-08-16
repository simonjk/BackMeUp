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
		log.info("Crawling directory ["+dir.getAbsolutePath()+"]" );
		if (dir.exists()) {
			if (dir.isFile()) {
				// Sonderbehandlung verweis auf File wurder übergeben
				connect.addBackupItem(run, dir.getAbsolutePath(), dir.length(), dir.lastModified());
				added++;
			} else {
				for( File c : dir.listFiles()){
					if (c.isFile()){
						connect.addBackupItem(run, c.getAbsolutePath(), c.length(), c.lastModified());
						added++;
					} else {						
						if (recur) control.addDir(c.getAbsolutePath());
					}
				}
			}
		}
		control.returnThread(added);
		
	}
	
	DirectoryCrawlerWorker(int run, String directory, boolean recursive, DirectoryCrawlerController controller, DBConnector dbConnector){
		control = controller;
		recur = recursive;
		dir = new File(directory);
		connect = dbConnector;
		this.run = run;
	}

}
