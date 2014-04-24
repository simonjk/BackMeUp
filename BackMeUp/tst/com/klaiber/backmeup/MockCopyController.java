package com.klaiber.backmeup;

public class MockCopyController implements FileCopyStatusReciever {

	private boolean returned =false;
	private boolean actionRequested = false;
	private int actionStatusCode = -1;
	private int finishedStatusCode = -1;
	
	public boolean isReturned() {
		return returned;
	}

	public boolean isActionRequested() {
		return actionRequested;
	}

	public int getActionStatusCode() {
		return actionStatusCode;
	}
	
	
	public int getFinishedStatusCode() {
		return finishedStatusCode;
	}

	@Override
	public void returnFinished(FileCopyWorker fcw, int statusCode) {		
		returned = true;
		finishedStatusCode = statusCode;
	}

	@Override
	public void requestAction(FileCopyWorker fcw, int statusCode) {
		actionRequested = true;
		actionStatusCode = statusCode;
	}

}
