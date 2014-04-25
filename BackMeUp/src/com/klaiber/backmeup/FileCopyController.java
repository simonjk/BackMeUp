package com.klaiber.backmeup;

import java.io.File;
//import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
//import java.util.Set;
//import java.util.List;

public class FileCopyController implements FileCopyStatusReciever {

	private DBConnector con;
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
	private BackupItem internalBui;
	private BackupItem externalBui;
	private Thread internalThread;
	private Thread externalThread;
	private String currentInternalDriveName; 
	private String currentExternalDriveName;
	private Logger log;
	
	public FileCopyController(DBConnector con, LinkedHashSet<BackupItem> internalItems,
			LinkedHashSet<BackupItem> externalItems, LinkedHashSet<String> internalDrives,
			LinkedHashSet<String> externalDrives, String internalDrivePath,
			String externalDrivePath, int externalOffset, long spaceReserve, long maxIgnoredSpace) {
		super();
		this.con = con;
		InternalItems = internalItems;
		ExternalItems = externalItems;
		InternalDrives = internalDrives;
		ExternalDrives = externalDrives;
		InternalDrivePath = internalDrivePath;
		ExternalDrivePath = externalDrivePath;
		this.externalOffset = externalOffset;
		this.spaceReserve = spaceReserve; 
		this.maxIgnoredSpace = maxIgnoredSpace;
		log = Logger.getLogger("FileCopyController");
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
				internalBui = bui;
				InternalItems.remove(bui);
				internalThread = new Thread(internalFcw);
				internalThread.start();
			}

			if (internalCount > externalOffset && !externalRunning && ExternalItems.size()>0){						
				BackupItem bui = ExternalItems.iterator().next();
				externalFcw = new FileCopyWorker(new File(bui.getPath()), new File(ExternalDrivePath), currentExternalDriveName, bui.getHash(), spaceReserve, maxIgnoredSpace, this);
				externalRunning = true;
				externalBui = bui;
				ExternalItems.remove(bui);
				externalThread = new Thread(externalFcw);
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
		boolean external = false;
		BackupItem bui = null;
		LinkedHashSet<BackupItem> skipped = null;
		LinkedHashSet<String> driveNames = null;
		if (fcw == internalFcw){			
			external = false;	
			internalFcw = null;
			internalRunning = false;
			bui = internalBui;
			driveNames = InternalDrives;
			internalBui = null;
			skipped = InternalSkippedItems;
		} else if (fcw == externalFcw)
		if (statusCode == 0){
			external = true;
			externalFcw = null;
			externalRunning = false;
			bui = externalBui;
			driveNames = ExternalDrives;
			externalBui = null;
			skipped = ExternalSkippedItems;
		} else {
			stopped = true;
			log.severe("Unkown Worker returned ["+fcw.getSrc().getAbsolutePath()+"]");
			return;
		}
		if (statusCode == 0){
			con.setItemSaved(bui, fcw.getDriveName(), external);
			log.info("Successful saved  ["+fcw.getSrc().getAbsolutePath()+"] to ["+fcw.getDriveName()+"] as ["+(external?"External":"Internal")+"]. Status Code:["+statusCode+"]");
			
		} else if (statusCode == 3 || statusCode == 4){
			skipped.add(bui);
			log.info("Not enough Space when saving ["+fcw.getSrc().getAbsolutePath()+"] to ["+fcw.getDriveName()+"] as ["+(external?"External":"Internal")+"] skipping File. Status Code:["+statusCode+"]");
			if (statusCode == 4) {
				String newDrive = ""; 
				driveNames.remove(fcw.getDriveName());
				newDrive = driveNames.iterator().next();
				if (external){
					currentExternalDriveName = newDrive;					
				} else {
					currentInternalDriveName = newDrive;
				}
				log.info("Switched Full Drive ["+fcw.getDriveName()+"] with drive  ["+newDrive+"] ["+(external?"External":"Internal")+"]");
				con.setDriveFull(fcw.getDriveName());
				
			}
		} else {
			log.warning("Error backup ["+fcw.getSrc().getAbsolutePath()+"] to ["+fcw.getDriveName()+"] as ["+(external?"External":"Internal")+"] Status Code:["+statusCode+"]");
		}
		
	}

	@Override
	public void requestAction(FileCopyWorker fcw, int statusCode) {
		log.info("Please insert correct Drive ["+fcw.getDriveName()+"] to Path ["+fcw.getDrive().getAbsolutePath()+"], Status Code:["+statusCode+"]. Rechecking in 3 Minutes");
		try {
			TimeUnit.MINUTES.sleep(3);			
		} catch (Exception e) {
			fcw.continueProcess();
			return;
		}
		fcw.continueProcess();
		return;		
	}



}
