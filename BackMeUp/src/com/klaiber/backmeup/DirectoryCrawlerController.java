package com.klaiber.backmeup;

import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class DirectoryCrawlerController {

	private DBConnector connect;
	private int maxThreads;
	private int activeThreads = 0;	
	private LinkedList<String> dirs;
	private long added = 0;
	private Logger log = Logger.getLogger("main");
	
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
		
		dirs.add(directory);
		log.info("Crawling directory ["+directory+"]" );
		while (dirs.size()>0 || activeThreads > 0)	{
			if (dirs.size()>0 && maxThreads > activeThreads) {
				Thread t = new Thread(new DirectoryCrawlerWorker(run, dirs.poll(), recursive, this, connect));
		        t.start();
		        activeThreads++;
			} else {	
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		long result = added;
		added = 0;
		return result;
	}
	
	
	public synchronized void returnThread(long itemsAdded){
		activeThreads--;
		added = added + itemsAdded;
	}
	
	public synchronized void addDir(String Directory){
		log.info("Directory ["+Directory+"] added to Crawllist" );
		dirs.add(Directory);
	}
}
