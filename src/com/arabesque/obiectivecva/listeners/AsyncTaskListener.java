package com.arabesque.obiectivecva.listeners;

public interface AsyncTaskListener {
	void onTaskComplete(String methodName, Object result);
}
