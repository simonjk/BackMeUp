package com.klaiber.backmeup;

public interface FileCopyStatusReciever {

	public void returnFinished(FileCopyWorker fcw, int statusCode);
	
	public void requestAction(FileCopyWorker fcw, int statusCode);
}
