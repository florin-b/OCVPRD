package com.arabesque.obiectivecva.model;

import java.util.HashMap;

import com.arabesque.obiectivecva.listeners.AsyncTaskListener;
import com.arabesque.obiectivecva.listeners.AsyncTaskWSCall;
import com.arabesque.obiectivecva.listeners.OperatiiAdresaListener;

import android.content.Context;

public class OperatiiAdrese implements AsyncTaskListener {

	private OperatiiAdresaListener listener;

	public void getAdreseJudet(String codJudet, Context context) {

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("codJudet", codJudet);

		AsyncTaskListener contextListener = (AsyncTaskListener) OperatiiAdrese.this;
		AsyncTaskWSCall call = new AsyncTaskWSCall(context, contextListener, "getAdreseJudet", params);
		call.getCallResultsFromFragment();
	}

	public void setOpAdresaListener(OperatiiAdresaListener listener) {
		this.listener = listener;
	}

	@Override
	public void onTaskComplete(String methodName, Object result) {
		if (listener != null) {
			listener.opAdresaComplete((String) result);
		}

	}

}
