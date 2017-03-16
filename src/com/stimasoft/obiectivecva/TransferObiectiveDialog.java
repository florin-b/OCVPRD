package com.stimasoft.obiectivecva;

import android.view.View.OnClickListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.beans.BeanDateTabele;
import com.arabesque.obiectivecva.enums.EnumFiliale;
import com.arabesque.obiectivecva.listeners.AsyncTaskListener;
import com.arabesque.obiectivecva.listeners.AsyncTaskWSCall;
import com.arabesque.obiectivecva.listeners.OperatiiAgentListener;
//import com.arabesque.obiectivecva.listeners.OperatiiAgentListenerTo;
import com.arabesque.obiectivecva.model.Agent;
import com.arabesque.obiectivecva.model.OperatiiAgent;
import com.arabesque.obiectivecva.model.OperatiiTabele;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.Setup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TransferObiectiveDialog extends Dialog implements OperatiiAgentListener, AsyncTaskListener {

	private Context context;
	private Spinner spinnerFiliale, spinnerConsilieriFrom, spinnerConsilieriTo;
	private OperatiiAgent opAgenti;
	private String codFiliala, codAgentVechi, codAgentNou;
	private Button btnTransfer, btnCancel;
	private TextView message;
	private static final String CHANGE_AGENT = "schimbaAgentObiecticeCVA";

	public TransferObiectiveDialog(Context context) {
		super(context, R.style.TransferObiectiveDialog2);

		this.context = context;
		setContentView(R.layout.transferobiective);

		setTitle("Transfer obiective");
		setCancelable(true);

		opAgenti = OperatiiAgent.getInstance();
		opAgenti.setOperatiiAgentListener(this);

		setupLayout();

	}

	private void setupLayout() {
		spinnerFiliale = (Spinner) findViewById(R.id.spinner_objective_filialaFrom);
		spinnerConsilieriFrom = (Spinner) findViewById(R.id.spinner_objective_consilierFrom);
		spinnerConsilieriTo = (Spinner) findViewById(R.id.spinner_objective_consilierTo);

		btnTransfer = (Button) findViewById(R.id.btnTransferaObiective);
		btnCancel = (Button) findViewById(R.id.btnCancel);

		
		message = (TextView) findViewById(R.id.mesage);
		message.setVisibility(View.INVISIBLE);

		setSpinnerConsilieriListenerTo();
		setSpinnerConsilieriListenerFrom();
		populateSpinnerFiliale();
		setbtnActions();

	}

	private void schimbaAgentObiectiveCVA(String codAgentVechi, String codAgentNou) {

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("codAgentVechi", codAgentVechi);
		params.put("codAgentNou", codAgentNou);

		AsyncTaskListener contextListener = (AsyncTaskListener) TransferObiectiveDialog.this;
		AsyncTaskWSCall call = new AsyncTaskWSCall(context, contextListener, CHANGE_AGENT, params);
		call.getCallResultsFromFragment();

	}

	private void setbtnActions() {

		btnTransfer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String valueStart= spinnerConsilieriFrom.getSelectedItem().toString();
				String valueEnd = spinnerConsilieriTo.getSelectedItem().toString();

				int posStart = spinnerConsilieriFrom.getSelectedItemPosition();
				int posEnd = spinnerConsilieriTo.getSelectedItemPosition();

				
				if (posStart == 0 && posEnd == 0) { 
					Toast.makeText(getContext(), "Selectati un agent", Toast.LENGTH_LONG).show();
					
				}
				else if (valueStart.equals(valueEnd)){
				Toast.makeText(getContext(), "Agenti sunt la fel", Toast.LENGTH_LONG).show();
				}
		
				else if(posStart==0){
					Toast.makeText(getContext(), "Selectati un  agent", Toast.LENGTH_LONG).show();
				}
				else if(posEnd==0){
					Toast.makeText(getContext(), "Selectati un  agent", Toast.LENGTH_LONG).show();
				}
				else{
					schimbaAgentObiectiveCVA(codAgentVechi, codAgentNou);
			
				}
			}
				
					
					
			

			

		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

	}

	private void populateSpinnerFiliale() {

		List<String> listFiliale = EnumFiliale.getFiliale();
		String[] arrayFiliale = listFiliale.toArray(new String[listFiliale.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, arrayFiliale);
		spinnerFiliale.setAdapter(dataAdapter);
		setSpinnerFilialeListener();

	}

	private void populateSpinnerConsilieri(List<Agent> listAgenti) {
		spinnerConsilieriFrom.setAdapter(new ArrayAdapter<Agent>(getContext(), android.R.layout.simple_list_item_1, listAgenti));
		spinnerConsilieriTo.setAdapter(new ArrayAdapter<Agent>(getContext(), android.R.layout.simple_list_item_1, listAgenti));

	}

	private void setSpinnerConsilieriListenerFrom() {
		spinnerConsilieriFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			
				if (position > 0) {
					codAgentVechi = ((Agent) spinnerConsilieriFrom.getSelectedItem()).getCod();
					// ((TextView) arg0.getChildAt(0)).setTextColor(Color.BLUE);
					((TextView) arg0.getChildAt(0)).setTextSize(17);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

	}

	private void setSpinnerConsilieriListenerTo() {
		spinnerConsilieriTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {

				if (position > 0) {
					codAgentNou = ((Agent) spinnerConsilieriTo.getSelectedItem()).getCod();
					((TextView) arg0.getChildAt(0)).setTextSize(17);

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

	}

	private void populateListConsilieri(String codFiliala) {
		// TODO Auto-generated method stub
		opAgenti.getListaAgenti(codFiliala, "11", getContext(), true);

	}

	private void setSpinnerFilialeListener() {
		spinnerFiliale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				if (position > 0) {

					codFiliala = EnumFiliale.getCodFiliala(parent.getAdapter().getItem(position).toString());
					populateListConsilieri(codFiliala);

				}

				message.setVisibility(View.INVISIBLE);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	@Override
	public void opAgentComplete(ArrayList<HashMap<String, String>> listAgenti) {
		// TODO Auto-generated method stub
		populateSpinnerConsilieri(opAgenti.getListObjAgenti());

	}

	@SuppressLint("ShowToast")
	@Override
	public void onTaskComplete(String methodName, Object result) {
		if (methodName.equals(CHANGE_AGENT)) {
			String transfer = (String) result;

			if (transfer.equals("true")) {
				message.setVisibility(View.VISIBLE);
				message.setText("Obiectivele au fost transferate");

			} else {
				Toast.makeText(getContext(), "Obiectivele nu au fost transferate", Toast.LENGTH_LONG).show();
			}
		}

	}

}
