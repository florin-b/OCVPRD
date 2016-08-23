package com.stimasoft.obiectivecva.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.enums.EnumFiliale;
import com.arabesque.obiectivecva.enums.EnumJudete;
import com.arabesque.obiectivecva.enums.EnumStadiuObiectiv;
import com.arabesque.obiectivecva.model.Agent;
import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.Region;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Utility class for filter generation
 */
public class FilterUtils {

	private Context context;
	private Activity activity;
	
	public static final String EQUALS = " = ";

	public static final String GREATER_THAN = " > ";
	public static final String LESS_THAN = " < ";

	public static final String GEATER_OR_EQUAL = " >= ";
	public static final String LESS_OR_EQUAL = " <= ";

	public static final String NOT_EQUALS = " <> ";

	private static final String IN = " IN ";
	private static final String LIKE = " LIKE ";
	public static final String BETWEEN = " BETWEEN ";

	private static final String FILTER_DATE_FORMAT = "dd-MM-yyyy";

	public static final String ASCENDING = "ASC";
	public static final String DESCENDING = "DESC";

	public FilterUtils(Context context) {
		this.context = context;
		this.activity = (Activity) context;
	}

	/**
	 * Generates "WHERE" arguments for the sql query using data from the fields
	 * in the filter drawer
	 *
	 * @return HashMap with the following structure: HashMap{@literal <}Column,
	 *         {@literal <}Operation, Value>
	 */
	public HashMap<String, Pair<String, String>> generateFilterWhere() {
		// TODO @Filip use this for filtering the map
		HashMap<String, Pair<String, String>> whereArgs = new HashMap<String, Pair<String, String>>();

		// filiala
		Spinner spinnerFiliale = (Spinner) activity.findViewById(R.id.spinner_objective_filiala);
		if (spinnerFiliale.getSelectedItemPosition() > 0) {
			String codFiliala = EnumFiliale.getCodFiliala(spinnerFiliale.getSelectedItem().toString());
			whereArgs.put(SQLiteHelper.FILIALA, new Pair<String, String>(EQUALS, "'" + codFiliala + "'"));
		}

		// consilier
		Spinner spinnerConsilieri = (Spinner) activity.findViewById(R.id.spinner_objective_consilier);
		if (spinnerConsilieri.getSelectedItemPosition() > 0) {
			String codConsilier = ((Agent) spinnerConsilieri.getSelectedItem()).getCod();
			whereArgs.put(SQLiteHelper.CVA_CODE, new Pair<String, String>(EQUALS, "'" + codConsilier + "'"));
		}

		if (UserInfo.getInstance().getTipUser().equalsIgnoreCase("CV")) {
			whereArgs.put(SQLiteHelper.FILIALA, new Pair<String, String>(EQUALS, "'" + UserInfo.getInstance().getUnitLog() + "'"));
			whereArgs.put(SQLiteHelper.CVA_CODE, new Pair<String, String>(EQUALS, "'" + UserInfo.getInstance().getCod() + "'"));
		}
		
		// status
		Spinner spinnerStatusObiect = (Spinner) activity.findViewById(R.id.spinner_filter_status);
		int objectiveStatusId = EnumStadiuObiectiv.getCodStadiu(spinnerStatusObiect.getSelectedItem().toString());
		whereArgs.put(SQLiteHelper.STATUS_ID, new Pair<String, String>(EQUALS, String.valueOf(objectiveStatusId)));

		// Region filter
		Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_filter_region);

		if (regionSpinner.getSelectedItemPosition() > 0) {
			int regionId = Integer.valueOf(EnumJudete.getCodJudet(regionSpinner.getSelectedItem().toString()));
			whereArgs.put(SQLiteHelper.REGION_ID, new Pair<String, String>(EQUALS, String.valueOf(regionId)));
		}

		// Make Name filter
		AutoCompleteTextView nameText = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_name);
		if (nameText.getText().length() > 0) {
			whereArgs.put(SQLiteHelper.NAME, new Pair<String, String>(LIKE, "'%" + nameText.getText().toString().trim() + "%'"));
		}

		// City filter
		EditText cityText = (EditText) activity.findViewById(R.id.editText_filter_city);
		if (cityText.getText().toString().trim().length() > 0) {
			whereArgs.put(SQLiteHelper.ADDRESS, new Pair<String, String>(LIKE, "'" + cityText.getText().toString().trim() + "%'"));
		}

		// Make Date Added filter
		TextView dateAddedStart = (TextView) activity.findViewById(R.id.filter_dateAdded_start);
		TextView dateAddedEnd = (TextView) activity.findViewById(R.id.filter_dateAdded_end);

		// Check if both fields are completed, and make a special sql statement
		// for that case
		if (dateAddedStart.getText().length() > 0 && dateAddedEnd.getText().length() > 0) {
			// Desired form: WHERE startDate BETWEEN 'date1' AND 'date2'
			String argumentString = "'" + changeDateFormat(dateAddedStart.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'" + " AND "
					+ "'" + changeDateFormat(dateAddedEnd.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";

			whereArgs.put(SQLiteHelper.CREATION_DATE, new Pair<String, String>(BETWEEN, argumentString));
		} else {
			if (dateAddedStart.getText().length() > 0) {
				String dateAddedStartString = "'" + changeDateFormat(dateAddedStart.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";
				whereArgs.put(SQLiteHelper.CREATION_DATE, new Pair<String, String>(GEATER_OR_EQUAL, dateAddedStartString));
			}

			if (dateAddedEnd.getText().length() > 0) {
				String dateAddedEndString = "'" + changeDateFormat(dateAddedEnd.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";
				whereArgs.put(SQLiteHelper.CREATION_DATE, new Pair<String, String>(LESS_OR_EQUAL, dateAddedEndString));
			}
		}

		// Make Beneficiary filter
		EditText beneficiary = (EditText) activity.findViewById(R.id.autoComplete_filter_beneficiary);

		if (beneficiary.getText().length() > 0) {
			whereArgs.put(SQLiteHelper.BENEFICIARY_NAME, new Pair<String, String>(LIKE, "'%" + beneficiary.getText().toString() + "%'"));
		}

		// Make Beneficiary Type filter
		RadioGroup beneficiaryRadios = (RadioGroup) activity.findViewById(R.id.radioGroup_filter_beneficiaryType);
		int selectedRadioID = beneficiaryRadios.getCheckedRadioButtonId();

		switch (selectedRadioID) {
		case R.id.radio_filter_fizic:
			whereArgs.put(SQLiteHelper.BENEFICIARY_TYPE, new Pair<String, String>(EQUALS, Integer.toString(0)));
			break;

		case R.id.radio_filter_juridic:
			whereArgs.put(SQLiteHelper.BENEFICIARY_TYPE, new Pair<String, String>(EQUALS, Integer.toString(1)));

			EditText cuiText = (EditText) activity.findViewById(R.id.editText_filter_cui);
			if (cuiText.getText().length() > 0)
				whereArgs.put(SQLiteHelper.CUI, new Pair<String, String>(LIKE, "'%" + cuiText.getText().toString() + "%'"));

			EditText nrRcText = (EditText) activity.findViewById(R.id.editText_filter_nrRc);
			if (nrRcText.getText().toString().length() > 0)
				whereArgs.put(SQLiteHelper.NR_RC, new Pair<String, String>(LIKE, "'%" + nrRcText.getText().toString() + "%'"));

		default:
			break;
		}

		// Make Authorization Date filter
		TextView authDateStart = (TextView) activity.findViewById(R.id.filter_authDate_start);
		TextView authDateEnd = (TextView) activity.findViewById(R.id.filter_authDate_end);

		if (authDateStart.getText().length() > 0 && authDateEnd.getText().length() > 0) {
			// Desired form: WHERE startDate BETWEEN 'date1' AND 'date2'
			String argumentString = "'" + changeDateFormat(authDateStart.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'" + " AND "
					+ "'" + changeDateFormat(authDateEnd.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";

			whereArgs.put(SQLiteHelper.AUTHORIZATION_START, new Pair<String, String>(BETWEEN, argumentString));
		} else {
			if (authDateStart.getText().length() > 0) {
				String authDateStartString = "'" + changeDateFormat(authDateStart.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";
				whereArgs.put(SQLiteHelper.AUTHORIZATION_START, new Pair<String, String>(GEATER_OR_EQUAL, authDateStartString));
			}

			if (authDateEnd.getText().length() > 0) {
				String authDateEndString = "'" + changeDateFormat(authDateEnd.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";
				whereArgs.put(SQLiteHelper.AUTHORIZATION_START, new Pair<String, String>(LESS_OR_EQUAL, authDateEndString));
			}
		}

		// Make Authorization Exp Date filter
		TextView authExpDateStart = (TextView) activity.findViewById(R.id.filter_authExpDate_start);
		TextView authExpDateEnd = (TextView) activity.findViewById(R.id.filter_authExpDate_end);

		if (authExpDateStart.getText().length() > 0 && authExpDateEnd.getText().length() > 0) {
			// Desired form: WHERE startDate BETWEEN 'date1' AND 'date2'
			String argumentString = "'" + changeDateFormat(authExpDateStart.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'" + " AND "
					+ "'" + changeDateFormat(authExpDateEnd.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";

			whereArgs.put(SQLiteHelper.AUTHORIZATION_END, new Pair<String, String>(BETWEEN, argumentString));
		} else {
			if (authExpDateStart.getText().length() > 0) {
				String authExpDateStartString = "'" + changeDateFormat(authExpDateStart.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT)
						+ "'";
				whereArgs.put(SQLiteHelper.AUTHORIZATION_END, new Pair<String, String>(GEATER_OR_EQUAL, authExpDateStartString));
			}

			if (authExpDateEnd.getText().length() > 0) {
				String authExpDateEndString = "'" + changeDateFormat(authExpDateEnd.getText().toString(), FILTER_DATE_FORMAT, Constants.DB_DATE_FORMAT) + "'";
				whereArgs.put(SQLiteHelper.AUTHORIZATION_END, new Pair<String, String>(LESS_OR_EQUAL, authExpDateEndString));
			}
		}
		// Make Estimated Value filter
		EditText estValueStart = (EditText) activity.findViewById(R.id.filter_estValue_start);
		EditText estValueEnd = (EditText) activity.findViewById(R.id.filter_estValue_end);

		if (estValueStart.getText().length() > 0 && estValueEnd.getText().length() > 0) {
			// Desired form: WHERE startDate BETWEEN 'value1' AND 'value2'

			// Check which number is the largest. This way, the filter will work
			// regardless of number order
			float valueLeft = Float.parseFloat(estValueStart.getText().toString());
			float valueRight = Float.parseFloat(estValueEnd.getText().toString());

			String argumentString;

			if (valueLeft < valueRight) {
				argumentString = estValueStart.getText().toString() + " AND " + estValueEnd.getText().toString();
			} else if (valueLeft > valueRight) {
				argumentString = estValueEnd.getText().toString() + " AND " + estValueStart.getText().toString();
			} else {
				argumentString = estValueStart.getText().toString() + " AND " + estValueEnd.getText().toString();
			}

			whereArgs.put(SQLiteHelper.ESTIMATION_VALUE, new Pair<String, String>(BETWEEN, argumentString));
		} else {
			if (estValueStart.getText().length() > 0) {
				whereArgs.put(SQLiteHelper.ESTIMATION_VALUE, new Pair<String, String>(GEATER_OR_EQUAL, estValueStart.getText().toString()));
			}

			if (estValueEnd.getText().length() > 0) {
				whereArgs.put(SQLiteHelper.ESTIMATION_VALUE, new Pair<String, String>(LESS_OR_EQUAL, estValueEnd.getText().toString()));
			}
		}

		// Make Postal Code filter
		EditText postalCode = (EditText) activity.findViewById(R.id.editText_filter_postalCode);

		if (postalCode.getText().length() > 0) {
			whereArgs.put(SQLiteHelper.ZIP, new Pair<String, String>(EQUALS, postalCode.getText().toString()));
		}

		// Make CVA Code filter
		//EditText cvaCode = (EditText) activity.findViewById(R.id.editText_filter_cvaCode);

		//if (cvaCode.getText().length() > 0) {
		//	whereArgs.put(SQLiteHelper.CVA_CODE, new Pair<String, String>(IN, "( " + cvaCode.getText().toString() + " )"));
		//}

		return whereArgs;
	}

	/**
	 * Changes a given date string's format. Dis is done by converting the
	 * string to a Date and then back to a string using the specified new
	 * format.
	 *
	 * @param dateString
	 *            The date string to be parsed and reformated
	 * @param oldDateFormat
	 *            The string's known old format
	 * @param newDateFormat
	 *            The string's desired new format
	 * @return
	 */
	private String changeDateFormat(String dateString, String oldDateFormat, String newDateFormat) {

		// SimpleDateFormat sdf = new
		// SimpleDateFormat(Constants.USER_DATE_FORMAT);

		// Log.d("DBG", "CHECK DATE FORMAT: \ndateString: " + dateString +
		// "\noldDateFormat: " + oldDateFormat + "\nnewDateFormat: " +
		// newDateFormat);

		DateFormat fromFormat = new SimpleDateFormat(oldDateFormat);
		fromFormat.setLenient(false);
		DateFormat toFormat = new SimpleDateFormat(newDateFormat);
		toFormat.setLenient(false);

		Date date = null;
		try {
			// date = fromFormat.parse(dateString);
			SimpleDateFormat sdf = new SimpleDateFormat(oldDateFormat);
			date = sdf.parse(dateString);
			if (!dateString.equals(sdf.format(date))) {
				date = toFormat.parse(dateString);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			Log.d("DBG", "Nu am putut sa schimb formatul datei din filtru " + e.getMessage());
		}

		return toFormat.format(date);
	}

}
