package com.klaiber.backmeup;

import java.io.File;

public class DirectoryCrawlerWorker implements Runnable {

	private File dir;
	private boolean recur = true;
	private DirectoryCrawlerController control;
	private DBConnector connect;
	private int run;
	
	@Override
	public void run() {
		long added = 0;
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
						control.addDir(c.getAbsolutePath());
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
