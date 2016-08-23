package com.stimasoft.obiectivecva;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.Utils;
import com.arabesque.obiectivecva.enums.EnumCategorieObiectiv;
import com.arabesque.obiectivecva.enums.EnumJudete;
import com.arabesque.obiectivecva.enums.EnumStadiuObiectiv;
import com.arabesque.obiectivecva.enums.EnumTipExecutant;
import com.arabesque.obiectivecva.model.OperatiiTabele;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stimasoft.obiectivecva.adapters.AutoCompleteBenefAdapter;
import com.stimasoft.obiectivecva.listeners.EditDateClickListener;
import com.stimasoft.obiectivecva.listeners.PhaseDurationChangeWatcher;
import com.stimasoft.obiectivecva.models.db_classes.Beneficiary;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.models.db_utilities.BeneficiaryData;
import com.stimasoft.obiectivecva.models.db_utilities.ObjectiveData;
import com.stimasoft.obiectivecva.models.db_utilities.PhaseData;
import com.stimasoft.obiectivecva.models.db_utilities.RegionData;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.Setup;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;
import com.stimasoft.obiectivecva.utils.StagePhaseSpinnerUtils;
import com.stimasoft.obiectivecva.utils.ui.DatePickerDialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddEditObjective extends AppCompatActivity implements DatePickerDialogFragment.SetLimitsInterface,
		PhaseDurationChangeWatcher.PhaseDurationChangedInterface {

	private int lastSelectedStageId = -1;
	private int lastSelectedPhaseId = -1;
	private int lastSelectedStageHierarchy = -1;
	private int lastSelectedPhaseHierarchy = -1;

	private List<Object> savedObjective;
	private User user;
	private Objective objectiveToEdit;

	private int purpose;
	private int mode;

	private boolean objectiveTypeRadioChecked = false;
	private boolean objectiveTypeChanged = false;
	private boolean objectiveStageChanged = false;

	private static final int EDIT = 0;
	private static final int ADD = 1;

	// Map Utils
	private GPSTracker gps;
	private Marker GPS_location = null;
	private GoogleMap map;
	private LatLng newMarkerPosition;

	private EditDateClickListener addDateClickListener;
	private EditDateClickListener authDateSClickListener;
	private EditDateClickListener authDateEClickListener;
	private EditDateClickListener phaseStartClickListener;
	private EditDateClickListener phaseEndClickListener;

	private EditText editText_objective_execName, editText_objective_execCUI, editText_objective_execNrRc,
			editText_objective_address_phone;
	private LinearLayout layout_executantDetailsName, layout_ExecutantDetailsCui, layout_executantDetailsNrRc;

	// Alin
	private EditText editText_objective_meserName, editText_objective_meserSurname, editText_objective_meserTel;
	private LinearLayout layout_meseriasName, layout_meseriasSurName, layout_meseriasNumarTel;
	// sf. Alin

	private boolean areGpsCoordsAutom = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_edit_objective);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
		user = sharedPrefHelper.getUserDetails();

		Setup setup = new Setup(this);
		setup.setupToolbarBack(toolbar);

		setupMapFunctionality();
		ObjectiveData objectiveData = new ObjectiveData(this);

		setupLayoutExecDetails();

		Intent intent = getIntent();
		if (intent.getExtras() != null) {
			mode = intent.getIntExtra(Constants.OBJECTIVES_MODE, -1);

			if (intent.getExtras().containsKey(Constants.KEY_COORDINATES)
					&& intent.getExtras().getString(Constants.KEY_PURPOSE).equals(Constants.VALUE_EDIT)) {
				objectiveToEdit = objectiveData
						.getObjectiveByCoords(intent.getExtras().getString(Constants.KEY_COORDINATES));
				Log.d("DBG", "Intrat in modul de editare din harta");
				purpose = EDIT;
				setupEditUi(objectiveToEdit);
			} else if (intent.getExtras().containsKey(Constants.KEY_ID)
					&& intent.getExtras().getString(Constants.KEY_PURPOSE).equals(Constants.VALUE_EDIT)) {
				objectiveToEdit = objectiveData.getObjectiveById(intent.getExtras().getInt(Constants.KEY_ID),
						intent.getExtras().getString(Constants.CVA_CODE));
				Log.d("DBG", "Intrat in modul de editare din lista");
				purpose = EDIT;
				setupEditUi(objectiveToEdit);
			} else {
				Log.d("DBG", "Intrat in modul de adaugare cu coordonate");
				purpose = ADD;
				setupAddUi();
			}
		} else {
			Log.d("DBG", "Intrat in modul de adaugare fara coordonate");
			purpose = ADD;
			setupAddUi();
		}
	}

	private void setupLayoutExecDetails() {
		layout_executantDetailsName = (LinearLayout) findViewById(R.id.layout_executantDetailsName);
		layout_ExecutantDetailsCui = (LinearLayout) findViewById(R.id.layout_ExecutantDetailsCui);
		layout_executantDetailsNrRc = (LinearLayout) findViewById(R.id.layout_executantDetailsNrRc);

		// Meserias Layout Fields, Author: Alin
		layout_meseriasName = (LinearLayout) findViewById(R.id.layout_meseriasName);
		layout_meseriasSurName = (LinearLayout) findViewById(R.id.layout_meseriasSurName);
		layout_meseriasNumarTel = (LinearLayout) findViewById(R.id.layout_meseriasNumarTel);
		// End Meserias layout
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		switch (purpose) {
		case EDIT:
			if (!(user.getUserType() == User.TYPE_DVA || mode == Constants.OBJECTIVES_ARCHIVE)) {
				getMenuInflater().inflate(R.menu.menu_edit_objective, menu);
			} else {
				getMenuInflater().inflate(R.menu.menu_archived_objective, menu);
			}
			break;
		case ADD:
			getMenuInflater().inflate(R.menu.menu_add_objective, menu);
			break;
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		switch (id) {
		case R.id.action_settings:
			return true;

		case android.R.id.home:
			Log.d("DBG", "Fired back button event");
			Intent i = getIntent();

			Setup setup = new Setup(AddEditObjective.this);
			setup.hideKeyboard();

			setResult(RESULT_CANCELED, i);
			finish();
			break;

		case R.id.action_save:
			try {
				saveNewObjective();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			}
			break;

		case R.id.action_saveModifications:
			saveObjectiveModifications();
			break;

		case R.id.action_archive:
			archiveObjective();
			break;

		case R.id.mapModHybrid:
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;

		case R.id.mapModNormal:
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;

		case R.id.mapModTerrain:
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void sendLocalDataToServer() {
		if (UserInfo.getInstance().getTipUser().equals("CV")) {
			OperatiiTabele operatiiTabele = new OperatiiTabele();
			operatiiTabele.salveazaTabelaObiective(this);
		}
	}

	/*
	 * Created a private class NumberTextWatcher in order to separate number
	 * values with commas Added: 13.07.2016, Author: Alin
	 */
	private class NumberTextWatcher implements TextWatcher {

		private DecimalFormat df;
		private DecimalFormat dfnd;
		private boolean hasFractionalPart;

		private EditText et;

		public NumberTextWatcher(EditText et) {
			df = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.US));
			df.setDecimalSeparatorAlwaysShown(true);
			dfnd = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));
			this.et = et;
			hasFractionalPart = false;
		}

		@SuppressWarnings("unused")
		private static final String TAG = "NumberTextWatcher";

		public void afterTextChanged(Editable s) {
			et.removeTextChangedListener(this);

			try {
				int inilen, endlen;
				inilen = et.getText().length();

				String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()),
						"");
				Number n = df.parse(v);
				int cp = et.getSelectionStart();
				if (hasFractionalPart) {
					et.setText(df.format(n));
				} else {
					et.setText(dfnd.format(n));
				}
				endlen = et.getText().length();
				int sel = (cp + (endlen - inilen));
				if (sel > 0 && sel <= et.getText().length()) {
					et.setSelection(sel);
				} else {
					// place cursor at the end?
					et.setSelection(et.getText().length() - 1);
				}
			} catch (NumberFormatException nfe) {
				// do nothing?
			} catch (ParseException e) {
				// do nothing?
			}

			et.addTextChangedListener(this);
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))) {
				hasFractionalPart = true;
			} else {
				hasFractionalPart = false;
			}
		}
	}

	/**
	 * Using input from the UI elements, generates an instance of the Objective
	 * class, the duration of the objective's phase and the phase's start and
	 * end dates
	 *
	 * @return A list containing: Objective instance, Phase duration, Phase
	 *         start and end date
	 */
	private List<Object> createObjectiveFromForm() {
		Objective objective;

		boolean thereAreErrors = false;

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		TextView addDateText = (TextView) findViewById(R.id.textView_objective_addDate);

		EditText nameText = (EditText) findViewById(R.id.editText_objective_name);
		EditText estValueText = (EditText) findViewById(R.id.editText_objective_estValue);
		RadioGroup objTypeGrp = (RadioGroup) findViewById(R.id.radioGroup_objectiveType);
		EditText beneficiaryText = (EditText) findViewById(R.id.acText_objective_beneficiary);
		RadioGroup benefTypeGrp = (RadioGroup) findViewById(R.id.radioGroup_beneficiaryType);
		TextView authDateStart = (TextView) findViewById(R.id.textView_objective_authStartDate);
		TextView authDateEnd = (TextView) findViewById(R.id.textView_objective_authEndDate);
		EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
		EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
		EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);
		Spinner stageSpinner = (Spinner) findViewById(R.id.spinner_stage);
		Spinner phaseSpinner = (Spinner) findViewById(R.id.spinner_phase);
		TextView phaseStart = (TextView) findViewById(R.id.textView_objective_phaseStartDate);
		TextView phaseEnd = (TextView) findViewById(R.id.textView_objective_phaseEndDate);
		EditText phaseDuration = (EditText) findViewById(R.id.editText_objective_phaseDuration);
		EditText coordinatesLatText = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
		EditText coordinatesLonText = (EditText) findViewById(R.id.editText_objective_coordinates_lon);

		Spinner regionsSpinner = (Spinner) findViewById(R.id.spinner_objective_region);
		Spinner statusSpinner = (Spinner) findViewById(R.id.spinner_objective_status);
		Spinner categorySpinner = (Spinner) findViewById(R.id.spinner_objective_category);

		Spinner executantSpinner = (Spinner) findViewById(R.id.spinner_objective_executant);

		// Handle objective type
		int objectiveType = -1;
		int objTypeRadio = objTypeGrp.getCheckedRadioButtonId();

		switch (objTypeRadio) {
		case R.id.radio_newConstruction:
			objectiveType = Objective.TYPE_NEW;
			break;

		case R.id.radio_renovation:
			objectiveType = Objective.TYPE_RENOVATION;
			break;

		default:
			TextView labelObjTpeGrp = (TextView) findViewById(R.id.label_objective_type);
			labelObjTpeGrp.setError(getString(R.string.error_field_empty));
			thereAreErrors = true;
			break;
		}

		// Handle beneficiary type
		final int beneficiaryType;
		int benefTypeRadio = benefTypeGrp.getCheckedRadioButtonId();

		switch (benefTypeRadio) {
		case R.id.radio_fizic:
			beneficiaryType = Beneficiary.TYPE_INDIVIDUAL;
			break;

		case R.id.radio_juridic:
			beneficiaryType = Beneficiary.TYPE_LEGAL;
			break;

		default:
			TextView labelBenefTpeGrp = (TextView) findViewById(R.id.label_objective_beneficiaryType);
			labelBenefTpeGrp.setError(getString(R.string.error_field_empty));
			beneficiaryType = -1;
			thereAreErrors = true;
			break;
		}

		String numeExecutant = editText_objective_execName.getText().toString().trim();
		String cuiExecutant = editText_objective_execCUI.getText().toString().trim();
		String nrcExecutant = editText_objective_execNrRc.getText().toString().trim();
		final String telefonBeneficiar = editText_objective_address_phone.getText().toString().trim();

		// Meserias String
		String numeMeserias = editText_objective_meserName.getText().toString().trim();
		String prenMeserias = editText_objective_meserSurname.getText().toString().trim();
		String telMeserias = editText_objective_meserTel.getText().toString().trim();
		// End Meserias String

		//Added error for incorrect completion of meserName, Author: Alin
		if (executantSpinner.getSelectedItem().toString().equals(EnumTipExecutant.TERT.getNume())
				&& editText_objective_execName.getText().toString().trim().equals("")) {
			editText_objective_execName.setError(getString(R.string.error_incorrect_completion));
			thereAreErrors = true;
		}
		else if(executantSpinner.getSelectedItem().toString().equals(EnumTipExecutant.REGIE_PROPRIE.getNume())
				&& editText_objective_meserName.getText().toString().trim().equals(""))
		{
			editText_objective_meserName.setError(getString(R.string.error_incorrect_completion));
			thereAreErrors = true;
		}
		//End Add
		
		// Handle name
		String name = "";
		if (nameText.getText().length() > 0) {
			name = nameText.getText().toString();
		} else {
			nameText.setError(getString(R.string.error_incorrect_completion));
			thereAreErrors = true;
		}

		/*
		 * Added a replace method in order to register a full float number
		 * inside the database Modified: 13.07.2016, Author: Alin
		 */
		float estimatedValue = 0;
		
		// Handle estimation value
		if (estValueText.getText().length() > 0) {
			estimatedValue = Float.parseFloat(estValueText.getText().toString().replace(",", "").replace("RON", ""));
		} else {
			estValueText.setError(getString(R.string.error_incorrect_completion));
			thereAreErrors = true;
		}

		// Handle estimation value
/*
		if (estValueText.getText().length() > 0) {
			estimatedValue = Float.parseFloat(estValueText.getText().toString());
		} else {
			estValueText.setError(getString(R.string.error_incorrect_completion));
			thereAreErrors = true;
		}
*/
		
		// Get date at time of creation
		Calendar creationCalendar = new GregorianCalendar();
		if (addDateText.getText().length() > 0)
			try {
				Date creationDate = sdf.parse(changeDateFormat(addDateText.getText().toString(),
						Constants.USER_DATE_FORMAT, Constants.DB_DATE_FORMAT));

				creationCalendar.setTime(creationDate);

			} catch (ParseException e) {
				e.printStackTrace();
				thereAreErrors = true;
			}
		else {
			TextView labelCreationDate = (TextView) findViewById(R.id.label_objective_addDate);
			labelCreationDate.setError(getString(R.string.error_field_empty));
			thereAreErrors = true;
		}

		// Handle authorization start date
		Calendar authorizationCalendar = new GregorianCalendar();
		if (authDateStart.getText().length() > 0) {
			try {
				Date authorizationDate = sdf.parse(changeDateFormat(authDateStart.getText().toString(),
						Constants.USER_DATE_FORMAT, Constants.DB_DATE_FORMAT));

				authorizationCalendar.setTime(authorizationDate);

			} catch (ParseException e) {
				e.printStackTrace();
				thereAreErrors = true;
			}
		} else {
			// TextView labelAuthDateStart = (TextView)
			// findViewById(R.id.label_objective_authStartDate);
			authDateStart.setError(getString(R.string.error_field_empty));
			thereAreErrors = true;
		}

		// Handle authorization end date
		Calendar authorizationEndCalendar = new GregorianCalendar();
		if (authDateEnd.getText().length() > 0) {
			try {
				Date authorizationEndDate = sdf.parse(changeDateFormat(authDateEnd.getText().toString(),
						Constants.USER_DATE_FORMAT, Constants.DB_DATE_FORMAT));

				authorizationEndCalendar.setTime(authorizationEndDate);

			} catch (ParseException e) {
				e.printStackTrace();
				thereAreErrors = true;
			}
		} else {
			// TextView labelAuthDateEnd = (TextView)
			// findViewById(R.id.label_objective_authEndDate);
			authDateEnd.setError(getString(R.string.error_field_empty));
			thereAreErrors = true;
		}

		// Handle address
		String address = generateAddressString(addressCity.getText().toString(), addressStreet.getText().toString(),
				addressNumber.getText().toString());

		// Handle stage
		int stageId = ((Stage) stageSpinner.getSelectedItem()).getId();

		// Handle phase
		int phaseId = ((Phase) phaseSpinner.getSelectedItem()).getId();

		// Handle phase start
		Calendar phaseStartCalendarEdited = new GregorianCalendar();
		if (phaseStart.getText().length() > 0) {
			try {
				Date phaseStartDate = sdf.parse(changeDateFormat(phaseStart.getText().toString(),
						Constants.USER_DATE_FORMAT, Constants.DB_DATE_FORMAT));

				phaseStartCalendarEdited.setTime(phaseStartDate);

			} catch (ParseException e) {
				e.printStackTrace();
				thereAreErrors = true;
			}
		} else {
			TextView labelPhaseStart = (TextView) findViewById(R.id.label_objective_phaseStartDate);
			labelPhaseStart.setError(getString(R.string.error_field_empty));
			thereAreErrors = true;
		}

		Log.d("DBG", "End Date: " + phaseStartCalendarEdited.toString());

		// Handle phase end
		Calendar phaseEndCalendarEdited = new GregorianCalendar();
		if (phaseEnd.getText().length() > 0) {
			try {
				Date phaseEndDate = sdf.parse(changeDateFormat(phaseEnd.getText().toString(),
						Constants.USER_DATE_FORMAT, Constants.DB_DATE_FORMAT));

				phaseEndCalendarEdited.setTime(phaseEndDate);

			} catch (ParseException e) {
				e.printStackTrace();
				thereAreErrors = true;
			}
		} else {
			TextView labelPhaseEnd = (TextView) findViewById(R.id.label_objective_phaseEndDate);
			labelPhaseEnd.setError(getString(R.string.error_field_empty));
			thereAreErrors = true;
		}

		// Get duration in days
		int days = Integer.parseInt(phaseDuration.getText().toString());

		// Get coordinates
		String coordinates = "";

		if (coordinatesLatText.getText().length() > 1) {
			coordinates = coordinatesLatText.getText().toString() + "," + coordinatesLonText.getText().toString();
		} else {

			String myAddress = regionsSpinner.getSelectedItem().toString() + ","
					+ generateAddressStringEmpty(addressCity.getText().toString(), addressStreet.getText().toString(),
							addressNumber.getText().toString());

			LatLng localCoords = getCoordinatesFromAddress(myAddress);

			if (localCoords == null) {
				Toast.makeText(getApplicationContext(), "Adresa incorecta, verificati datele.", Toast.LENGTH_LONG)
						.show();
				return null;
			} else
				coordinates = String.valueOf(localCoords.latitude) + "," + String.valueOf(localCoords.longitude);

		}

		// Get county id and cva code
		String cvaCode = user.getCode();
		String cvaBranch = user.getBranchCode();

		final int regionId = Integer.valueOf(EnumJudete.getCodJudet(regionsSpinner.getSelectedItem().toString()));

		final int objectiveStatusId = EnumStadiuObiectiv.getCodStadiu(statusSpinner.getSelectedItem().toString());

		final int objectivCategoryId = EnumCategorieObiectiv
				.getCodeCategory(categorySpinner.getSelectedItem().toString());

		// Handle beneficiary adition.
		HashMap<String, String> whereConditions = new HashMap<String, String>();
		final String beneficiaryNameString;

		if (beneficiaryText.getText().length() > 0) {
			beneficiaryNameString = beneficiaryText.getText().toString();
			whereConditions.put(SQLiteHelper.NAME, beneficiaryNameString);
		} else {
			beneficiaryNameString = "";
			beneficiaryText.setError(getString(R.string.error_incorrect_completion));
			thereAreErrors = true;
		}

		final int[] beneficiaryId = new int[1];
		final boolean[] benefErrors = new boolean[1];
		benefErrors[0] = false;

		if (beneficiaryType > -1 && !thereAreErrors) {
			whereConditions.put(SQLiteHelper.TYPE, Integer.toString(beneficiaryType));

			if (beneficiaryType == Beneficiary.TYPE_LEGAL) {
				EditText cuiText = (EditText) findViewById(R.id.editText_objective_benefCUI);
				EditText nrRcText = (EditText) findViewById(R.id.editText_objective_benefNrRc);

				if (cuiText.getText().length() > 0) {
					whereConditions.put(SQLiteHelper.CUI, cuiText.getText().toString());
				}

				if (nrRcText.getText().length() > 0) {
					whereConditions.put(SQLiteHelper.NR_RC, nrRcText.getText().toString());
				}

			}

			final BeneficiaryData beneficiaryData = new BeneficiaryData(this);
			int foundBenefId = beneficiaryData.beneficiaryExists(whereConditions);

			if (foundBenefId > -1) {
				beneficiaryId[0] = foundBenefId;

			} else {

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							boolean benefAddErrors = false;

							if (beneficiaryType == Beneficiary.TYPE_INDIVIDUAL) {
								Beneficiary beneficiaryToAdd = new Beneficiary(regionId, beneficiaryNameString,
										beneficiaryType);

								beneficiaryData.addBeneficiary(beneficiaryToAdd);
							} else if (beneficiaryType == Beneficiary.TYPE_LEGAL) {
								EditText cuiText = (EditText) findViewById(R.id.editText_objective_benefCUI);
								EditText nrRcText = (EditText) findViewById(R.id.editText_objective_benefNrRc);

								String cuiTextString = "";
								String nrRcTextString = "";

								if (cuiText.getText().length() > 0) {
									cuiTextString = cuiText.getText().toString();
								} else {
									cuiText.setError(
											getApplicationContext().getString(R.string.error_incorrect_completion));
									benefAddErrors = true;
								}
								if (nrRcText.getText().length() > 0) {
									nrRcTextString = nrRcText.getText().toString();
								} else {
									nrRcText.setError(
											getApplicationContext().getString(R.string.error_incorrect_completion));
									benefAddErrors = true;
								}

								if (!benefAddErrors) {
									Beneficiary beneficiaryToAdd = new Beneficiary(regionId, beneficiaryNameString,
											beneficiaryType, cuiTextString, nrRcTextString);

									Beneficiary addedBeneficiary = beneficiaryData.addBeneficiary(beneficiaryToAdd);
									if (addedBeneficiary != null)
										beneficiaryId[0] = addedBeneficiary.getId();
									benefErrors[0] = false;
									createObjectiveFromForm();
								} else {
									benefErrors[0] = true;
									Toast.makeText(getApplicationContext(),
											getApplicationContext().getString(R.string.error_beneficiary_not_added),
											Toast.LENGTH_SHORT).show();
								}
							}
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							benefErrors[0] = true;
							break;
						}
					}
				};

				benefErrors[0] = true;

				AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
				builder.setTitle(getString(R.string.beneficiary_add_title));
				builder.setMessage(getString(R.string.beneficiary_add_message));
				builder.setPositiveButton(getString(R.string.beneficiary_add_button_positive), dialogClickListener);
				builder.setNegativeButton(getString(R.string.beneficiary_add_button_negative), dialogClickListener);
				builder.show();
			}
		}
		if (!thereAreErrors && !benefErrors[0]) {
			List<Object> resultList = new ArrayList<Object>();
			StringBuilder sBuilder = new StringBuilder();
			String NEW_LINE = System.getProperty("line.separator");
			sBuilder.append("Phase details:" + NEW_LINE);
			sBuilder.append("Duration = " + days + " days" + NEW_LINE);
			sBuilder.append("Phase start date = " + sdf.format(phaseStartCalendarEdited.getTime()) + NEW_LINE);

			switch (purpose) {
			case ADD:

				objective = new Objective(objectiveType, cvaCode, regionId, name, creationCalendar, beneficiaryId[0],
						beneficiaryType, authorizationCalendar, authorizationEndCalendar, estimatedValue, address, -1,
						coordinates, stageId, phaseId, phaseEndCalendarEdited, 1);

				objective.setPhaseValues(sBuilder.toString());

				objective.setStatusId(objectiveStatusId);
				objective.setCategoryId(objectivCategoryId);

				objective.setNumeExecutant(numeExecutant);
				objective.setCuiExecutant(cuiExecutant);
				objective.setNrcExecutant(nrcExecutant);

				// Meserias fields, Author: Alin
				objective.setNumeMeserias(numeMeserias);
				objective.setPrenMeserias(prenMeserias);
				objective.setTelMeserias(telMeserias);
				// End Meserias fields

				objective.setTelBenef(telefonBeneficiar);

				objective.setFiliala(UserInfo.getInstance().getUnitLog());

				resultList.add(objective);
				resultList.add(days);
				resultList.add(new Pair<Calendar, Calendar>(phaseStartCalendarEdited, phaseEndCalendarEdited));

				return resultList;

			case EDIT:
				objective = new Objective(objectiveToEdit.getId(), objectiveType, cvaCode, regionId, name,
						creationCalendar, beneficiaryId[0], beneficiaryType, authorizationCalendar,
						authorizationEndCalendar, estimatedValue, address, -1, coordinates, stageId, phaseId,
						phaseStartCalendarEdited, 1);

				objective.setPhaseValues(sBuilder.toString());

				objective.setStatusId(objectiveStatusId);
				objective.setCategoryId(objectivCategoryId);

				objective.setNumeExecutant(numeExecutant);
				objective.setCuiExecutant(cuiExecutant);
				objective.setNrcExecutant(nrcExecutant);

				// Meserias fields, Author: Alin
				objective.setNumeMeserias(numeMeserias);
				objective.setPrenMeserias(prenMeserias);
				objective.setTelMeserias(telMeserias);
				// End Meserias fields

				objective.setTelBenef(telefonBeneficiar);

				objective.setFiliala(UserInfo.getInstance().getUnitLog());

				resultList.add(objective);
				resultList.add(days);
				resultList.add(new Pair<Calendar, Calendar>(phaseStartCalendarEdited, phaseEndCalendarEdited));

				return resultList;

			default:
				break;
			}
		} else
			Toast.makeText(this, getString(R.string.error_objective_definitions), Toast.LENGTH_SHORT).show();
		return null;
	}

	private ArrayList<Phase> populatePhasesSpinner(Spinner spinner, int selectedStage) {
		PhaseData phaseData = new PhaseData(this);

		ArrayList<Phase> phases = phaseData.getAllPhasesForStage(selectedStage);

		PhaseSpinnerAdapter adapter = new PhaseSpinnerAdapter(this, android.R.layout.simple_spinner_item, phases);

		spinner.setAdapter(adapter);

		return phases;
	}

	private void populateSpinnerExecutant(Spinner spinner, Objective objective) {
		List<String> listExec = EnumTipExecutant.getTipExecNames();
		String[] arrayExec = listExec.toArray(new String[listExec.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
				arrayExec);
		spinner.setAdapter(dataAdapter);

		// Fixed a bug where the Executant will change, based on the Category Id
		// and not the Executant one
		if (null != objective) {
			int position = 0;
			for (String category : listExec) {
				if (String.valueOf(EnumTipExecutant.getNumeTip(position)) == objective.getNumeExecutant()) { // Modified:
																												// 07.07.2016,
																												// Author:
																												// Alin
					spinner.setSelection(position);
					break;
				}

				position++;
			}

		}

	}

	private void populateSpinnerCategory(Spinner spinner, Objective objective) {
		List<String> listCategory = EnumCategorieObiectiv.getCategoriesNames();
		String[] arrayCategory = listCategory.toArray(new String[listCategory.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
				arrayCategory);
		spinner.setAdapter(dataAdapter);

		if (null != objective) {
			int position = 0;
			for (String category : listCategory) {
				if (Integer.valueOf(EnumCategorieObiectiv.getCodeCategory(category)) == objective.getCategoryId()) {
					spinner.setSelection(position);
					break;
				}

				position++;
			}

		}

	}

	private void populateSpinnerStatus(Spinner spinner, Objective objective) {
		List<String> listStatus = EnumStadiuObiectiv.getStatusNames();
		String[] arrayStatus = listStatus.toArray(new String[listStatus.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
				arrayStatus);
		spinner.setAdapter(dataAdapter);

		if (null != objective) {
			int position = 0;
			for (String status : listStatus) {
				if (Integer.valueOf(EnumStadiuObiectiv.getCodStadiu(status)) == objective.getStatusId()) {
					spinner.setSelection(position);
					break;
				}

				position++;
			}

		}

	}

	private void populateSpinnerRegions(Spinner spinner, Objective objective) {

		List<String> listRegions = EnumJudete.getRegionNames();
		String[] arrayJud = listRegions.toArray(new String[listRegions.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
				arrayJud);
		spinner.setAdapter(dataAdapter);

		if (null != objective) {
			int position = 0;
			for (String regions : listRegions) {
				if (Integer.valueOf(EnumJudete.getCodJudet(regions)) == objective.getRegionID()) {
					spinner.setSelection(position);
					break;
				}

				position++;
			}

		}

	}

	private void addNewSpinners(Objective objective) {
		final Spinner regionsSpinner = (Spinner) findViewById(R.id.spinner_objective_region);
		populateSpinnerRegions(regionsSpinner, objective);

		final Spinner statusSpinner = (Spinner) findViewById(R.id.spinner_objective_status);
		populateSpinnerStatus(statusSpinner, objective);

		final Spinner categorySpinner = (Spinner) findViewById(R.id.spinner_objective_category);
		populateSpinnerCategory(categorySpinner, objective);

		final Spinner executantSpinner = (Spinner) findViewById(R.id.spinner_objective_executant);
		populateSpinnerExecutant(executantSpinner, objective);
		setListenerSpinnerExecutant(executantSpinner, objective);

		if (objective != null && objective.getNumeExecutant().trim().length() > 0)
			executantSpinner.setSelection(EnumTipExecutant.TERT.getCod());

	}

	private void setListenerSpinnerExecutant(Spinner spinner, final Objective objective) {

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getAdapter().getItem(position).toString().equals(EnumTipExecutant.TERT.getNume())) {
					setExecutantDetailsVisibility(true, objective);
					setMeseriasDetailsVisibility(false, null); // Author: Alin
				} else {
					setExecutantDetailsVisibility(false, null);
					setMeseriasDetailsVisibility(true, objective); // Author:
																	// Alin
				}

			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void setExecutantDetailsVisibility(boolean isVisible, Objective objective) {
		if (isVisible) {

			layout_executantDetailsName.setVisibility(View.VISIBLE);
			layout_ExecutantDetailsCui.setVisibility(View.VISIBLE);
			layout_executantDetailsNrRc.setVisibility(View.VISIBLE);

			if (objective != null) {
				editText_objective_execName.setText(objective.getNumeExecutant().trim());
				editText_objective_execCUI.setText(objective.getCuiExecutant().trim());
				editText_objective_execNrRc.setText(objective.getNrcExecutant().trim());
			}
		} else {
			layout_executantDetailsName.setVisibility(View.GONE);
			layout_ExecutantDetailsCui.setVisibility(View.GONE);
			layout_executantDetailsNrRc.setVisibility(View.GONE);
			editText_objective_execName.setText("");
			editText_objective_execCUI.setText("");
			editText_objective_execNrRc.setText("");
		}
	}

	// Created a function for meserias fields where
	// if "Regie Proprie" is chosen, fields become visible, otherwise,
	// they become invisible, Author: Alin
	private void setMeseriasDetailsVisibility(boolean isVisible, Objective objective) {
		if (isVisible) {
			// Meserias fields
			layout_meseriasName.setVisibility(View.VISIBLE);
			layout_meseriasSurName.setVisibility(View.VISIBLE);
			layout_meseriasNumarTel.setVisibility(View.VISIBLE);

			if (objective != null) {
				editText_objective_meserName.setText(objective.getNumeMeserias().trim());
				editText_objective_meserSurname.setText(objective.getPrenMeserias().trim());
				editText_objective_meserTel.setText(objective.getTelMeserias().trim());

			}
		}

		else

		{
			// Meserias fields
			layout_meseriasName.setVisibility(View.GONE);
			layout_meseriasSurName.setVisibility(View.GONE);
			layout_meseriasNumarTel.setVisibility(View.GONE);
			editText_objective_meserName.setText("");
			editText_objective_meserSurname.setText("");
			editText_objective_meserTel.setText("");

		}
	}

	/**
	 * Initializes the UI elements for add mode.
	 */
	private void setupAddUi() {
		this.setTitle(R.string.add_edit_title_add);

		final Spinner stageSpinner = (Spinner) findViewById(R.id.spinner_stage);
		final Spinner phaseSpinner = (Spinner) findViewById(R.id.spinner_phase);

		addNewSpinners(null);

		final StagePhaseSpinnerUtils spsUtils = new StagePhaseSpinnerUtils(this);

		spsUtils.populateAvailableStagesSpinner(stageSpinner, -1, 0, stageSpinner.getSelectedItemPosition());
		lastSelectedStageId = ((Stage) stageSpinner.getSelectedItem()).getId();

		stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				int selectedStageId = ((Stage) adapterView.getSelectedItem()).getId();
				Log.d("DBG", "Selected stage Id: " + selectedStageId);
				int selectedPhasePosition = phaseSpinner.getSelectedItemPosition();

				if (selectedStageId != lastSelectedStageId || objectiveTypeChanged) {
					spsUtils.populateAvailablePhasesSpinner(phaseSpinner, -1, selectedStageId);

					if (selectedStageId == lastSelectedStageId) {
						phaseSpinner.setSelection(selectedPhasePosition);
					}

					lastSelectedStageId = selectedStageId;
					objectiveTypeChanged = false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		/*
		 * Added the TextWatcher in the saveUI in order to separate number
		 * values with commas when adding a new object Added: 13.07.2016,
		 * Author: Alin
		 */
		EditText editTextEstValue = (EditText) findViewById(R.id.editText_objective_estValue);
		editTextEstValue.addTextChangedListener(new NumberTextWatcher(editTextEstValue));

		int selectedStageId = ((Stage) stageSpinner.getSelectedItem()).getId();
		spsUtils.populateAvailablePhasesSpinner(phaseSpinner, -1, selectedStageId);

		TextView textViewPhaseStart = (TextView) findViewById(R.id.textView_objective_phaseStartDate);
		TextView textViewPhaseEnd = (TextView) findViewById(R.id.textView_objective_phaseEndDate);

		RadioGroup beneficiaryRadios = (RadioGroup) findViewById(R.id.radioGroup_beneficiaryType);
		int selectedRadioID = beneficiaryRadios.getCheckedRadioButtonId();

		final LinearLayout juridicDetailsCui = (LinearLayout) findViewById(R.id.layout_juridicaDetailsCui);
		final LinearLayout juridicDetailsNrRc = (LinearLayout) findViewById(R.id.layout_juridicaDetailsNrRc);

		switch (selectedRadioID) {
		case R.id.radio_fizic:
			juridicDetailsCui.setVisibility(View.GONE);
			juridicDetailsNrRc.setVisibility(View.GONE);
			break;

		case R.id.radio_juridic:
			juridicDetailsCui.setVisibility(View.VISIBLE);
			juridicDetailsNrRc.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}

		final AutoCompleteTextView acTextViewBenef = (AutoCompleteTextView) findViewById(
				R.id.acText_objective_beneficiary);
		final TextView labelBenefTypeGrp = (TextView) findViewById(R.id.label_objective_beneficiaryType);

		beneficiaryRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio_fizic:
					juridicDetailsCui.setVisibility(View.GONE);
					juridicDetailsNrRc.setVisibility(View.GONE);
					acTextViewBenef.setNextFocusDownId(R.id.editText_objective_address_city);
					acTextViewBenef.setNextFocusRightId(R.id.editText_objective_address_city);
					labelBenefTypeGrp.setError(null);
					break;

				case R.id.radio_juridic:
					juridicDetailsCui.setVisibility(View.VISIBLE);
					juridicDetailsNrRc.setVisibility(View.VISIBLE);
					acTextViewBenef.setNextFocusDownId(R.id.editText_objective_benefCUI);
					acTextViewBenef.setNextFocusRightId(R.id.editText_objective_benefCUI);
					labelBenefTypeGrp.setError(null);
					break;

				default:
					break;
				}
			}
		});

		final TextView labelObjTpeGrp = (TextView) findViewById(R.id.label_objective_type);

		RadioGroup grpObjType = (RadioGroup) findViewById(R.id.radioGroup_objectiveType);

		grpObjType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				int initialPosition = stageSpinner.getSelectedItemPosition();
				switch (checkedId) {
				case R.id.radio_newConstruction:
					labelObjTpeGrp.setError(null);
					if (initialPosition > 0 && objectiveTypeRadioChecked) {
						initialPosition++;
					}
					int newPosition = spsUtils.populateAvailableStagesSpinner(stageSpinner, -1, 0,
							initialPosition).second;
					stageSpinner.setSelection(newPosition);
					objectiveTypeRadioChecked = true;
					objectiveTypeChanged = true;
					break;

				case R.id.radio_renovation:
					labelObjTpeGrp.setError(null);
					if (initialPosition >= 0 && objectiveTypeRadioChecked) {
						initialPosition--;
					}
					int newPositionReno = spsUtils.populateAvailableStagesSpinner(stageSpinner, -1, 1,
							initialPosition).second;
					stageSpinner.setSelection(newPositionReno);
					objectiveTypeRadioChecked = true;
					objectiveTypeChanged = true;
					break;

				default:
					break;
				}
			}
		});

		LinearLayout addDate = (LinearLayout) findViewById(R.id.layout_addDate);

		TextView authDateStart = (TextView) findViewById(R.id.textView_objective_authStartDate);
		TextView authDateEnd = (TextView) findViewById(R.id.textView_objective_authEndDate);

		LinearLayout phaseStart = (LinearLayout) findViewById(R.id.layout_phaseStartDate);
		LinearLayout phaseEnd = (LinearLayout) findViewById(R.id.layout_phaseEndDate);

		TextView textDataAddOb = (TextView) findViewById(R.id.textView_objective_addDate);
		textDataAddOb.setText(Utils.getCurrentDate());

		addDateClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_START, null, Calendar.getInstance(), R.id.textView_objective_addDate,
				R.id.textView_objective_addDate);
		addDate.setOnClickListener(addDateClickListener);

		authDateSClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_START, null, Calendar.getInstance(), R.id.textView_objective_authStartDate,
				R.id.textView_objective_authEndDate);
		authDateStart.setOnClickListener(authDateSClickListener);

		authDateEClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_END, null, Calendar.getInstance(), R.id.textView_objective_authEndDate,
				R.id.textView_objective_authStartDate);
		authDateEnd.setOnClickListener(authDateEClickListener);

		phaseStartClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_START, null, Calendar.getInstance(), R.id.textView_objective_phaseStartDate,
				R.id.textView_objective_phaseEndDate);
		phaseStart.setOnClickListener(phaseStartClickListener);

		phaseEndClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_END, null, Calendar.getInstance(), R.id.textView_objective_phaseEndDate,
				R.id.textView_objective_phaseStartDate);
		phaseEnd.setOnClickListener(phaseEndClickListener);

		final EditText phaseDuration = (EditText) findViewById(R.id.editText_objective_phaseDuration);
		phaseDuration.addTextChangedListener(
				new PhaseDurationChangeWatcher(this, phaseEndClickListener, textViewPhaseStart, textViewPhaseEnd));

		phaseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				int phaseDefaultDuration = ((Phase) phaseSpinner.getSelectedItem()).getDays();
				phaseDuration.setText(Integer.toString(phaseDefaultDuration));
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		// Setup beneficiary text
		acTextViewBenef.setThreshold(3);

		ArrayAdapter<Pair<Integer, String>> autocompleteAdapter;

		autocompleteAdapter = new AutoCompleteBenefAdapter(this, android.R.layout.simple_dropdown_item_1line,
				new ArrayList<Pair<Integer, String>>());

		acTextViewBenef.setAdapter(autocompleteAdapter);

		acTextViewBenef.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				BeneficiaryData benefData = new BeneficiaryData(getApplicationContext());
				ArrayList<Pair<Integer, String>> suggestions = new ArrayList<Pair<Integer, String>>();

				RadioGroup beneficiaryRadios = (RadioGroup) findViewById(R.id.radioGroup_beneficiaryType);
				int selectedRadioID = beneficiaryRadios.getCheckedRadioButtonId();
				int benefType;

				switch (selectedRadioID) {
				case R.id.radio_fizic:
					benefType = Beneficiary.TYPE_INDIVIDUAL;
					break;

				case R.id.radio_juridic:
					benefType = Beneficiary.TYPE_LEGAL;
					break;

				default:
					benefType = -1;
					break;
				}

				if (acTextViewBenef.isPerformingCompletion()) {
					// An item has been selected from the list.
					return;
				}

				suggestions.clear();

				suggestions = benefData.getBeneficiaryAC(s.toString(), benefType);

				AutoCompleteBenefAdapter adapter = new AutoCompleteBenefAdapter(AddEditObjective.this,
						android.R.layout.simple_dropdown_item_1line, suggestions);

				acTextViewBenef.setAdapter(adapter);

				adapter.notifyDataSetChanged();
			}
		});

		acTextViewBenef.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("DBG", "Am dat click pe ceva din lista");

				BeneficiaryData beneficiaryData = new BeneficiaryData(getApplicationContext());
				Pair<Integer, String> selection = (Pair<Integer, String>) parent.getItemAtPosition(position);

				Beneficiary beneficiary = beneficiaryData.getBeneficiaryById(selection.first);

				acTextViewBenef.setText(beneficiary.getName());
				if (beneficiary.getType() == 1) {
					EditText cuiText = (EditText) findViewById(R.id.editText_objective_benefCUI);
					EditText nrRcText = (EditText) findViewById(R.id.editText_objective_benefNrRc);

					RadioButton legalPerson = (RadioButton) findViewById(R.id.radio_juridic);
					legalPerson.setChecked(true);

					cuiText.setText(beneficiary.getCui());
					nrRcText.setText(beneficiary.getNrRc());

				} else {
					RadioButton individualPerson = (RadioButton) findViewById(R.id.radio_fizic);
					individualPerson.setChecked(true);
				}

			}
		});

		// Setup Address fields
		final EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
		final EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
		final EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);

		editText_objective_execName = (EditText) findViewById(R.id.editText_objective_execName);
		editText_objective_execCUI = (EditText) findViewById(R.id.editText_objective_executantCUI);
		editText_objective_execNrRc = (EditText) findViewById(R.id.editText_objective_executantNrRc);

		// Meserias Fields
		editText_objective_meserName = (EditText) findViewById(R.id.editText_objective_meserName);
		editText_objective_meserSurname = (EditText) findViewById(R.id.editText_objective_meserSurname);
		editText_objective_meserTel = (EditText) findViewById(R.id.editText_objective_meserTel);
		// Meserias Fields

		editText_objective_address_phone = (EditText) findViewById(R.id.editText_objective_address_phone);

		// Setup compute Button
		Button buttonComputeCoords = (Button) findViewById(R.id.button_computeCoordinates);
		buttonComputeCoords.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String addressString = generateAddressString(addressCity.getText().toString(),
						addressStreet.getText().toString(), addressNumber.getText().toString());

				Setup setup = new Setup(AddEditObjective.this);
				setup.hideKeyboard();

				populateCoordinateFields(addressString);

			}
		});

		// Setup my location button
		Button myLocation = (Button) findViewById(R.id.button_getCurrentGpsLocation);
		myLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				LatLng coordinates = getCurrentGpsLocation();

				if (coordinates != null) {

					if (coordinates.latitude == 0) {
						Toast.makeText(getApplicationContext(),
								"Coordonate eronate, repetati operatiunea dupa 30 de secunde.", Toast.LENGTH_LONG)
								.show();
						return;
					}

					EditText coordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
					EditText coordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);
					coordinatesLat.setText(Double.toString(coordinates.latitude));
					coordinatesLon.setText(Double.toString(coordinates.longitude));

					populateGeoFields(coordinates);
					areGpsCoordsAutom = true;

				}
			}
		});

		Intent i = getIntent();

		if (i.getExtras() != null) {
			if (i.getExtras().containsKey(Constants.KEY_COORDINATES)) {
				EditText coordinatesLatText = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
				EditText coordinatesLonText = (EditText) findViewById(R.id.editText_objective_coordinates_lon);
				String coordinates = i.getStringExtra(Constants.KEY_COORDINATES);

				String[] latlong = coordinates.split(",");

				coordinatesLatText.setText(latlong[0]);
				coordinatesLonText.setText(latlong[1]);

				double latitude = Double.parseDouble(latlong[0]);
				double longitude = Double.parseDouble(latlong[1]);

				LatLng latLng = new LatLng(latitude, longitude);

				populateGeoFields(latLng);

				map.addMarker(new MarkerOptions().position(latLng)
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 21); // Zoom
																						// to
																						// new
																						// marker
																						// location
				map.animateCamera(update);

			} else {
				// Move map to CVA's Region
				RegionData regionData = new RegionData(this);
				final int regionId = regionData.getCVARegionId(user.getBranchCode());

				Region region = regionData.getRegionById(regionId);

				// Put a marker where the CVA'S Region is
				String[] latlong = region.getGps().split(",");
				double latitude = Double.parseDouble(latlong[0]);
				double longitude = Double.parseDouble(latlong[1]);

				LatLng latLng = new LatLng(latitude, longitude);

				map.clear();
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, MapActivity.ZOOM_COUNTY); // Zoom
																											// to
																											// new
																											// marker
																											// location
				map.animateCamera(update);
			}
		}
	}

	/**
	 * Initializes the UI elements for edit mode and populates them
	 *
	 * @param objective
	 *            The objective info with which the UI will be populated
	 */
	private void setupEditUi(Objective objective) {
		switch (objective.getStatus()) {
		case Objective.INACTIVE:
			this.setTitle(R.string.add_edit_title_view);
			break;
		case Objective.ACTIVE:
			if (user.getUserType() == User.TYPE_CVA)
				this.setTitle(R.string.add_edit_title_edit);
			else
				this.setTitle("Informatii obiectiv " + " " + objective.getFiliala());
			break;
		}

		addNewSpinners(objective);

		// Get all necesary details.
		final ObjectiveData objectiveData = new ObjectiveData(this);
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);

		// Get the beneficiary of the objective
		BeneficiaryData beneficiaryData = new BeneficiaryData(this);
		Beneficiary beneficiary = beneficiaryData.getBeneficiaryById(objective.getBeneficiaryId());

		// Get the current phaseduration
		int currentPhaseDuration = objectiveData.getCurrentObjectivePhaseDuration(objective.getId(),
				objective.getPhaseId());

		// Get the start and end date of the current phase
		Pair<Calendar, Calendar> phaseInterval = objectiveData.getCurrentObjectivePhaseDetails(objective.getId(),
				objective.getPhaseId());

		Calendar phaseStartCalendar = phaseInterval.first;
		Calendar phaseEndCalendar = phaseInterval.second;

		// Set the objective data into the UI elements

		// Setup the created Date field
		TextView textViewAddedDate = (TextView) findViewById(R.id.textView_objective_addDate);
		textViewAddedDate.setText(sdf.format(objective.getCreationDate().getTime()));

		LinearLayout addDate = (LinearLayout) findViewById(R.id.layout_addDate);
		addDateClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_START, null, objective.getCreationDate(), R.id.textView_objective_addDate,
				-1);

		addDate.setOnClickListener(addDateClickListener);

		// Setup the Name field:
		EditText editTextName = (EditText) findViewById(R.id.editText_objective_name);
		editTextName.setText(objective.getName());

		// Setup the Estimated value field:
		/*
		 * Modified TextFormat in order to put commas and decimals to the price
		 * value Added: 13.07.2016, Author: Alin
		 */
		EditText editTextEstValue = (EditText) findViewById(R.id.editText_objective_estValue);
		
		String textformat = Float.toString(objective.getEstimationValue());
		DecimalFormat formatter = new DecimalFormat("#,###.##" + " " + "RON",
				DecimalFormatSymbols.getInstance(Locale.US));
		Double amount = Double.parseDouble(textformat);
		editTextEstValue.setText(formatter.format(amount));

		editTextEstValue.addTextChangedListener(new NumberTextWatcher(editTextEstValue));

		// Set the type picker and it's listener:
		RadioGroup grpObjType = (RadioGroup) findViewById(R.id.radioGroup_objectiveType);

		switch (objective.getTypeId()) {
		case Objective.TYPE_NEW:
			RadioButton radioNew = (RadioButton) findViewById(R.id.radio_newConstruction);
			radioNew.setChecked(true);
			break;

		case Objective.TYPE_RENOVATION:
			RadioButton radioReno = (RadioButton) findViewById(R.id.radio_renovation);
			radioReno.setChecked(true);
			break;

		default:
			break;
		}

		// Setup beneficiary name
		final AutoCompleteTextView acvBeneficiaryName = (AutoCompleteTextView) findViewById(
				R.id.acText_objective_beneficiary);
		acvBeneficiaryName.setText(beneficiary.getName());

		// Setup autocomplete name behaviour
		acvBeneficiaryName.setThreshold(3);

		ArrayAdapter<Pair<Integer, String>> autocompleteAdapter;

		autocompleteAdapter = new AutoCompleteBenefAdapter(this, android.R.layout.simple_dropdown_item_1line,
				new ArrayList<Pair<Integer, String>>());

		acvBeneficiaryName.setAdapter(autocompleteAdapter);

		acvBeneficiaryName.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				BeneficiaryData benefData = new BeneficiaryData(getApplicationContext());
				ArrayList<Pair<Integer, String>> suggestions = new ArrayList<Pair<Integer, String>>();

				RadioGroup beneficiaryRadios = (RadioGroup) findViewById(R.id.radioGroup_beneficiaryType);
				int selectedRadioID = beneficiaryRadios.getCheckedRadioButtonId();
				int benefType;

				switch (selectedRadioID) {
				case R.id.radio_fizic:
					benefType = 0;
					break;

				case R.id.radio_juridic:
					benefType = 1;
					break;

				default:
					benefType = -1;
					break;
				}

				if (acvBeneficiaryName.isPerformingCompletion()) {
					// An item has been selected from the list.
					return;
				}

				suggestions.clear();

				suggestions = benefData.getBeneficiaryAC(s.toString(), benefType);

				AutoCompleteBenefAdapter adapter = new AutoCompleteBenefAdapter(AddEditObjective.this,
						android.R.layout.simple_dropdown_item_1line, suggestions);

				acvBeneficiaryName.setAdapter(adapter);

				adapter.notifyDataSetChanged();
			}
		});

		acvBeneficiaryName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("DBG", "Am dat click pe ceva din lista");

				BeneficiaryData beneficiaryData = new BeneficiaryData(getApplicationContext());
				Pair<Integer, String> selection = (Pair<Integer, String>) parent.getItemAtPosition(position);

				Beneficiary beneficiary = beneficiaryData.getBeneficiaryById(selection.first);

				acvBeneficiaryName.setText(beneficiary.getName());

				if (beneficiary.getType() == 1) {
					EditText cuiText = (EditText) findViewById(R.id.editText_objective_benefCUI);
					EditText nrRcText = (EditText) findViewById(R.id.editText_objective_benefNrRc);

					RadioButton legalPerson = (RadioButton) findViewById(R.id.radio_juridic);
					legalPerson.setChecked(true);

					cuiText.setText(beneficiary.getCui());
					nrRcText.setText(beneficiary.getNrRc());

				} else {
					RadioButton individualPerson = (RadioButton) findViewById(R.id.radio_fizic);
					individualPerson.setChecked(true);
				}

			}
		});

		// Setup beneficiary type radios
		RadioGroup beneficiaryRadios = (RadioGroup) findViewById(R.id.radioGroup_beneficiaryType);

		final LinearLayout juridicDetailsCui = (LinearLayout) findViewById(R.id.layout_juridicaDetailsCui);
		final LinearLayout juridicDetailsNrRc = (LinearLayout) findViewById(R.id.layout_juridicaDetailsNrRc);

		switch (objective.getBeneficiaryType()) {
		case Beneficiary.TYPE_INDIVIDUAL:
			juridicDetailsCui.setVisibility(View.GONE);
			juridicDetailsNrRc.setVisibility(View.GONE);

			RadioButton radioIndividual = (RadioButton) findViewById(R.id.radio_fizic);
			radioIndividual.setChecked(true);

			break;

		case Beneficiary.TYPE_LEGAL:
			juridicDetailsCui.setVisibility(View.VISIBLE);
			juridicDetailsNrRc.setVisibility(View.VISIBLE);

			BeneficiaryData benefData = new BeneficiaryData(this);
			Beneficiary currentBeneficiary = benefData.getBeneficiaryById(objective.getBeneficiaryId());

			EditText cui = (EditText) findViewById(R.id.editText_objective_benefCUI);
			EditText nrRc = (EditText) findViewById(R.id.editText_objective_benefNrRc);

			cui.setText(currentBeneficiary.getCui());
			nrRc.setText(currentBeneficiary.getNrRc());

			RadioButton radioLegal = (RadioButton) findViewById(R.id.radio_juridic);
			radioLegal.setChecked(true);
			break;

		default:
			break;
		}

		final TextView labelBenefTypeGrp = (TextView) findViewById(R.id.label_objective_beneficiaryType);

		beneficiaryRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio_fizic:
					juridicDetailsCui.setVisibility(View.GONE);
					juridicDetailsNrRc.setVisibility(View.GONE);
					labelBenefTypeGrp.setError(null);
					break;

				case R.id.radio_juridic:
					juridicDetailsCui.setVisibility(View.VISIBLE);
					juridicDetailsNrRc.setVisibility(View.VISIBLE);
					labelBenefTypeGrp.setError(null);
					break;

				default:
					break;
				}
			}
		});

		// Setup Authorization date start
		TextView textViewAuthorizationStart = (TextView) findViewById(R.id.textView_objective_authStartDate);
		textViewAuthorizationStart.setText(sdf.format(objective.getAuthorizationStart().getTime()));

		TextView authDateStart = (TextView) findViewById(R.id.textView_objective_authStartDate);

		authDateSClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_START, objective.getAuthorizationEnd(), objective.getAuthorizationStart(),
				R.id.textView_objective_authStartDate, R.id.textView_objective_authEndDate);

		authDateStart.setOnClickListener(authDateSClickListener);

		// Setup Authorization date end
		TextView textViewAuthorizatioEnd = (TextView) findViewById(R.id.textView_objective_authEndDate);
		textViewAuthorizatioEnd.setText(sdf.format(objective.getAuthorizationEnd().getTime()));

		TextView authDateEnd = (TextView) findViewById(R.id.textView_objective_authEndDate);

		authDateEClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_END, objective.getAuthorizationStart(), objective.getAuthorizationEnd(),
				R.id.textView_objective_authEndDate, R.id.textView_objective_authStartDate);
		authDateEnd.setOnClickListener(authDateEClickListener);

		// Setup address
		final EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
		final EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
		final EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);

		HashMap<String, String> addressComponents = getAddressComponents(objective.getAddress());
		addressCity.setText(Utils.flattenToAscii(addressComponents.get("city")));
		addressStreet.setText(Utils.flattenToAscii(addressComponents.get("street")));
		addressNumber.setText(addressComponents.get("number"));

		// Setup compute Button
		Button buttonComputeCoords = (Button) findViewById(R.id.button_computeCoordinates);
		buttonComputeCoords.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String addressString = generateAddressString(addressCity.getText().toString(),
						addressStreet.getText().toString(), addressNumber.getText().toString());

				Setup setup = new Setup(AddEditObjective.this);
				setup.hideKeyboard();

				populateCoordinateFields(addressString);

			}
		});

		// Setup my location button
		Button myLocation = (Button) findViewById(R.id.button_getCurrentGpsLocation);
		myLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng coordinates = getCurrentGpsLocation();

				if (coordinates != null) {

					if (coordinates.latitude == 0) {
						Toast.makeText(getApplicationContext(),
								"Coordonate eronate, repetati operatiunea dupa 30 de secunde.", Toast.LENGTH_LONG)
								.show();
						return;
					}

					EditText coordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
					EditText coordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);
					coordinatesLat.setText(Double.toString(coordinates.latitude));
					coordinatesLon.setText(Double.toString(coordinates.longitude));

					populateGeoFields(coordinates);
					areGpsCoordsAutom = true;

				}
			}
		});

		// Setup coordinates
		EditText editTextCoordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
		EditText editTextCoordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);
		String[] latlong = objective.getGps().split(",");

		editTextCoordinatesLat.setText(latlong[0]);
		editTextCoordinatesLon.setText(latlong[1]);

		editText_objective_execName = (EditText) findViewById(R.id.editText_objective_execName);
		editText_objective_execCUI = (EditText) findViewById(R.id.editText_objective_executantCUI);
		editText_objective_execNrRc = (EditText) findViewById(R.id.editText_objective_executantNrRc);

		// Meserias Fields, Author: Alin
		editText_objective_meserName = (EditText) findViewById(R.id.editText_objective_meserName);
		editText_objective_meserSurname = (EditText) findViewById(R.id.editText_objective_meserSurname);
		editText_objective_meserTel = (EditText) findViewById(R.id.editText_objective_meserTel);
		// Meserias Fields Alin
		
		editText_objective_address_phone = (EditText) findViewById(R.id.editText_objective_address_phone);
		editText_objective_address_phone.setText(objective.getTelBenef().trim());

		// Setup Stage spinner
		final StagePhaseSpinnerUtils spsUtils = new StagePhaseSpinnerUtils(this);
		final int objectiveId = objective.getId();

		final Spinner stageSpinner = (Spinner) findViewById(R.id.spinner_stage);
		final Spinner phaseSpinner = (Spinner) findViewById(R.id.spinner_phase);

		int limit = 0;
		if (objective.getTypeId() == Objective.TYPE_RENOVATION) {
			limit = 1;
		}

		spsUtils.populateAvailableStagesSpinner(stageSpinner, objectiveId, limit,
				stageSpinner.getSelectedItemPosition());

		for (int i = 0; i < stageSpinner.getCount(); i++) { // Find current
															// stage in spinner
			if (((Stage) stageSpinner.getAdapter().getItem(i)).getId() == objective.getStageId()) {
				stageSpinner.setSelection(i, false);
				lastSelectedStageId = i;
				lastSelectedStageHierarchy = ((Stage) stageSpinner.getAdapter().getItem(i)).getHierarchy();
				break;
			}
		}

		stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				int selectedStageId = ((Stage) adapterView.getSelectedItem()).getId();
				Log.d("DBG", "Selected stage Id: " + selectedStageId);
				int selectedPhasePosition = phaseSpinner.getSelectedItemPosition();

				if (selectedStageId != lastSelectedStageId || objectiveTypeChanged) {
					lastSelectedPhaseHierarchy = -1; // Reset so no conflicts
														// occur when
														// repopulating phases
					objectiveStageChanged = true;

					spsUtils.populateAvailablePhasesSpinner(phaseSpinner, -1, selectedStageId);

					if (selectedStageId == lastSelectedStageId) {
						phaseSpinner.setSelection(selectedPhasePosition);
					}

					lastSelectedStageId = selectedStageId;
					objectiveTypeChanged = false;

					int selectedStageHierarchy = ((Stage) adapterView.getSelectedItem()).getHierarchy();
					if (selectedStageHierarchy > ++lastSelectedStageHierarchy && !objectiveTypeChanged) {
						Log.d("DBG", "firing stage alert dialog");
						AlertDialog.Builder builder = new AlertDialog.Builder(AddEditObjective.this,
								R.style.AlertDialog);
						builder.setTitle(getString(R.string.stage_phase_warning_title));
						builder.setMessage(getString(R.string.stage_warning_text));
						builder.setPositiveButton(getString(R.string.stage_phase_warning_button_positive), null);
						builder.show();
					}

					lastSelectedStageHierarchy = ((Stage) adapterView.getSelectedItem()).getHierarchy();
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		// Setup Phase spinner
		spsUtils.populateAvailablePhasesSpinner(phaseSpinner, objectiveId, objective.getStageId());

		for (int i = 0; i < phaseSpinner.getCount(); i++) { // Find current
															// phase in spinner
			if (((Phase) phaseSpinner.getAdapter().getItem(i)).getId() == objective.getPhaseId()) {
				phaseSpinner.setSelection(i, false);
				lastSelectedPhaseId = i;
				lastSelectedPhaseHierarchy = ((Phase) phaseSpinner.getAdapter().getItem(i)).getHierarchy();
				break;
			}
		}

		phaseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
				Log.d("DBG", "Firing phaseSpinner onItemSelectedListener");

				if (position != lastSelectedPhaseId) {
					int phaseDefaultDuration = ((Phase) adapterView.getSelectedItem()).getDays();
					EditText phaseDuration = (EditText) findViewById(R.id.editText_objective_phaseDuration);
					phaseDuration.setText(Integer.toString(phaseDefaultDuration));
					lastSelectedPhaseId = position;

					int selectedPhaseHierarchy = ((Phase) adapterView.getSelectedItem()).getHierarchy();
					if (selectedPhaseHierarchy > ++lastSelectedPhaseHierarchy && !objectiveStageChanged) {
						Log.d("DBG", "firing phase alert dialog");
						AlertDialog.Builder builder = new AlertDialog.Builder(AddEditObjective.this,
								R.style.AlertDialog);
						builder.setTitle(getString(R.string.stage_phase_warning_title));
						builder.setMessage(getString(R.string.phase_warning_text));
						builder.setPositiveButton(getString(R.string.stage_phase_warning_button_positive), null);
						builder.show();
					}

					lastSelectedPhaseHierarchy = ((Phase) adapterView.getSelectedItem()).getHierarchy();
					objectiveStageChanged = false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});

		final TextView labelObjTpeGrp = (TextView) findViewById(R.id.label_objective_type);

		grpObjType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
				int initialPosition = stageSpinner.getSelectedItemPosition();
				switch (checkedId) {
				case R.id.radio_newConstruction:
					labelObjTpeGrp.setError(null);
					if (initialPosition > 0 && objectiveTypeRadioChecked) {
						initialPosition++;
					}
					int newPosition = spsUtils.populateAvailableStagesSpinner(stageSpinner, -1, 0,
							initialPosition).second;
					stageSpinner.setSelection(newPosition);
					objectiveTypeRadioChecked = true;
					objectiveTypeChanged = true;
					break;

				case R.id.radio_renovation:
					labelObjTpeGrp.setError(null);
					if (initialPosition >= 0 && objectiveTypeRadioChecked) {
						initialPosition--;
					}
					int newPositionReno = spsUtils.populateAvailableStagesSpinner(stageSpinner, -1, 1,
							initialPosition).second;
					stageSpinner.setSelection(newPositionReno);
					objectiveTypeRadioChecked = true;
					objectiveTypeChanged = true;
					break;

				default:
					break;
				}
			}
		});

		// Setup phase start field
		TextView textViewPhaseStart = (TextView) findViewById(R.id.textView_objective_phaseStartDate);
		textViewPhaseStart.setText(sdf.format(phaseStartCalendar.getTime()));

		LinearLayout phaseStart = (LinearLayout) findViewById(R.id.layout_phaseStartDate);
		phaseStartClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_START, phaseEndCalendar, phaseStartCalendar,
				R.id.textView_objective_phaseStartDate, R.id.textView_objective_phaseEndDate);
		phaseStart.setOnClickListener(phaseStartClickListener);

		// Setup phase end field
		TextView textViewPhaseEnd = (TextView) findViewById(R.id.textView_objective_phaseEndDate);
		textViewPhaseEnd.setText(sdf.format(phaseEndCalendar.getTime()));

		LinearLayout phaseEnd = (LinearLayout) findViewById(R.id.layout_phaseEndDate);
		phaseEndClickListener = new EditDateClickListener(AddEditObjective.this, EditDateClickListener.PURPOSE_ADD,
				EditDateClickListener.TYPE_END, phaseStartCalendar, phaseEndCalendar,
				R.id.textView_objective_phaseEndDate, R.id.textView_objective_phaseStartDate);
		phaseEnd.setOnClickListener(phaseEndClickListener);

		// Setup phase duration
		EditText editTextPhaseDuration = (EditText) findViewById(R.id.editText_objective_phaseDuration);
		editTextPhaseDuration.setText(Integer.toString(currentPhaseDuration));

		editTextPhaseDuration.addTextChangedListener(
				new PhaseDurationChangeWatcher(this, phaseEndClickListener, textViewPhaseStart, textViewPhaseEnd));

		if (user.getUserType() == User.TYPE_DVA || mode == Constants.OBJECTIVES_ARCHIVE) {
			RadioButton radioNewConstruction = (RadioButton) findViewById(R.id.radio_newConstruction);
			RadioButton radioRenovation = (RadioButton) findViewById(R.id.radio_renovation);
			RadioButton radioIndividual = (RadioButton) findViewById(R.id.radio_fizic);
			RadioButton radioLegal = (RadioButton) findViewById(R.id.radio_juridic);
			EditText cuiText = (EditText) findViewById(R.id.editText_objective_benefCUI);
			EditText nrRcText = (EditText) findViewById(R.id.editText_objective_benefNrRc);

			Spinner regionsSpinner = (Spinner) findViewById(R.id.spinner_objective_region);
			Spinner statusSpinner = (Spinner) findViewById(R.id.spinner_objective_status);
			Spinner categorySpinner = (Spinner) findViewById(R.id.spinner_objective_category);
			Spinner executantSpinner = (Spinner) findViewById(R.id.spinner_objective_executant);

			ArrayList<View> views = new ArrayList<View>();
			views.add(addDate);
			views.add(editTextName);
			views.add(editTextEstValue);
			views.add(grpObjType);
			views.add(radioNewConstruction);
			views.add(radioRenovation);
			views.add(acvBeneficiaryName);
			views.add(beneficiaryRadios);
			views.add(radioIndividual);
			views.add(radioLegal);
			views.add(cuiText);
			views.add(nrRcText);
			views.add(authDateStart);
			views.add(authDateEnd);
			views.add(addressCity);
			views.add(addressStreet);
			views.add(addressNumber);
			views.add(buttonComputeCoords);
			views.add(myLocation);
			views.add(editTextCoordinatesLat);
			views.add(editTextCoordinatesLon);
			views.add(phaseSpinner);
			views.add(stageSpinner);
			views.add(phaseStart);
			views.add(phaseEnd);
			views.add(editTextPhaseDuration);
			views.add(regionsSpinner);
			views.add(statusSpinner);
			views.add(categorySpinner);
			views.add(executantSpinner);
			views.add(editText_objective_execName);
			views.add(editText_objective_execCUI);
			views.add(editText_objective_execNrRc);
			
			// Meserias views Alin
			views.add(editText_objective_meserName);
			views.add(editText_objective_meserSurname);
			views.add(editText_objective_meserTel);
			// End Meserias views Alin
			
			views.add(editText_objective_address_phone);


			disableUi(views);

			map.setOnMapLongClickListener(null);
		}

		// Put a marker where the objective is
		// String[] latlong = objective.getGps().split(",");
		double latitude = Double.parseDouble(latlong[0]);
		double longitude = Double.parseDouble(latlong[1]);

		LatLng latLng = new LatLng(latitude, longitude);

		map.addMarker(new MarkerOptions().position(latLng)
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 21); // Zoom
																				// to
																				// new
																				// marker
																				// location
		map.animateCamera(update);
	}

	/**
	 * Convenience method for saving a new objective
	 */
	private void saveNewObjective() {
		Setup setup = new Setup(AddEditObjective.this);
		setup.hideKeyboard();

		List<Object> savedObjectiveData = createObjectiveFromForm();
		ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());

		if (savedObjectiveData != null) {
			objectiveData.addObjective(savedObjectiveData);
			Objective tempObjective = null;

			for (Object obj : savedObjectiveData) {
				if (obj instanceof Objective) {
					tempObjective = (Objective) obj;
				}
			}

			Intent i = getIntent();
			i.putExtra(Constants.KEY_COORDINATES, tempObjective.getGps());
			setResult(RESULT_OK, i);
			finish();

			sendLocalDataToServer();

		}
		Log.d("DBG", "Am salvat obiectivul");
	}

	/**
	 * Convenience method for saving objective edits
	 */
	private void saveObjectiveModifications() {
		Setup setup = new Setup(AddEditObjective.this);
		setup.hideKeyboard();

		ObjectiveData objectiveData = new ObjectiveData(this);

		savedObjective = createObjectiveFromForm();

		if (savedObjective != null) {
			objectiveData.editObjective(savedObjective);
			Objective tempObjective = null;

			for (Object obj : savedObjective) {
				if (obj instanceof Objective) {
					tempObjective = (Objective) obj;
				}
			}

			Intent i = getIntent();
			i.putExtra(Constants.KEY_COORDINATES, tempObjective.getGps());
			setResult(RESULT_OK, i);
			finish();

			sendLocalDataToServer();
		}
		Log.d("DBG", "Am salvat obiectivul");
	}

	/**
	 * Convenience method for archiving an objective
	 */
	private void archiveObjective() {
		Setup setup = new Setup(AddEditObjective.this);
		setup.hideKeyboard();

		ObjectiveData objectiveData = new ObjectiveData(this);
		savedObjective = createObjectiveFromForm();

		if (savedObjective != null) {
			objectiveData.editAndArchive(savedObjective);
			Objective tempObjective = null;

			for (Object obj : savedObjective) {
				if (obj instanceof Objective) {
					tempObjective = (Objective) obj;
				}
			}

			Intent i = getIntent();
			i.putExtra(Constants.KEY_COORDINATES, tempObjective.getGps());
			setResult(RESULT_OK, i);
			finish();

			sendLocalDataToServer();
		}
		Log.d("DBG", "Am salvat obiectivul");
	}

	/**
	 * Disables all the provided views in order to prevent editing
	 *
	 * @param views
	 *            The views that will be disabled
	 */
	private void disableUi(ArrayList<View> views) {

		for (View view : views) {
			if (view instanceof EditText) {
				((EditText) view).setKeyListener(null);
			}
			if (view instanceof Button) {
				((Button) view).setEnabled(false);
			}

			if (view instanceof Spinner) {
				((Spinner) view).setEnabled(false);
			}

			view.setFocusable(false);
			view.setClickable(false);
			view.setFocusableInTouchMode(false);
		}

	}

	@Override
	public void onDateSelected(Calendar limit, int modifierPurpose, int modifierType, int changeTarget,
			int limitTarget) {

		// Change the date in the targeted date field
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);
		TextView dateToChange = (TextView) findViewById(changeTarget);
		EditText days = (EditText) findViewById(R.id.editText_objective_phaseDuration);
		int daysInt = Integer.parseInt(days.getText().toString());
		dateToChange.setText(sdf.format(limit.getTime()));

		// Update date limits based on limitTarget
		switch (limitTarget) {
		case R.id.textView_objective_authStartDate:
			authDateEClickListener.setDefaultDate(limit);
			TextView authEnd = (TextView) findViewById(R.id.textView_objective_authEndDate);
			authEnd.setError(null);

			authDateSClickListener.setLimit(limit);
			break;

		case R.id.textView_objective_authEndDate:
			authDateSClickListener.setDefaultDate(limit);
			TextView authStart = (TextView) findViewById(R.id.textView_objective_authStartDate);
			authStart.setError(null);
			authDateEClickListener.setLimit(limit);
			break;

		case R.id.textView_objective_phaseStartDate:
			phaseEndClickListener.setDefaultDate(limit);
			TextView labelPhaseEnd = (TextView) findViewById(R.id.label_objective_phaseEndDate);
			labelPhaseEnd.setError(null);
			phaseStartClickListener.setLimit(limit);
			break;

		case R.id.textView_objective_phaseEndDate:
			phaseStartClickListener.setDefaultDate(limit);
			TextView labelPhaseStart = (TextView) findViewById(R.id.label_objective_phaseStartDate);
			labelPhaseStart.setError(null);
			limit.add(Calendar.DAY_OF_MONTH, daysInt);
			phaseEndClickListener.setLimit(limit);
			break;

		case R.id.textView_objective_addDate:
			TextView labelAddDate = (TextView) findViewById(R.id.label_objective_addDate);
			labelAddDate.setError(null);

		default:
			break;

		}

	}

	@Override
	public void onPhaseDurationChanged(Calendar newLimit, boolean changeDate) {
		phaseEndClickListener.setLimit(newLimit);

		if (changeDate) {
			phaseEndClickListener.setDefaultDate(newLimit);

			TextView phaseEndText = (TextView) findViewById(R.id.textView_objective_phaseEndDate);
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);

			phaseEndText.setText(sdf.format(newLimit.getTime()));
		}
	}

	private class StageSpinnerAdapter extends ArrayAdapter<Stage> {

		public StageSpinnerAdapter(Context context, int resource, List<Stage> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_dropdown_item, parent,
						false);
			}

			TextView nameText = (TextView) convertView.findViewById(R.id.spinner_text);
			nameText.setText(getItem(pos).getName());

			return convertView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.spinner_dropdown_item, null);
			}
			TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
			label.setText(getItem(position).getName());

			return label;
		}

	}

	private class PhaseSpinnerAdapter extends ArrayAdapter<Phase> {

		public PhaseSpinnerAdapter(Context context, int resource, List<Phase> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_dropdown_item, parent,
						false);
			}

			TextView nameText = (TextView) convertView.findViewById(R.id.spinner_text);
			nameText.setText(getItem(pos).getName());

			return convertView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.spinner_dropdown_item, null);
			}
			TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
			label.setText(getItem(position).getName());

			return label;
		}

	}

	private String changeDateFormat(String dateString, String oldDateFormat, String newDateFormat) {

		DateFormat fromFormat = new SimpleDateFormat(oldDateFormat);
		fromFormat.setLenient(false);
		DateFormat toFormat = new SimpleDateFormat(newDateFormat);
		toFormat.setLenient(false);
		Date date = null;
		try {
			date = fromFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			Log.d("DBG", "Nu am putut sa schimb formatul datei din filtru " + e.getMessage());
		}
		return toFormat.format(date);
	}

	/**
	 * Setup method for defining the behavior of the map fragment
	 */
	private void setupMapFunctionality() {
		// SET UP Google Map
		SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

		map = mapFrag.getMap();
		map.setMapType(map.MAP_TYPE_NORMAL);
		// display zoom in/out button on map
		UiSettings uiSettings = map.getUiSettings();
		uiSettings.setZoomControlsEnabled(true);
		uiSettings.setCompassEnabled(true);
		uiSettings.setMyLocationButtonEnabled(false);

		if (GPS_location != null) {
			GPS_location.remove();
			GPS_location = null;
		}

		gps = new GPSTracker(AddEditObjective.this);

		if (gps.isCanGetLocation()) {

			double latitude = gps.getLatitude();
			double longitude = gps.getlongitude();

			LatLng currentPosition = new LatLng(latitude, longitude);

			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentPosition, 12);
			map.animateCamera(update);

		} else {
			gps.showSettingsAlert();
		}

		// Call DialogBox to create new Objective
		map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				gps = new GPSTracker(AddEditObjective.this);

				if (gps.isCanGetLocation()) {

					newMarkerPosition = latLng;

					final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:

								map.clear();
								// we do not use it like this, we use clustering
								// to add marker to map!
								map.addMarker(new MarkerOptions().position(newMarkerPosition) // create
																								// new
																								// marker
										.title("Obiectiv Nou").icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

								EditText coordinatesLat = (EditText) findViewById(
										R.id.editText_objective_coordinates_lat);
								EditText coordinatesLon = (EditText) findViewById(
										R.id.editText_objective_coordinates_lon);
								coordinatesLat.setText(Double.toString(newMarkerPosition.latitude));
								coordinatesLon.setText(Double.toString(newMarkerPosition.longitude));

								coordinatesLat.setError(null);
								coordinatesLon.setError(null);

								EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
								EditText addressStreet = (EditText) findViewById(
										R.id.editText_objective_address_street);
								EditText addressNumber = (EditText) findViewById(
										R.id.editText_objective_address_number);

								populateGeoFields(newMarkerPosition);

								CameraUpdate update = CameraUpdateFactory.newLatLngZoom(newMarkerPosition, 21); // Zoom
																												// to
																												// new
																												// marker
																												// location
								map.animateCamera(update);

								break;

							case DialogInterface.BUTTON_NEGATIVE:
								// No button clicked
								break;
							}
						}
					};

					String dialogMessage;
					if (purpose == EDIT) {
						dialogMessage = getString(R.string.objective_edit_message);
					} else {
						dialogMessage = getString(R.string.objective_add_message);
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(AddEditObjective.this, R.style.AlertDialog);
					builder.setTitle(getString(R.string.objective_add_title));
					builder.setMessage(dialogMessage);
					builder.setPositiveButton(getString(R.string.objective_add_positive), dialogClickListener);
					builder.setNegativeButton(getString(R.string.objective_add_negative), dialogClickListener);
					builder.show();
				}
			}
		});
	}

	private LatLng getCoordinatesFromAddress(String address) {

		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocationName(address, 1);
			if (addresses.size() > 0)
				return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
			else
				return null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void populateCoordinateFields(String address) {

		new AsyncTask<String, Void, LatLng>() {

			@Override
			protected void onPreExecute() {

				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_computeCoordinates);
				progressBar.setVisibility(View.VISIBLE);

				EditText coordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
				EditText coordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);

				EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
				EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
				EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);

				coordinatesLat.setEnabled(false);
				coordinatesLon.setEnabled(false);
				addressCity.setEnabled(false);
				addressStreet.setEnabled(false);
				addressNumber.setEnabled(false);
			}

			@Override
			protected LatLng doInBackground(String... params) {
				Geocoder geocoder = new Geocoder(AddEditObjective.this, Locale.getDefault());
				List<Address> addresses;
				try {
					addresses = geocoder.getFromLocationName(params[0], 1);
					if (addresses.size() > 0)
						return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
					else
						return null;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(LatLng computedCoordinates) {

				EditText coordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
				EditText coordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);

				EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
				EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
				EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);

				coordinatesLat.setEnabled(true);
				coordinatesLon.setEnabled(true);
				addressCity.setEnabled(true);
				addressStreet.setEnabled(true);
				addressNumber.setEnabled(true);

				if (computedCoordinates != null) {
					map.clear();
					map.addMarker(new MarkerOptions().position(computedCoordinates)
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

					CameraUpdate update = CameraUpdateFactory.newLatLngZoom(computedCoordinates, 21); // Zoom
																										// to
																										// new
																										// marker
																										// location
					map.animateCamera(update);

					coordinatesLat.setText(Double.toString(computedCoordinates.latitude));
					coordinatesLon.setText(Double.toString(computedCoordinates.longitude));

					coordinatesLat.setError(null);
					coordinatesLon.setError(null);

				} else {
					Toast.makeText(getApplicationContext(), "Nu au putut fi gasite coordinate pentru aceasta adresa",
							Toast.LENGTH_LONG).show();
				}

				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_computeCoordinates);
				progressBar.setVisibility(View.INVISIBLE);
			}

		}.execute(address);

	}

	private Address getAddressFromCoordinates(LatLng coordinates) {
		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
			if (addresses.size() > 0)
				return addresses.get(0);
			else
				return null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void populateGeoFields(LatLng coordinates) {

		new AsyncTask<LatLng, Void, Address>() {
			@Override
			protected void onPreExecute() {

				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_currentLocation);
				progressBar.setVisibility(View.VISIBLE);

				EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
				EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
				EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);
				EditText coordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
				EditText coordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);

				coordinatesLat.setEnabled(false);
				coordinatesLon.setEnabled(false);
				addressCity.setEnabled(false);
				addressStreet.setEnabled(false);
				addressNumber.setEnabled(false);
			}

			@Override
			protected Address doInBackground(LatLng... params) {
				Geocoder geocoder = new Geocoder(AddEditObjective.this, Locale.getDefault());
				List<Address> addresses;
				try {
					addresses = geocoder.getFromLocation(params[0].latitude, params[0].longitude, 1);
					if (addresses.size() > 0)
						return addresses.get(0);
					else
						return null;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(Address address) {

				EditText addressCity = (EditText) findViewById(R.id.editText_objective_address_city);
				EditText addressStreet = (EditText) findViewById(R.id.editText_objective_address_street);
				EditText addressNumber = (EditText) findViewById(R.id.editText_objective_address_number);
				EditText coordinatesLat = (EditText) findViewById(R.id.editText_objective_coordinates_lat);
				EditText coordinatesLon = (EditText) findViewById(R.id.editText_objective_coordinates_lon);
				Spinner regionsSpinner = (Spinner) findViewById(R.id.spinner_objective_region);

				coordinatesLat.setEnabled(true);
				coordinatesLon.setEnabled(true);
				addressCity.setEnabled(true);
				addressStreet.setEnabled(true);
				addressNumber.setEnabled(true);

				if (address != null) {

					String[] region = com.arabesque.obiectivecva.Utils.flattenToAscii(address.getAdminArea())
							.split(" ");

					for (int i = 0; i < regionsSpinner.getCount(); i++) {
						if (regionsSpinner.getItemAtPosition(i).toString().toLowerCase(Locale.US)
								.contains(region[region.length - 1].toLowerCase())) {
							regionsSpinner.setSelection(i);
							break;

						}
					}

					addressCity
							.setText(address.getLocality() != null ? Utils.flattenToAscii(address.getLocality()) : "");
					addressStreet.setText(
							address.getThoroughfare() != null ? Utils.flattenToAscii(address.getThoroughfare()) : "");
					addressNumber.setText(address.getSubThoroughfare() != null ? address.getSubThoroughfare() : "");

				} else {
					Toast.makeText(getApplicationContext(), "Nu a putut fi gasita o adresa pentru aceste coordonate",
							Toast.LENGTH_LONG).show();

					addressCity.setText("");
					addressStreet.setText("");
					addressNumber.setText("");

				}

				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_currentLocation);
				progressBar.setVisibility(View.GONE);
			}
		}.execute(coordinates);

	}

	private String generateAddressStringEmpty(String city, String street, String number) {
		StringBuilder sBuilder = new StringBuilder();

		if (city.length() > 0)
			sBuilder.append(city);

		if (street.length() > 0) {
			sBuilder.append(", ");
			sBuilder.append(street);
		}

		if (number.length() > 0) {
			sBuilder.append(", ");
			sBuilder.append(number);
		}

		return sBuilder.toString();
	}

	private String generateAddressString(String city, String street, String number) {
		StringBuilder sBuilder = new StringBuilder();

		// Append city if exists;
		if (city.length() > 0)
			sBuilder.append(city);
		else
			sBuilder.append("");

		if (street.length() > 0) {
			sBuilder.append(", ");
			sBuilder.append(street);
		} else
			sBuilder.append("");

		if (number.length() > 0) {
			sBuilder.append(", ");
			sBuilder.append(number);
		} else
			sBuilder.append("");

		return sBuilder.toString();
	}

	private HashMap<String, String> getAddressComponents(String address) {
		String[] addressComponentsSplit = address.split(",");
		HashMap<String, String> addressComponents = new HashMap<String, String>();

		addressComponents.put("city", addressComponentsSplit[0].trim());

		if (addressComponentsSplit.length >= 2)
			addressComponents.put("street", addressComponentsSplit[1].trim());
		else
			addressComponents.put("street", "");

		if (addressComponentsSplit.length >= 3)
			addressComponents.put("number", addressComponentsSplit[2].trim());
		else
			addressComponents.put("number", "");

		return addressComponents;
	}

	private void disableCopyPaste(EditText editText) {

		editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

			@Override
			public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode actionMode) {
			}
		});

	}

	private void enableViews(View v, boolean enabled) {
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				enableViews(vg.getChildAt(i), enabled);
			}
		}
		v.setEnabled(enabled);
	}

	private LatLng getCurrentGpsLocation() {
		gps = new GPSTracker(AddEditObjective.this);
		LatLng currentPosition = null;

		if (gps.isCanGetLocation()) {

			double latitude = gps.getLatitude();
			double longitude = gps.getlongitude();

			// display text in button of the window

			currentPosition = new LatLng(latitude, longitude);

			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

			map.clear();
			map.addMarker(new MarkerOptions().position(currentPosition)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentPosition, MapActivity.ZOOM_PLACE);
			map.animateCamera(update);

			// map.setMyLocationEnabled(true);

		} else {
			gps.showSettingsAlert();
		}

		return currentPosition;
	}
}
