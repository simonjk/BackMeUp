package com.klaiber.backmeup;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class FileRestoreWorker implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	
	public static boolean restoreFile(String sourceDrivePath, String sourceDriveName, String hash, String targetFile, boolean overwrite){
		Logger log = LogHandler.getLogger();
		
		try{
		   File sourceDrive = new File(sourceDrivePath);
		   while (FileCopyWorker.checkDrive(sourceDrive, sourceDriveName, 0, 0, 0)!= 0){
			   log.info("insert drive ["+sourceDriveName+"] to  ["+sourceDrivePath+"]");
			   Thread.sleep(30000);
		   }
		   
		   File srcdir = new File (sourceDrive.getAbsolutePath()+"/"+hash.substring(0, 2)+"/"+hash.substring(0, 4)+((hash.startsWith("com")||hash.startsWith("lpt"))?"_":"")+"/"+hash.substring(0, 6)+"/"+hash.substring(0, 8));
		   File src = new File(srcdir.getAbsolutePath()+"/"+hash);
		   
		   File tgt = new File(targetFile);
		   File tgtdir = tgt.getParentFile();
		   
		   //check if target dir exist and create if necessary
		   if (!tgtdir.exists()){
			   tgtdir.mkdirs();
		   }
		   
		   // check if target exist if overwrite = false
		   if (!overwrite){
			   if (tgt.exists()){
				   log.warning("File allready exists ["+targetFile+"] from ["+sourceDriveName+"] Hash ["+hash+"]");
				   return false;
			   }
		   }
		   
		   
		   FileUtils.copyFile(src, tgt);
			
			
		} catch (Exception e) {
			e.printStackTrace();		
			log.warning("Error restoring ["+targetFile+"] from ["+sourceDriveName+"] Hash ["+hash+"]");
			log.warning(e.getMessage());
			return false;
		}
		log.info("Successfull restored ["+targetFile+"] from ["+sourceDriveName+"] Hash ["+hash+"]");
		return true;
	}
	
	
}
