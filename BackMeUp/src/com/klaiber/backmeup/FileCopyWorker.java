package com.klaiber.backmeup;

import java.io.File;

public class FileCopyWorker implements Runnable {
	private File src;
	private File drive;
	private String driveName;
	private String hash;
	private long space_reserve;
	private FileCopyStatusReciever statusReciever;
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	public static int checkDrive(File drive, String driveName, long size, long space_reserve ){
		int status = 0; 
		
		return status;
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

	public long getSpace_reserve() {
		return space_reserve;
	}

	public FileCopyStatusReciever getStatusReciever() {
		return statusReciever;
	}

}
