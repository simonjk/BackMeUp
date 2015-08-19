package com.klaiber.backmeup;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.apache.commons.io.FileUtils;

/*
 * Return Codes Finished
 * 0 = Successfull
 * 1 = wrong hash
 * 2 = src file does not exist
 * 3 = File to large for remaining space
 * 4 = drive Full
 * 5 = target allready exists
 * 6 = Error during Copy
 * 7 = INVALID is not an allowed drive Name
 * 100 = stopped
 * 111 = crashed
 * Action Required
 * 1 = tgt drive does not exist
 * 2 = tgt drive not initialized
 * 3 = wrong tgt drive 
 * 4 = token not readable 
 * 
 */

public class FileCopyWorker implements Runnable {
	
	private Logger log = LogHandler.getLogger();
	
	private File src;
	private File drive;
	private String driveName;
	private String hash;
	private long spaceReserve;
	private long maxIgnoredSpace;
	private FileCopyStatusReciever statusReciever;
	private boolean continueProcess = false;
	private boolean waiting = false;
	private boolean stop = false;
	
	@Override
	public void run() {
		if (!src.isFile()) {
			statusReciever.returnFinished(this, 2);
			return;
		}
			
		if (stop) {
			statusReciever.returnFinished(this, 100);
			return;
		}
		
		int dsc = checkDrive(drive, driveName, src.length(), spaceReserve, maxIgnoredSpace);
		if (dsc < 0) {
			statusReciever.returnFinished(this, (dsc*-1));
			return;
		}
		while ( dsc != 0 ){
			waiting = true;
			statusReciever.requestAction(this, dsc);
			while (!continueProcess){
				try{
					if (stop) {
						statusReciever.returnFinished(this, 100);
						return;
					}
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					//log uncritical exception
					e.printStackTrace();
					statusReciever.returnFinished(this, 111);
					return;
				}
			}
			waiting = false;
			dsc = checkDrive(drive, driveName, src.length(), spaceReserve, maxIgnoredSpace);
			continueProcess = false;
			if (dsc < 0) {
				statusReciever.returnFinished(this, (dsc*-1));
				return;
			}
		}
		
		
		if (stop) {
			statusReciever.returnFinished(this, 100);
			return;
		}
		//build target filename
		File tgtdir = new File (drive.getAbsolutePath()+"/"+hash.substring(0, 2)+"/"+hash.substring(0, 4)+((hash.startsWith("com")||hash.startsWith("lpt"))?"_":"")+"/"+hash.substring(0, 6)+"/"+hash.substring(0, 8));
		File tgt = new File(tgtdir.getAbsolutePath()+"/"+hash);
		//Check if target exists
		if (tgtdir.isDirectory()) {
			if (tgt.exists())  {
				String tgtHash = BackupItem.generateHashForFile(tgt.getAbsolutePath());		
				if (!tgtHash.equalsIgnoreCase(hash)){
					log.info("File ["+tgt.getAbsolutePath()+"] already but had wrong hash ["+tgtHash+"]");
					statusReciever.returnFinished(this, 5);
					return;
					
				} else {
					log.info("File ["+tgt.getAbsolutePath()+"] already existed and was identically");
					statusReciever.returnFinished(this, 0);
					return;
				}
			}
		} else {
			//create tgt dir
			tgtdir.mkdirs();
		}
		
		if (stop) {
			statusReciever.returnFinished(this, 100);
			return;
		}
		
		//copy file
		try {
			FileUtils.copyFile(src, tgt);
		} catch (IOException e){
			log.warning(e.getMessage());
			statusReciever.returnFinished(this, 6);
			return;
		}
		
		if (stop) {
			statusReciever.returnFinished(this, 100);
			return;
		}
		
		//verify tgt file
		String tgtHash = BackupItem.generateHashForFile(tgt.getAbsolutePath());		
		if (!tgtHash.equalsIgnoreCase(hash)){
			log.info("tgt Hash ["+tgtHash+"]" );
			log.info("src Hash ["+hash+"]" );
			tgt.delete();
			statusReciever.returnFinished(this, 1);			
			return;
		} else {
			statusReciever.returnFinished(this, 0);
			return;
		}

	}
	
	public void continueProcess(){
		if(waiting) {
			continueProcess = true;			
		}
	}
	
	public void stopProcess(){
		stop = true;
	}
	
	
	public static int checkDrive(File drive, String driveName, long size, long space_reserve, long max_ignored_space){
		int status = 0;
		// check if target drive is there
		if (!drive.isDirectory()) return 1;
		
		//check if target drive is initialized
		File tokenfile = new File(drive.getAbsolutePath()+"/_bmudrive");
		if (!tokenfile.isFile()) return 2;
		
		String token = "INVALID";
		try {
			token = FileUtils.readFileToString(tokenfile);
		} catch (IOException e) {
			return 4;
		}
		
		//check if correct target drive is there
		if (driveName.equalsIgnoreCase("INVALID")) return -7;
		if (!token.equalsIgnoreCase(driveName)) return 3;
				
		//check if file is to big and if drive is full
		long rem_space = drive.getFreeSpace();
		if (rem_space - size < space_reserve){
			if (size > max_ignored_space) {
				//System.out.println("Size: " + size);
				//System.out.println("maxig: " + max_ignored_space);
				return -3;
			} else {
				return -4;
			}
		}
		
		return status;
	}

	public FileCopyWorker(File src, File drive, String driveName, String hash,
			 long spaceReserve, long maxIgnoredSpace, FileCopyStatusReciever statusReciever) {
		super();
		this.src = src;
		this.drive = drive;
		this.driveName = driveName;
		this.hash = hash;
		this.spaceReserve = spaceReserve;
		this.maxIgnoredSpace = maxIgnoredSpace;
		this.statusReciever = statusReciever;
	}

	public File getSrc() {
		return src;
	}

	public File getDrive() {
		return drive;
	}

	public String getDriveName() {
		return driveName;
	}

	public String getHash() {
		return hash;
	}

	public long getSpaceReserve() {
		return spaceReserve;
	}

	public long getMaxIgnoredSpace() {
		return maxIgnoredSpace;
	}

	
	public FileCopyStatusReciever getStatusReciever() {
		return statusReciever;
	}

}
