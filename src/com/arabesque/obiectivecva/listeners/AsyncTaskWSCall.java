package com.arabesque.obiectivecva.listeners;

import java.util.HashMap;

import com.arabesque.obiectivecva.WebServiceCall;
import com.arabesque.obiectivecva.WebServiceCallFromFragment;

import android.content.Context;

public class AsyncTaskWSCall {

	private String methodName;
	private HashMap<String, String> params;
	private Context context;
	private AsyncTaskListener contextListener;

	public AsyncTaskWSCall(Context context) {
		this.context = context;
	}

	public AsyncTaskWSCall(String methodName, Context context) {
		this.context = context;
		this.methodName = methodName;
	}

	public AsyncTaskWSCall(AsyncTaskListener contextListener, Context context) {
		this.context = context;
		this.contextListener = contextListener;
	}

	public AsyncTaskWSCall(String methodName, HashMap<String, String> params, AsyncTaskListener myListener,
			Context context) {
		this.contextListener = myListener;
		this.methodName = methodName;
		this.params = params;
		this.context = context;

	}

	public AsyncTaskWSCall(Context context, String methodName, HashMap<String, String> params) {
		this.context = context;
		this.methodName = methodName;
		this.params = params;
	}

	public AsyncTaskWSCall(Context context, AsyncTaskListener contextListener, String methodName,
			HashMap<String, String> params) {
		this.context = context;
		this.methodName = methodName;
		this.params = params;
		this.contextListener = contextListener;
	}

	public void getCallResults() {
		new WebServiceCall(context, methodName, params).execute();
	}

	public void getCallResultsFromFragment() {
		new WebServiceCallFromFragment(context, contextListener, methodName, params).execute();
	}
	
	
}
