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

}
