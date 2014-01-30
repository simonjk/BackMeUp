package com.klaiber.backmeup;

import java.util.Map;
import java.util.Set;

public interface DBConnector {
	
	public boolean open(String ConnectString);
	
	public boolean isActive();
	
	public boolean init();
	
	public Set<String> getDirectories(int group);
	
	public Map<Integer,String> getGroups();
	
	public int createRun(int group);
	
	public Run getRun(int run);
	
	public Map<Integer,Run> getAllRuns(int group);
	
	public Map<Integer,Run> getUnfinishedRuns(int group);
	
	public Map<Integer,Run> getFinishedRuns(int group);
	
	public Map<Integer,Run> getSuccesfulRuns(int group);
	
	public Map<Integer,Run> getFailedRuns(int group);
	
	public boolean finishRun(int run, boolean success);
	
	public boolean addBackupItem(int run, String path, long size, long modified);
	
	public long checkForUnmodifiedFiles(int run);
	
	public long matchHashes(int run);
	
	public Set<BackupItem> getUnhashedItems(int run);
	
	public boolean setBackupItemHash(BackupItem backupItem);
	
	public Set<BackupItem> getUnsavedItems(int run, boolean external);
	
	public boolean setItemSaved(BackupItem item, int drive, boolean external);
	

}
