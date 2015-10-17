package com.klaiber.backmeup;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class DirectoryCrawler {

	private Logger log = LogHandler.getLogger();
	private DBConnector connect;
	private int runId;
	private String[] fileFilters;
	private String[] dirFilters;
	
	
	public DirectoryCrawler(DBConnector connect, int run, int group) {		
		this.connect = connect;
		this.runId = run;
		fileFilters = connect.getFileFilters(group);
		dirFilters = connect.getDirFilters(group);
	}


	public void crawl(Path path, boolean recursive) throws IOException {		
		log.info("Crawling directory ["+path.toString()+"]");
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
	        for (Path entry : stream) {
	            if (Files.isDirectory(entry)) {
	                if (recursive && passFilter(entry,dirFilters)) crawl(entry, recursive);
	            } else {
	            	if (passFilter(entry,fileFilters)) connect.addBackupItem(runId, entry.toAbsolutePath().toString(), Files.size(entry), Files.getLastModifiedTime(entry).toMillis());
	            }	            
	        }
	        stream.close();
	    }
	}
	
	private boolean passFilter(Path path, String[] filters) {
		System.out.println(path.getFileName().toString());
		for (String filter : filters){
			String regex= filter.replace("*", "(.*)");
			System.out.println("regex: " + regex);
		    if(path.getFileName().toString().matches(regex)) {
		        log.info("Filtering ["+path+"]");
		    	return false;
		    }
		}		
		
		return true;
	}
}
