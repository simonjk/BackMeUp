package com.klaiber.backmeup;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DirectoryCrawlerController {

	private DBConnector connect;
	private int maxThreads;
	private int activeThreads = 0;	
	private LinkedList<String> dirs;
	private long added = 0;
	private Logger log = LogHandler.getLogger();
	private Set<String> activeDirs = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
	
	DirectoryCrawlerController(DBConnector dbConnector, int maxThreads){
		connect = dbConnector;
		if (maxThreads < 1) {
			this.maxThreads = 1;
			
		} else { 
			this.maxThreads = maxThreads;
		}
		dirs = new LinkedList<String>();
	}
	
	
	public long crawl(String directory, boolean recursive, int run){
		int count = 0;
		//int loopsAfterZero = 0;
		dirs.add(directory);
		log.info("Crawling directory ["+directory+"]" );
		while (dirs.size()>0 || activeDirs.size() > 0)	{
			count++;
			if (dirs.size()>0 && maxThreads > activeThreads) {
				String dir = dirs.poll();
				Thread t = new Thread(new DirectoryCrawlerWorker(run, dir, recursive, this, connect));
		        t.start();
		        File d = new File(dir);
		        activeDirs.add(d.getAbsolutePath());
		        activeThreads++;
			} else {	
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (count>=50) {
				//if (dirs.size()==0){
				//	loopsAfterZero++;
				//}				
				count = 0;
				String[] ad = new String[activeDirs.size()];
				activeDirs.toArray(ad);
				log.info( activeThreads +" - "+ ad.length + " - " +Arrays.toString(ad));
				//if (loopsAfterZero>=20 && activeDirs.size()==0)  activeThreads = 0;
			}
		}
		
		long result = added;
		added = 0;
		return result;
	}
	
	
	public synchronized void returnThread(long itemsAdded, String path){
		activeThreads--;
		added = added + itemsAdded;
		activeDirs.remove(path);
	}
	
	public synchronized void addDir(String Directory){
		//log.info("Directory ["+Directory+"] added to Crawllist" );
		dirs.add(Directory);
	}
}
