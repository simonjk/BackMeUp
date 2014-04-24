package com.klaiber.backmeup;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;

public class FileCopyController implements FileCopyStatusReciever {

	private LinkedHashSet<BackupItem> InternalItems;
	private LinkedHashSet<BackupItem> ExternalItems;
	private LinkedHashSet<BackupItem> InternalSkippedItems = new LinkedHashSet<BackupItem>();
	private LinkedHashSet<BackupItem> ExternalSkippedItems = new LinkedHashSet<BackupItem>();
	private LinkedHashSet<String> InternalDrives;
	private LinkedHashSet<String> ExternalDrives;
	private String InternalDrivePath;
	private String ExternalDrivePath;
	private int externalOffset;
	private long spaceReserve;
	private long maxIgnoredSpace; 
	private boolean internalRunning = false;
	private boolean externalRunning = false;
	private boolean stopped = false;
	private FileCopyWorker internalFcw;
	private FileCopyWorker externalFcw;
	private Thread internalThread;
	private Thread externalThread;
	private String currentInternalDriveName; 
	private String currentExternalDriveName;
	
	public FileCopyController(LinkedHashSet<BackupItem> internalItems,
			LinkedHashSet<BackupItem> externalItems, LinkedHashSet<String> internalDrives,
			LinkedHashSet<String> externalDrives, String internalDrivePath,
			String externalDrivePath, int externalOffset, long spaceReserve, long maxIgnoredSpace) {
		super();
		InternalItems = internalItems;
		ExternalItems = externalItems;
		InternalDrives = internalDrives;
		ExternalDrives = externalDrives;
		InternalDrivePath = internalDrivePath;
		ExternalDrivePath = externalDrivePath;
		this.externalOffset = externalOffset;
		this.spaceReserve = spaceReserve; 
		this.maxIgnoredSpace = maxIgnoredSpace;
	}

	
	public int saveItems(){
		int status = 0;
		int internalCount = 0;
		currentInternalDriveName = InternalDrives.iterator().next();
		currentExternalDriveName = ExternalDrives.iterator().next();
		while (!stopped && (InternalItems.size()>0 || ExternalItems.size()>0)){
			if (!internalRunning && InternalItems.size()>0){
				internalCount++;								
				BackupItem bui = InternalItems.iterator().next();
				internalFcw = new FileCopyWorker(new File(bui.getPath()), new File(InternalDrivePath), currentInternalDriveName, bui.getHash(), spaceReserve, maxIgnoredSpace, this);
				internalRunning = true;
				internalThread = new Thread(internalFcw);
				internalThread.start();
			}

			if (internalCount > externalOffset && !externalRunning && ExternalItems.size()>0){						
				BackupItem bui = ExternalItems.iterator().next();
				externalFcw = new FileCopyWorker(new File(bui.getPath()), new File(ExternalDrivePath), currentExternalDriveName, bui.getHash(), spaceReserve, maxIgnoredSpace, this);
				externalRunning = true;
				externalThread = new Thread(internalFcw);
				externalThread.start();
			}
			
			
			//ADD Skipped to the End
			if (InternalSkippedItems.size()>0){
				InternalItems.addAll(InternalSkippedItems);
				InternalSkippedItems = new LinkedHashSet<BackupItem>();				
			}			
			if (ExternalSkippedItems.size()>0){
				ExternalItems.addAll(ExternalSkippedItems);
				ExternalSkippedItems = new LinkedHashSet<BackupItem>();				
			}
		}
		
		return status;
	}
	
	
	@Override
	public void returnFinished(FileCopyWorker fcw, int statusCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestAction(FileCopyWorker fcw, int statusCode) {
		// TODO Auto-generated method stub
		
	}



}
