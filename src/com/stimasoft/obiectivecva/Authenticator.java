package com.stimasoft.obiectivecva;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONException;
import org.json.JSONObject;

import com.arabesque.obiectivecva.Logon;
import com.arabesque.obiectivecva.UserInfo;

import com.arabesque.obiectivecva.Utils;
import com.arabesque.obiectivecva.beans.BeanDateTabele;
import com.arabesque.obiectivecva.listeners.AsyncTaskListener;
import com.arabesque.obiectivecva.listeners.AsyncTaskWSCall;
import com.arabesque.obiectivecva.model.OperatiiTabele;
import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.notifications.AlarmReceiver;
import com.stimasoft.obiectivecva.notifications.ServiceSendNotification;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Authenticator extends AppCompatActivity implements AsyncTaskListener {

	// UI Elements
	private Button buttonLogin;
	private EditText username;
	private EditText password;
	private RadioGroup radioGroup;

	private SQLiteHelper sqLiteDatabase;
	String RECEIVER_ACTION = "com.stimasoft.obiectiveCva.START_NOTIFICATIONS";
	private static final String LOGON_SERVICE = "userLogon";
	private static final String POPULATE_TABLES = "getListObiectiveCVA";

	private String buildVer = "0";
	private FTPClient mFTPClient = null;
	private Bundle bundle = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authenticator);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		LinearLayout root = (LinearLayout) findViewById(R.id.relativeLayout_authenticatorRoot);
		root.requestFocus();

		sqLiteDatabase = new SQLiteHelper(this);
		sqLiteDatabase.doSomething();

		buttonLogin = (Button) findViewById(R.id.button_login);
		buttonLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				performLogon();
			}
		});

		username = (EditText) findViewById(R.id.editText_username);

		password = (EditText) findViewById(R.id.editText_password);
		password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					performLogon();
				}
				return false;
			}
		});

		username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					password.requestFocus();
				}
				return false;
			}
		});

		radioGroup = (RadioGroup) findViewById(R.id.radioGroup_roles);

		ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
		PackageManager pm = this.getPackageManager();

		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

		int status = getPackageManager().getComponentEnabledSetting(receiver);
		Log.d("DBG", "RECEIVER STATUS: " + Integer.toString(status));

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

		}

		buildVer = String.valueOf(pInfo.versionCode);

		bundle = getIntent().getExtras();

		checkForUpdate();

	}

	public static String getStackTrace(Exception ex) {
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

	private void loadExtraInfo(Bundle b) {

		if (!getIntent().hasExtra("UserInfo"))
			return;

		Utils.deserializeUserInfo(b.getString("UserInfo"), getApplicationContext());
		b.remove("UserInfo");
		
		username.setEnabled(false);
		password.setEnabled(false);
		buttonLogin.setEnabled(false);

		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			radioGroup.getChildAt(i).setEnabled(false);
		}

		if (UserInfo.getInstance().getTipUser().equals("CV")) {
			radioGroup.check(R.id.radio_consultant);
			populateLocalTables();
		} else if (UserInfo.getInstance().getTipUser().equals("DV"))
			radioGroup.check(R.id.radio_director);

	}

	private void checkForUpdate() {
		new CheckUpdate(this).execute();
	}

	private class CheckUpdate extends AsyncTask<Void, Void, String> {
		String errMessage = "";
		Context mContext;
		private ProgressDialog dialog;

		private CheckUpdate(Context context) {
			super();
			this.mContext = context;
		}

		protected void onPreExecute() {

			this.dialog = new ProgressDialog(mContext);
			this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			this.dialog.setMessage("Verificare versiune aplicatie...");
			this.dialog.setCancelable(false);
			this.dialog.show();

		}

		@Override
		protected String doInBackground(Void... url) {
			String response = "";
			mFTPClient = new FTPClient();

			try {

				mFTPClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

				mFTPClient.connect("10.1.0.6", 21);

				if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {

					mFTPClient.login("litesfa", "egoo4Ur");

					mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
					mFTPClient.enterLocalPassiveMode();

					String sourceFile = "/Update/LiteSFA/ObiectiveCVAVer.txt";

					FileOutputStream desFile2 = new FileOutputStream("sdcard/download/ObiectiveCVAVer.txt");
					mFTPClient.retrieveFile(sourceFile, desFile2);

					desFile2.close();

				} else {
					errMessage = "Probeme la conectare!";
				}
			} catch (Exception e) {
				errMessage = e.getMessage();

			} finally {
				if (mFTPClient.isConnected()) {
					{
						try {
							mFTPClient.logout();
							mFTPClient.disconnect();
						} catch (IOException f) {
							errMessage = f.getMessage();
							Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_LONG).show();
						}
					}
				}
			}

			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			if (dialog != null) {
				dialog.dismiss();
			}

			if (!errMessage.equals("")) {
				Toast toast = Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				validateUpdate();
			}

		}

	}

	public void validateUpdate() {

		try {

			File fVer = new File(Environment.getExternalStorageDirectory() + "/download/ObiectiveCVAVer.txt");
			FileInputStream fileIS = new FileInputStream(fVer);
			BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
			String readString = buf.readLine();
			String[] tokenVer = readString.split("#");
			fileIS.close();

			if (!tokenVer[2].equals("0")) // 1 - fisierul este gata pentru
											// update, 0 - inca nu
			{

				if (Float.parseFloat(buildVer) < Float.parseFloat(tokenVer[3])) {
					// exista update
					try {
						downloadUpdate download = new downloadUpdate(this);
						download.execute("dummy");
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
					}

				}

				else {
					if (bundle != null) {
						loadExtraInfo(bundle);
					}
				}
			}

		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}

	private class downloadUpdate extends AsyncTask<String, Void, String> {
		String errMessage = "";
		Context mContext;
		private ProgressDialog dialog;

		private downloadUpdate(Context context) {
			super();
			this.mContext = context;
		}

		protected void onPreExecute() {
			this.dialog = new ProgressDialog(mContext);
			this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			this.dialog.setMessage("Descarcare...");
			this.dialog.setCancelable(false);
			this.dialog.show();

		}

		@Override
		protected String doInBackground(String... url) {
			String response = "";
			mFTPClient = new FTPClient();

			try {

				mFTPClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

				mFTPClient.connect("10.1.0.6", 21);

				if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {

					mFTPClient.login("litesfa", "egoo4Ur");

					mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
					mFTPClient.enterLocalPassiveMode();

					String sourceFile = "/Update/LiteSFA/ObiectiveCVA.apk";
					FileOutputStream desFile1 = new FileOutputStream("sdcard/download/ObiectiveCVA.apk");
					mFTPClient.retrieveFile(sourceFile, desFile1);

					sourceFile = "/Update/LiteSFA/ObiectiveCVAVer.txt";
					FileOutputStream desFile2 = new FileOutputStream("sdcard/download/ObiectiveCVAVer.txt");
					mFTPClient.retrieveFile(sourceFile, desFile2);

					desFile1.close();
					desFile2.close();

				} else {
					errMessage = "Probeme la conectare!";
				}
			} catch (Exception e) {
				errMessage = e.getMessage();
			} finally {
				if (mFTPClient.isConnected()) {
					{
						try {
							mFTPClient.logout();
							mFTPClient.disconnect();
						} catch (IOException f) {
							errMessage = f.getMessage();
						}
					}
				}
			}

			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			if (dialog != null) {
				dialog.dismiss();
			}

			if (!errMessage.equals("")) {
				Toast toast = Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				startInstall();
			}

		}

	}

	public void startInstall() {

		String fileUrl = "/download/ObiectiveCVA.apk";
		String file = android.os.Environment.getExternalStorageDirectory().getPath() + fileUrl;
		File f = new File(file);

		if (f.exists()) {

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "ObiectiveCVA.apk")),
					"application/vnd.android.package-archive");
			startActivity(intent);
			finish();
		} else {
			Toast toast = Toast.makeText(getApplicationContext(), "Fisier corupt, repetati operatiunea!", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_authenticator, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void performLogon() {
		HashMap<String, String> params = new HashMap<String, String>();

		String userN = username.getText().toString().trim();
		String passN = password.getText().toString().trim();

		params.put("userId", userN);
		params.put("userPass", passN);
		params.put("ipAdr", "");

		AsyncTaskWSCall call = new AsyncTaskWSCall(this, LOGON_SERVICE, params);
		call.getCallResults();
	}

	private void populateLocalTables() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("tipUser", UserInfo.getInstance().getTipUser());
		params.put("codUser", UserInfo.getInstance().getCod());

		AsyncTaskWSCall call = new AsyncTaskWSCall(this, POPULATE_TABLES, params);
		call.getCallResults();
	}

	private void launchHome() {

		SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);

		Intent i = new Intent();

		int selectedRadioID = radioGroup.getCheckedRadioButtonId();

		User userDetails = new User();

		switch (selectedRadioID) {
		case R.id.radio_consultant:

			if (UserInfo.getInstance().getTipUser().equals("CV")) {

				userDetails = new User(User.TYPE_CVA, Constants.CODE_BRANCH01, UserInfo.getInstance().getNume(), "", UserInfo.getInstance().getCod());

				i = new Intent(this, Objectives.class);
				i.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ONGOING);
				Intent newIntent = new Intent(Constants.NOTIFICATIONS);
				sendBroadcast(newIntent);
				Log.d("DBG", "SentBroadcast!!!!!");

				sharedPrefHelper.logIn(userDetails);
				startActivity(i);

			} else {
				Toast.makeText(getApplicationContext(), "Acces interzis", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.radio_director:

			if (UserInfo.getInstance().getTipUser().equals("DV")) {

				userDetails = new User(User.TYPE_DVA, Constants.CODE_BRANCH02, UserInfo.getInstance().getNume(), "", UserInfo.getInstance().getCod());

				i = new Intent(this, DirectorHome.class);

				sharedPrefHelper.logIn(userDetails);
				startActivity(i);
			} else {
				Toast.makeText(getApplicationContext(), "Acces interzis", Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}

	}

	public void testServices(View view) {

		Log.d("DBG", "Called on Destroy");
		ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
		PackageManager pm = this.getPackageManager();

		pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

		int status = getPackageManager().getComponentEnabledSetting(receiver);
		Log.d("DBG", "RECEIVER STATUS: " + Integer.toString(status));

		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent serviceIntent = new Intent(this, ServiceSendNotification.class);

		PendingIntent registeredPendingIntent = PendingIntent.getService(this, Constants.ALARM_REQUEST_CODE, serviceIntent, PendingIntent.FLAG_NO_CREATE);

		if (registeredPendingIntent != null) {
			alarmManager.cancel(registeredPendingIntent);
			registeredPendingIntent.cancel();
		}
	}

	public void onTaskComplete(String methodName, Object result) {
		if (methodName.equals(LOGON_SERVICE)) {
			validateLogon((String) result);
		}

		if (methodName.equals(POPULATE_TABLES)) {

			try {
				OperatiiTabele opTab = new OperatiiTabele();

				BeanDateTabele dateTabele = opTab.deserializeObiective((String) result);

				sqLiteDatabase.clearLocalTables();
				sqLiteDatabase.populateObjectivesPhases(dateTabele.getListStadii());
				sqLiteDatabase.populateBeneficiaries(dateTabele.getListBeneficiari());
				sqLiteDatabase.populateObjectives(dateTabele.getListObiective());

			} catch (Exception ex) {
				Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
			}

			launchHome();
		}

	} 

	private void validateLogon(String result) {
		Logon logon = new Logon();
		if (logon.validateLogin(result, getApplicationContext())) {

			if (UserInfo.getInstance().getTipUser().equals("CV"))
				populateLocalTables();
			else
				launchHome();

		}

	}
}
