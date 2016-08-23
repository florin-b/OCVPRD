package com.arabesque.obiectivecva;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.arabesque.obiectivecva.connection.ConnectionStrings;
import com.arabesque.obiectivecva.listeners.AsyncTaskListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class WebServiceCall extends AsyncTask<Void, Void, String> {
	String errMessage = "";
	Context mContext;
	private ProgressDialog dialog;
	private AsyncTaskListener listener;
	private String methodName;
	private Map<String, String> params;

	public WebServiceCall(Context context, String methodName, Map<String, String> params) {
		super();
		this.mContext = context;
		this.listener = (AsyncTaskListener) mContext;
		this.methodName = methodName;
		this.params = params;
	}

	protected void onPreExecute() {
		dialog = new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Asteptati...");
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected String doInBackground(Void... url) {
		String response = "";
		try {
			SoapObject request = new SoapObject(ConnectionStrings.getInstance().getNamespace(), methodName);

			for (Entry<String, String> entry : params.entrySet()) {
				request.addProperty(entry.getKey(), entry.getValue());
			}

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(ConnectionStrings.getInstance().getUrl(), 60000);

			List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
			headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode("bflorin:bflorin".getBytes())));
			androidHttpTransport.call(ConnectionStrings.getInstance().getNamespace() + methodName, envelope, headerList);
			Object result = envelope.getResponse();
			response = result.toString();
		} catch (Exception e) {
			errMessage = e.getMessage();
		}
		return response;
	}

	protected void onPostExecute(String result) {
		// TODO

		try {
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}

			if (!errMessage.equals("")) {
				
			} else {
				listener.onTaskComplete(methodName, result);
			}
		} catch (Exception e) {
			
		}
	}

}