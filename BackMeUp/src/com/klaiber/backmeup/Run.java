package com.klaiber.backmeup;

import java.util.Date;

public class Run {
	
	private int id;
	private int backupgroup;
	private Date started;
	private Date finished;
	private boolean success;
	
	public Run(int id, int backupgroup, Date started, Date finished,
			boolean success) {
		super();
		this.id = id;
		this.backupgroup = backupgroup;
		this.started = started;
		this.finished = finished;
		this.success = success;
	}
	public int getId() {
		return id;
	}
	public int getBackupgroup() {
		return backupgroup;
	}
	public Date getStarted() {
		return started;
	}
	public Date getFinished() {
		return finished;
	}
	public boolean isSuccess() {
		return success;
	} 

}

