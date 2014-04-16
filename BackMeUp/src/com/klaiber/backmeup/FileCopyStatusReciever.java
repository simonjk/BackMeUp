package com.klaiber.backmeup;

public interface FileCopyStatusReciever {

	public void returnFinished(FileCopyWorker fcw, int StatusCode);
	
	public void requestAction(FileCopyWorker fcw, int StatusCode);
}
