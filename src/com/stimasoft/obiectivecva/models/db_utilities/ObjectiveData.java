package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.model.OperatiiTabele;
import com.arabesque.obiectivecva.utils.Utils;
import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.models.db_classes.ObjectiveLite;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.maps.ObjectiveItem;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for handling objective database operations
 */

public class ObjectiveData {

	private SQLiteHelper dbHelper;
	private Context context;

	private static final int QUERY_LIMIT = 400;

	public ObjectiveData(Context cntxt) {
		context = cntxt;
		dbHelper = new SQLiteHelper(cntxt);
	}

	/**
	 * Utility method designed to handle the generation of "WHERE" arguments
	 * that need conditioning
	 *
	 * @param whereArgs
	 *            Hashmap containing (Column, (Operation, Value)) elements
	 * @param db
	 *            Database instance needed for queries
	 * @return A string containing the "WHERE" arguments generated using the
	 *         provided hashmap
	 */
	private String getCondition(HashMap<String, Pair<String, String>> whereArgs, SQLiteDatabase db) {
		String query = "";

		// Get the beneficiary filter if it exists
		Pair<String, String> benefNameEntry = whereArgs.get(SQLiteHelper.BENEFICIARY_NAME);

		if (benefNameEntry != null) {
			query += " AND b." + SQLiteHelper.NAME + " " + benefNameEntry.first + " " + benefNameEntry.second;
			whereArgs.remove(SQLiteHelper.BENEFICIARY_NAME);
		}

		Pair<String, String> benefTypeEntry = whereArgs.get(SQLiteHelper.BENEFICIARY_TYPE);

		if (benefTypeEntry != null) {
			query += " AND o." + SQLiteHelper.BENEFICIARY_TYPE + " " + benefTypeEntry.first + " "
					+ benefTypeEntry.second;

			Pair<String, String> cuiEntry = whereArgs.get(SQLiteHelper.CUI);
			Pair<String, String> nrRcEntry = whereArgs.get(SQLiteHelper.NR_RC);

			String beneficiariesQuery = "SELECT b." + SQLiteHelper.ID + " FROM " + SQLiteHelper.TABLE_BENEFICIARIES
					+ " b WHERE 1 = 1 ";

			beneficiariesQuery += " AND b." + SQLiteHelper.TYPE + " " + benefTypeEntry.first + " "
					+ benefTypeEntry.second;

			if (cuiEntry != null) {
				beneficiariesQuery += " AND b." + SQLiteHelper.CUI + " " + cuiEntry.first + " " + cuiEntry.second;
				whereArgs.remove(SQLiteHelper.CUI);
			}

			if (nrRcEntry != null) {
				beneficiariesQuery += " AND b." + SQLiteHelper.NR_RC + " " + nrRcEntry.first + " " + nrRcEntry.second;
				whereArgs.remove(SQLiteHelper.NR_RC);
			}

			Cursor cursor = db.rawQuery(beneficiariesQuery, null);

			cursor.moveToFirst();

			ArrayList<String> validBeneficiaries = new ArrayList<String>();

			while (!cursor.isAfterLast()) {
				validBeneficiaries.add(Integer.toString(cursor.getInt(0)));

				cursor.moveToNext();
			}

			String beneficiaryIds = "(";

			if (validBeneficiaries.size() > 0) {

				for (int i = 0; i < validBeneficiaries.size() - 1; i++) {
					beneficiaryIds += validBeneficiaries.get(i) + ", ";
				}

				beneficiaryIds += validBeneficiaries.get(validBeneficiaries.size() - 1);
			} else {
				beneficiaryIds += "-1";
			}

			beneficiaryIds += ")";

			query += " AND o." + SQLiteHelper.BENEFICIARY_ID + " IN " + beneficiaryIds;

			whereArgs.remove(SQLiteHelper.BENEFICIARY_TYPE);
		}

		for (Map.Entry<String, Pair<String, String>> entry : whereArgs.entrySet()) {
			query += " AND o." + entry.getKey() + " " + entry.getValue().first + " " + entry.getValue().second;
		}

		// Set set = params.entrySet();
		// Iterator iterator = set.iterator();
		// while (iterator.hasNext()) {
		// Map.Entry mentry = (Map.Entry) iterator.next();
		// Pair<String, String> item = params.get(mentry.getKey());
		// query += " AND o." + mentry.getKey() + item.first + item.second;
		//
		// }

		return query;
	}

	/**
	 * Get list of markers Objectives from database
	 *
	 * @param params
	 * @return list of ObjectiveItem
	 */
	public List<ObjectiveItem> readMapData(HashMap<String, Pair<String, String>> params) {

		List<ObjectiveItem> items = new ArrayList<ObjectiveItem>();

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// JOIN TABLES TO DISPLAY INFO FOR MAP
		String query = " SELECT o." + SQLiteHelper.ID + ", o." + SQLiteHelper.GPS + ", o." + SQLiteHelper.NAME + ", o."
				+ SQLiteHelper.STAGE_ID + ", s." + SQLiteHelper.NAME + ", o." + SQLiteHelper.PHASE_ID + ", p."
				+ SQLiteHelper.NAME + ",pov." + SQLiteHelper.PHASE_START + ",pov." + SQLiteHelper.PHASE_END + ", pov."
				+ SQLiteHelper.DAYS + " as days " + "  FROM " + SQLiteHelper.TABLE_OBJECTIVES + " as o "
				+ " INNER JOIN " + SQLiteHelper.TABLE_STAGES + " as s ON s.id = o." + SQLiteHelper.STAGE_ID
				+ " INNER JOIN " + SQLiteHelper.TABLE_PHASES + " as p ON p.id = o." + SQLiteHelper.PHASE_ID
				+ " LEFT JOIN " + SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " as pov ON (pov." + SQLiteHelper.OBJECTIVE_ID
				+ " = o." + SQLiteHelper.ID + " AND pov." + SQLiteHelper.PHASE_ID + " = p." + SQLiteHelper.ID + ")"
				+ " INNER JOIN " + SQLiteHelper.TABLE_BENEFICIARIES + " as b ON b.id = o." + SQLiteHelper.BENEFICIARY_ID
				+ " WHERE 1=1  AND pov." + SQLiteHelper.CVA_CODE + " = o." + SQLiteHelper.CVA_CODE;

		String where = getCondition(params, db);

		query += where;

		Log.d("Query:", query);

		Cursor c = db.rawQuery(query, null);
		if (c.getCount() == 0) {
			Toast.makeText(context, R.string.no_data_found, Toast.LENGTH_LONG).show();
			return items;
		}
		c.moveToFirst();
		while (!c.isAfterLast()) {

			String[] latlong = c.getString(1).split(","); // split GPS position
															// to location
															// coordinates
			double lat = Double.parseDouble(latlong[0]);
			double lng = Double.parseDouble(latlong[1]);

			// set title of the marker
			String title = c.getString(2);

			boolean expired = checkObjectiveExpired(c.getString(8));

			// set snippets values
			JSONObject snippet = new JSONObject();
			try {
				snippet.put("id", c.getString(0));

				snippet.put("stage_id", c.getString(3));
				snippet.put("stage", c.getString(4));
				snippet.put("phase_id", c.getString(5));
				snippet.put("phase", c.getString(6));
				snippet.put("expirationPhaseStart", c.getString(7));
				snippet.put("expirationPhaseEnd", c.getString(8));

				String daysVal = "0";
				if (c.getString(9) != null)
					daysVal = c.getString(9);

				snippet.put("days", daysVal);

				snippet.put("expired", expired);
			} catch (Exception e) {
				Log.d("JSON_ERR:", snippet.toString() + " => " + e.getMessage());
			}

			// Log.d("JSON_DATA:",expirationPhase + " = " + c.getString(4));

			items.add(new ObjectiveItem(lat, lng, title, snippet.toString()));

			c.moveToNext();
		}

		db.close();

		return items;
	}

	/**
	 * check current date and date of the objective if the expiration Phase is
	 * in expired or going to expire in 8 hours.
	 *
	 * @param expirationPhase
	 *            - date of the objective
	 * @return boolean
	 */
	public boolean checkObjectiveExpired(String expirationPhase) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar expPhaseDate = new GregorianCalendar();
		try {
			expPhaseDate.setTime(sdf.parse(expirationPhase));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		expPhaseDate.add(Calendar.HOUR_OF_DAY, +Constants.ALARM_START_TIME); // before
																				// alert,
																				// increment
																				// 8
																				// hours
		Date alertDate = expPhaseDate.getTime();

		// String checkDate = sdf.format(alertDate);
		// Log.d("CheckDate",checkDate);

		// if date is less then calculated Date, display alert Marker
		if (System.currentTimeMillis() > alertDate.getTime()) {
			return true;
		}

		DateUtils dateUtils = new DateUtils(); // implement Date Utils
		if (dateUtils.isToday(alertDate.getTime())
				&& expPhaseDate.get(Calendar.HOUR_OF_DAY) >= Constants.ALARM_END_TIME) // check
																						// if
																						// objective
																						// date
																						// is
																						// today
																						// and
																						// more
																						// the
																						// 20:00,
																						// display
																						// Alert
		{
			return true;
		}

		return false;
	}

	/**
	 * Method used to populate the objectives list
	 *
	 * @param cvaId
	 *            The id of the user that views the list
	 * @param offset
	 *            Offset used to paginate the results
	 * @param whereArgs
	 *            Filter "WHERE" arguments. HashMap{@literal <}Column, Pair
	 *            {@literal <}Operation, Value>
	 * @param orderArgs
	 *            Filter "GROUP BY" arguments. HashMap{@literal <}Column,
	 *            Direction(asc,desc)>
	 * @return An array of ObjectiveLite instances containing the valid
	 *         objectives
	 */
	public ArrayList<ObjectiveLite> getListObjectives(String cvaId, int offset,
			HashMap<String, Pair<String, String>> whereArgs, HashMap<String, String> orderArgs) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ArrayList<ObjectiveLite> objectives = new ArrayList<ObjectiveLite>();

		// Modified, Author: Alin

		String query = "SELECT " + "o." + SQLiteHelper.ID + ", o." + SQLiteHelper.NAME + ", o." + SQLiteHelper.PHASE_ID
				+ ", o." + SQLiteHelper.EXPIRATION_PHASE + ", o." + SQLiteHelper.AUTHORIZATION_END + ", p."
				+ SQLiteHelper.NAME + ", b." + SQLiteHelper.NAME + ", o." + SQLiteHelper.EXEC_NAME + ", o."
				+ SQLiteHelper.ADDRESS + ", o." + SQLiteHelper.REGION_ID + ", o." + SQLiteHelper.CVA_CODE + ", o."
				+ SQLiteHelper.MESER_NAME + " FROM " + SQLiteHelper.TABLE_OBJECTIVES + " o " + " INNER JOIN "
				+ SQLiteHelper.TABLE_PHASES + " p ON p." + SQLiteHelper.ID + " = o." + SQLiteHelper.PHASE_ID
				+ " INNER JOIN " + SQLiteHelper.TABLE_BENEFICIARIES + " b ON b." + SQLiteHelper.ID + " = o."
				+ SQLiteHelper.BENEFICIARY_ID + " WHERE 1=1 AND b." + SQLiteHelper.CVA_CODE + " = o."
				+ SQLiteHelper.CVA_CODE;

		if (whereArgs != null) {

			// Check if specific users are filtered by, else, use all of the
			// dva's asigned cvas
			Pair<String, String> cvaEntry = whereArgs.get(SQLiteHelper.CVA_CODE);

			if (cvaEntry != null) {
				query += " AND o." + SQLiteHelper.CVA_CODE + " " + cvaEntry.first + " " + cvaEntry.second;
			} else {

				if (cvaId != null)
					query += " AND o." + SQLiteHelper.CVA_CODE + " IN ( '" + cvaId + "' ) ";
			}

			query += getCondition(whereArgs, db);

			query += " GROUP BY o." + SQLiteHelper.ID + ", o." + SQLiteHelper.NAME + ", o. " + SQLiteHelper.PHASE_ID
					+ ", o. " + SQLiteHelper.EXPIRATION_PHASE + ", o." + SQLiteHelper.AUTHORIZATION_END + ", p."
					+ SQLiteHelper.NAME;

		}

		int mapCounter = 0;
		if (orderArgs != null) {
			query += " ORDER BY ";

			for (Map.Entry<String, String> entry : orderArgs.entrySet()) {
				if (mapCounter == orderArgs.size() - 1) {
					query += "o." + entry.getKey() + " " + entry.getValue() + " ";
				} else {
					query += "o." + entry.getKey() + " " + entry.getValue() + ", ";
				}
				mapCounter++;
			}
		}

		query += " LIMIT " + Integer.toString(QUERY_LIMIT) + " OFFSET " + Integer.toString(offset * QUERY_LIMIT);

		Log.d("DBG", "The resuling query: " + query);
		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String expPhaseDateString = cursor.getString(3);

			Calendar expPhaseDate = new GregorianCalendar();
			try {
				expPhaseDate.setTime(sdf.parse(expPhaseDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String expAuthDateString = cursor.getString(4);

			Calendar expAuthDate = new GregorianCalendar();
			try {
				expAuthDate.setTime(sdf.parse(expAuthDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			ObjectiveLite objectiveLite = new ObjectiveLite(cursor.getInt(0), cursor.getString(1), cursor.getString(5),
					cursor.getInt(2), expPhaseDate, expAuthDate);

			objectiveLite.setBeneficiaryName(cursor.getString(6));
			// Meser fields, Author: Alin
			if (cursor.getString(11).toString().trim().length() != 0) {
				objectiveLite.setConstructorName(cursor.getString(11));
			} else {
				objectiveLite.setConstructorName(cursor.getString(7));
			}
			objectiveLite.setCity(cursor.getString(8));
			objectiveLite.setRegionCode(cursor.getInt(9));
			objectiveLite.setCvaCode(cursor.getString(10));

			objectives.add(objectiveLite);

			cursor.moveToNext();
		}

		cursor.close();
		db.close();
		return objectives;
	}

	/**
	 * Gets an objective using it's unique set of coordinates
	 *
	 * @param coordinates
	 *            The coordinates used to search for the objective
	 * @return The matching Objective instance
	 */
	public Objective getObjectiveByCoords(String coordinates) {

		Objective foundObjective;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT * FROM " + SQLiteHelper.TABLE_OBJECTIVES + " b" + " WHERE b." + SQLiteHelper.GPS + " = '"
				+ coordinates + "'";

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			// Parse the date fields
			String creationDateString = cursor.getString(5);

			Calendar creationDate = new GregorianCalendar();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				creationDate.setTime(sdf.parse(creationDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String autStartDateString = cursor.getString(8);

			Calendar autStartDate = new GregorianCalendar();
			try {
				autStartDate.setTime(sdf.parse(autStartDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String autEndDateString = cursor.getString(9);

			Calendar autEndDate = new GregorianCalendar();
			try {
				autEndDate.setTime(sdf.parse(autEndDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String expPhaseDateString = cursor.getString(16);

			Calendar expPhaseDate = new GregorianCalendar();
			try {
				expPhaseDate.setTime(sdf.parse(expPhaseDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			foundObjective = new Objective(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3),
					cursor.getString(4), creationDate, cursor.getInt(6), cursor.getInt(7), autStartDate, autEndDate,
					cursor.getFloat(10), cursor.getString(11), cursor.getInt(12), cursor.getString(13),
					cursor.getInt(14), cursor.getInt(15), expPhaseDate, cursor.getInt(17));

			cursor.close();
			db.close();
			return foundObjective;
		}

		return null;
	}

	/**
	 * Gets an objective using it's unique database row id
	 *
	 * @param id
	 *            The row id used to search for the objective
	 * @param cvaCode
	 *            TODO
	 * @return The matching Objective instance
	 */
	public Objective getObjectiveById(int id, String cvaCode) {

		Objective foundObjective;

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT * FROM " + SQLiteHelper.TABLE_OBJECTIVES + " b" + " WHERE b." + SQLiteHelper.ID + " = '"
				+ Integer.toString(id) + "' " + " and b." + SQLiteHelper.CVA_CODE + " ='" + cvaCode + "' ";

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			// Parse the date fields
			String creationDateString = cursor.getString(5);

			Calendar creationDate = new GregorianCalendar();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				creationDate.setTime(sdf.parse(creationDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String autStartDateString = cursor.getString(8);

			Calendar autStartDate = new GregorianCalendar();
			try {
				autStartDate.setTime(sdf.parse(autStartDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String autEndDateString = cursor.getString(9);

			Calendar autEndDate = new GregorianCalendar();
			try {
				autEndDate.setTime(sdf.parse(autEndDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String expPhaseDateString = cursor.getString(16);

			Calendar expPhaseDate = new GregorianCalendar();
			try {
				expPhaseDate.setTime(sdf.parse(expPhaseDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			foundObjective = new Objective(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3),
					cursor.getString(4), creationDate, cursor.getInt(6), cursor.getInt(7), autStartDate, autEndDate,
					cursor.getFloat(10), cursor.getString(11), cursor.getInt(12), cursor.getString(13),
					cursor.getInt(14), cursor.getInt(15), expPhaseDate, cursor.getInt(17));

			foundObjective.setStatusId(cursor.getInt(18));
			foundObjective.setCategoryId(cursor.getInt(19));

			foundObjective.setNumeExecutant(cursor.getString(20));
			foundObjective.setCuiExecutant(cursor.getString(21));
			foundObjective.setNrcExecutant(cursor.getString(22));

			// Meserias fields Alin
			foundObjective.setNumeMeserias(cursor.getString(23));
			foundObjective.setPrenMeserias(cursor.getString(24));
			foundObjective.setTelMeserias(cursor.getString(25));
			// End Meserias fields Alin

			foundObjective.setTelBenef(cursor.getString(23));

			foundObjective.setFiliala(cursor.getString(24));

			cursor.close();
			db.close();
			return foundObjective;
		}

		return null;

	}

	/**
	 * Get the custom phase details for the specified objective
	 *
	 * @param objId
	 *            The searched objective
	 * @param phaseId
	 *            The required phase
	 * @return The start date and end date for the specified phase of the
	 *         specified objective
	 */
	public Pair<Calendar, Calendar> getCurrentObjectivePhaseDetails(int objId, int phaseId) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		String query = "SELECT pov." + SQLiteHelper.PHASE_START + ", pov." + SQLiteHelper.PHASE_END + " FROM "
				+ SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " pov" + " WHERE pov." + SQLiteHelper.OBJECTIVE_ID + " = "
				+ Integer.toString(objId) + " AND pov." + SQLiteHelper.PHASE_ID + " = " + Integer.toString(phaseId);

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {

			Calendar startDateCalendar = new GregorianCalendar();

			// Get Start Date
			try {
				startDateCalendar.setTime(sdf.parse(cursor.getString(0)));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Calendar endDateCalendar = new GregorianCalendar();

			// Get End Date
			try {
				endDateCalendar.setTime(sdf.parse(cursor.getString(1)));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Pair<Calendar, Calendar> resultPair = new Pair<Calendar, Calendar>(startDateCalendar, endDateCalendar);

			cursor.close();
			db.close();

			return resultPair;
		}

		return null;
	}

	/**
	 * Get the custom phase duration for the specified objective
	 *
	 * @param objId
	 *            The searched objective
	 * @param phaseId
	 *            The required phase
	 * @return The number of days required by the specified phase
	 */
	public int getCurrentObjectivePhaseDuration(int objId, int phaseId) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT pov." + SQLiteHelper.DAYS + " FROM " + SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " pov"
				+ " WHERE pov." + SQLiteHelper.OBJECTIVE_ID + " = " + Integer.toString(objId) + " AND pov."
				+ SQLiteHelper.PHASE_ID + " = " + Integer.toString(phaseId);

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			int result = cursor.getInt(0);
			cursor.close();
			db.close();
			return result;
		}

		return -1;
	}

	/**
	 * Adds a new objective to the database, creates a log and a new custom
	 * phase row.
	 *
	 * @param objectiveData
	 *            List containing the new Objective, Current phase duration and
	 *            start & end dates for the phase
	 * @return
	 */
	public int addObjective(List<Object> objectiveData) {
		int addedObjectiveId = -1;

		Objective objective = null;
		Calendar phaseStart = new GregorianCalendar();
		Calendar phaseEnd = new GregorianCalendar();
		int phaseDays = 0;

		for (Object obj : objectiveData) {
			if (obj instanceof Objective) {
				objective = (Objective) obj;
			}
			if (obj instanceof Integer) {
				phaseDays = (Integer) obj;
			}
			if (obj instanceof Pair) {
				Pair<?, ?> pair = (Pair<?, ?>) obj;
				phaseStart = (Calendar) pair.first;
				phaseEnd = (Calendar) pair.second;
			}
		}

		// Add objective to database
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		values.put(SQLiteHelper.ID, Utils.getIntRandom());
		values.put(SQLiteHelper.TYPE_ID, objective.getTypeId());
		values.put(SQLiteHelper.CVA_CODE, objective.getCvaCode());
		values.put(SQLiteHelper.REGION_ID, objective.getRegionID());
		values.put(SQLiteHelper.NAME, objective.getName());
		values.put(SQLiteHelper.CREATION_DATE, sdf.format(objective.getCreationDate().getTime()));
		values.put(SQLiteHelper.BENEFICIARY_ID, objective.getBeneficiaryId());
		values.put(SQLiteHelper.BENEFICIARY_TYPE, objective.getBeneficiaryType());
		values.put(SQLiteHelper.AUTHORIZATION_START, sdf.format(objective.getAuthorizationStart().getTime()));
		values.put(SQLiteHelper.AUTHORIZATION_END, sdf.format(objective.getAuthorizationEnd().getTime()));
		values.put(SQLiteHelper.ESTIMATION_VALUE, objective.getEstimationValue());
		values.put(SQLiteHelper.ADDRESS, objective.getAddress());
		values.put(SQLiteHelper.ZIP, objective.getZip());
		values.put(SQLiteHelper.GPS, objective.getGps());
		values.put(SQLiteHelper.STAGE_ID, objective.getStageId());
		values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
		values.put(SQLiteHelper.EXPIRATION_PHASE, sdf.format(objective.getExpirationPhase().getTime()));
		values.put(SQLiteHelper.STATUS, objective.getStatus());

		values.put(SQLiteHelper.STATUS_ID, objective.getStatusId());
		values.put(SQLiteHelper.CATEGORY_ID, objective.getCategoryId());

		values.put(SQLiteHelper.EXEC_NAME, objective.getNumeExecutant());
		values.put(SQLiteHelper.EXEC_CUI, objective.getCuiExecutant());
		values.put(SQLiteHelper.EXEC_NRC, objective.getNrcExecutant());

		// Meserias fields Alin
		values.put(SQLiteHelper.MESER_NAME, objective.getNumeMeserias());
		values.put(SQLiteHelper.MESER_SURNAME, objective.getPrenMeserias());
		values.put(SQLiteHelper.MESER_TEL, objective.getTelMeserias());
		// End Meserias fields Alin

		values.put(SQLiteHelper.TEL_BENEF, objective.getTelBenef());

		values.put(SQLiteHelper.FILIALA, objective.getFiliala());

		db.insert(SQLiteHelper.TABLE_OBJECTIVES, null, values);

		// Get the added objective's id by gps coordinates

		// String query = "SELECT o." + SQLiteHelper.ID + " FROM " +
		// SQLiteHelper.TABLE_OBJECTIVES + " o" + " WHERE o." + SQLiteHelper.GPS
		// + " = '"
		// + objective.getGps() + "'";

		String query = "SELECT o." + SQLiteHelper.ID + " FROM " + SQLiteHelper.TABLE_OBJECTIVES + " o" + " WHERE o."
				+ SQLiteHelper.CVA_CODE + " = '" + objective.getCvaCode() + "' and o." + SQLiteHelper.NAME + " = '"
				+ objective.getName() + "' and o." + SQLiteHelper.ADDRESS + " = '" + objective.getAddress() + "' ";

		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			addedObjectiveId = cursor.getInt(0);
		}

		objective.setId(addedObjectiveId);

		cursor.close();
		values.clear();

		values.put(SQLiteHelper.ID, Utils.getIntRandom());
		values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
		values.put(SQLiteHelper.OBJECTIVE_ID, addedObjectiveId);
		values.put(SQLiteHelper.DAYS, phaseDays);
		values.put(SQLiteHelper.PHASE_START, sdf.format(phaseStart.getTime()));
		values.put(SQLiteHelper.PHASE_END, sdf.format(phaseEnd.getTime()));
		values.put(SQLiteHelper.CVA_CODE, UserInfo.getInstance().getCod());

		db.insert(SQLiteHelper.TABLE_PHASE_OBJ_VALUES, null, values);

		values.clear();
		db.close();

		LogUtils logUtils = new LogUtils(context);
		logUtils.logObjectiveChanged(objective);

		return addedObjectiveId;
	}

	/**
	 * Edits an objective and creates a log.
	 *
	 * @param objectiveData
	 *            List containing the objective that will be edited and it's
	 *            details, the phase start date and end date, the phase duration
	 * @return The database row id of the edited objective.
	 */
	public int editObjective(List<Object> objectiveData) {
		Objective objective = null;
		Calendar phaseStart = new GregorianCalendar();
		Calendar phaseEnd = new GregorianCalendar();
		int phaseDays = 0;

		for (Object obj : objectiveData) {
			if (obj instanceof Objective) {
				objective = (Objective) obj;
			}
			if (obj instanceof Integer) {
				phaseDays = (Integer) obj;
			}
			if (obj instanceof Pair) {
				Pair<?, ?> pair = (Pair<?, ?>) obj;
				phaseStart = (Calendar) pair.first;
				phaseEnd = (Calendar) pair.second;
			}
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		objective.setExpirationPhase(phaseEnd);

		String objectivePhase = "insert or replace into " + SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " (" + SQLiteHelper.ID
				+ ", " + SQLiteHelper.OBJECTIVE_ID + ", " + SQLiteHelper.PHASE_ID + ", " + SQLiteHelper.DAYS + ", "
				+ SQLiteHelper.PHASE_START + ", " + SQLiteHelper.PHASE_END + ", " + SQLiteHelper.CVA_CODE + ") "
				+ "values ((select " + SQLiteHelper.ID + " from " + SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " where "
				+ SQLiteHelper.OBJECTIVE_ID + " = " + objective.getId() + " AND " + SQLiteHelper.PHASE_ID + " = "
				+ objective.getPhaseId() + "), " + objective.getId() + ", " + objective.getPhaseId() + ", " + phaseDays
				+ ", '" + sdf.format(phaseStart.getTime()) + "', '" + sdf.format(phaseEnd.getTime()) + "', '"
				+ UserInfo.getInstance().getCod() + "')";
		Log.d("INSERT", objectivePhase);
		db.execSQL(objectivePhase);

		// Update all objective fields
		values.clear();

		values.put(SQLiteHelper.TYPE_ID, objective.getTypeId());
		values.put(SQLiteHelper.CVA_CODE, objective.getCvaCode());
		values.put(SQLiteHelper.REGION_ID, objective.getRegionID());
		values.put(SQLiteHelper.NAME, objective.getName());
		values.put(SQLiteHelper.CREATION_DATE, sdf.format(objective.getCreationDate().getTime()));
		values.put(SQLiteHelper.BENEFICIARY_ID, objective.getBeneficiaryId());
		values.put(SQLiteHelper.BENEFICIARY_TYPE, objective.getBeneficiaryType());
		values.put(SQLiteHelper.AUTHORIZATION_START, sdf.format(objective.getAuthorizationStart().getTime()));
		values.put(SQLiteHelper.AUTHORIZATION_END, sdf.format(objective.getAuthorizationEnd().getTime()));
		values.put(SQLiteHelper.ESTIMATION_VALUE, objective.getEstimationValue());
		values.put(SQLiteHelper.ADDRESS, objective.getAddress());
		values.put(SQLiteHelper.ZIP, objective.getZip());
		values.put(SQLiteHelper.GPS, objective.getGps());
		values.put(SQLiteHelper.STAGE_ID, objective.getStageId());
		values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
		values.put(SQLiteHelper.EXPIRATION_PHASE, sdf.format(objective.getExpirationPhase().getTime()));
		values.put(SQLiteHelper.STATUS, objective.getStatus());

		values.put(SQLiteHelper.STATUS_ID, objective.getStatusId());
		values.put(SQLiteHelper.CATEGORY_ID, objective.getCategoryId());

		values.put(SQLiteHelper.EXEC_NAME, objective.getNumeExecutant());
		values.put(SQLiteHelper.EXEC_CUI, objective.getCuiExecutant());
		values.put(SQLiteHelper.EXEC_NRC, objective.getNrcExecutant());

		// Meserias fields Alin
		values.put(SQLiteHelper.MESER_NAME, objective.getNumeMeserias());
		values.put(SQLiteHelper.MESER_SURNAME, objective.getPrenMeserias());
		values.put(SQLiteHelper.MESER_TEL, objective.getTelMeserias());
		// Meserias fields Alin

		values.put(SQLiteHelper.TEL_BENEF, objective.getTelBenef());

		db.update(SQLiteHelper.TABLE_OBJECTIVES, values, SQLiteHelper.ID + " = " + Integer.toString(objective.getId()),
				null);

		db.close();
		// cursor.close();

		LogUtils logUtils = new LogUtils(context);
		logUtils.logObjectiveChanged(objective);

		return objective.getId();
	}

	/**
	 * Gets a list of all the objectives that are about to expire
	 *
	 * @param limitDate
	 *            The expiration limit
	 * @param userCode
	 *            The user for which the objectives will be provided
	 * @return A list of all the objectives that have phases expiring at the
	 *         {@code limitDate}
	 */
	public ArrayList<ObjectiveLite> getExpiringObjectives(Calendar limitDate, String userCode) {
		ArrayList<ObjectiveLite> objectives = new ArrayList<ObjectiveLite>();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT " + "o." + SQLiteHelper.ID + ", o." + SQLiteHelper.NAME + ", o." + SQLiteHelper.PHASE_ID
				+ ", o." + SQLiteHelper.EXPIRATION_PHASE + ", o." + SQLiteHelper.AUTHORIZATION_END + ", p."
				+ SQLiteHelper.NAME +

				" FROM " + SQLiteHelper.TABLE_OBJECTIVES + " o, " + SQLiteHelper.TABLE_PHASES + " p" +

				" WHERE o." + SQLiteHelper.EXPIRATION_PHASE + " <= '" + sdf.format(limitDate.getTime()) + "'"
				+ " AND o." + SQLiteHelper.CVA_CODE + " = '" + userCode + "' " + " AND p." + SQLiteHelper.ID + " = o."
				+ SQLiteHelper.PHASE_ID + " AND o." + SQLiteHelper.STATUS + " = " + Objective.ACTIVE + " ORDER BY o."
				+ SQLiteHelper.EXPIRATION_PHASE + " ASC";

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {

			while (!cursor.isAfterLast()) {

				String expPhaseDateString = cursor.getString(3);

				Calendar expPhaseDate = new GregorianCalendar();
				try {
					expPhaseDate.setTime(sdf.parse(expPhaseDateString));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				String expAuthDateString = cursor.getString(4);

				Calendar expAuthDate = new GregorianCalendar();
				try {
					expAuthDate.setTime(sdf.parse(expAuthDateString));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				objectives.add(new ObjectiveLite(cursor.getInt(0), cursor.getString(1), cursor.getString(5),
						cursor.getInt(2), expPhaseDate, expAuthDate));

				cursor.moveToNext();
			}
		} else {
			cursor.close();
			db.close();
			return objectives;
		}

		cursor.close();
		db.close();
		return objectives;
	}

	// Solved bug where application would stop if phase date is modified on the
	// map
	// Added: 06.06.2016, Author: Alin
	private void sendLocalDataToServer() {
		if (UserInfo.getInstance().getTipUser().equals("CV")) {
			OperatiiTabele operatiiTabele = new OperatiiTabele();
			operatiiTabele.salveazaTabelaObiective(context);
		}
	}

	/**
	 * //TODO @Filip Documentation required
	 * 
	 * @param objectiveId
	 * @param selectedStageId
	 * @param selectedPhaseId
	 * @param expirationStartDate
	 * @param expirationEndDate
	 * @param days
	 */
	public void setPhaseChanges(int objectiveId, int selectedStageId, int selectedPhaseId, String expirationStartDate,
			String expirationEndDate, int days) {

		if (objectiveId > 0) {

			// update
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String objectiveQuery = "UPDATE " + SQLiteHelper.TABLE_OBJECTIVES + " SET " + SQLiteHelper.EXPIRATION_PHASE
					+ " = '" + expirationEndDate + "', " + dbHelper.PHASE_ID + "=" + selectedPhaseId + ", "
					+ SQLiteHelper.STAGE_ID + "=" + selectedStageId + "  WHERE id=" + objectiveId;
			db.execSQL(objectiveQuery);

			/*
			 * *****************************************************************
			 * ****************************************** Fixed error where the
			 * modified data from the map objective would not be saved and
			 * crashing the application. Added CVA_CODE to fix this issue. Added
			 * setLocalDataToServer() method in order to store the new data
			 * inside the database.
			 * *****************************************************************
			 * ******************************************
			 * 
			 * Update 06.06.2016, Author: Alin
			 */
			try {
				String objectivePhase = "insert or replace into " + SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " ("
						+ SQLiteHelper.ID + ", " + SQLiteHelper.OBJECTIVE_ID + ", " + SQLiteHelper.PHASE_ID + ", "
						+ SQLiteHelper.DAYS + ", " + SQLiteHelper.PHASE_START + ", " + SQLiteHelper.PHASE_END + ", "
						+ SQLiteHelper.CVA_CODE + ") " + "values ((select " + SQLiteHelper.ID + " from "
						+ SQLiteHelper.TABLE_PHASE_OBJ_VALUES + " where " + SQLiteHelper.OBJECTIVE_ID + " = "
						+ objectiveId + " AND " + SQLiteHelper.PHASE_ID + " = " + selectedPhaseId + "), " + objectiveId
						+ ", " + selectedPhaseId + ", " + days + ", '" + expirationStartDate + "', '"
						+ expirationEndDate + "', '" + UserInfo.getInstance().getCod() + "')";
				Log.d("INSERT", objectivePhase);
				db.execSQL(objectivePhase);
				sendLocalDataToServer();
			} catch (Exception e) {
				for (int i = 0; i < 4; i++) {
					Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
				}
			}
			//End update
			
			// write to log current settings before those should be changed
			Objective objective = getObjectiveById(objectiveId, UserInfo.getInstance().getCod());
			String NEW_LINE = System.getProperty("line.separator");
			StringBuilder phaseValues = new StringBuilder();
			phaseValues.append("Phase details:" + NEW_LINE);
			phaseValues.append("Duration = " + days + " days" + NEW_LINE);
			phaseValues.append("Phase start date = " + expirationStartDate + NEW_LINE);

			objective.setPhaseValues(phaseValues.toString());

			LogUtils log = new LogUtils(context);
			log.logObjectiveChanged(objective);

		} else {
			Log.d("ObjectiveErr", "Obiectiv GOL");
		}

	}

	/**
	 * Commits the changes to the specified objective and then changes it's
	 * status to inactive (archived)
	 *
	 * @param objectiveData
	 *            List containing the objective to be archived, phase duration
	 *            and phase start / end dates
	 */
	public void editAndArchive(List<Object> objectiveData) {

		Objective objective = null;
		Calendar phaseStart = new GregorianCalendar();
		Calendar phaseEnd = new GregorianCalendar();
		long phaseDays = 0;

		for (Object obj : objectiveData) {
			if (obj instanceof Objective) {
				objective = (Objective) obj;
			}
			if (obj instanceof Long) {
				phaseDays = (Long) obj;
			}
			if (obj instanceof Pair) {
				Pair<?, ?> pair = (Pair<?, ?>) obj;
				phaseStart = (Calendar) pair.first;
				phaseEnd = (Calendar) pair.second;
			}
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		// Get current phase from db
		int currentDbPhase = -1;
		String query = "SELECT o." + SQLiteHelper.PHASE_ID + " FROM " + SQLiteHelper.TABLE_OBJECTIVES + " o"
				+ " WHERE o." + SQLiteHelper.ID + " = " + Integer.toString(objective.getId());

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			currentDbPhase = cursor.getInt(0);
		}

		if (currentDbPhase != objective.getPhaseId()) {

			values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
			values.put(SQLiteHelper.OBJECTIVE_ID, objective.getId());
			values.put(SQLiteHelper.DAYS, phaseDays);
			values.put(SQLiteHelper.PHASE_START, sdf.format(phaseStart.getTime()));
			values.put(SQLiteHelper.PHASE_END, sdf.format(phaseEnd.getTime()));
			values.put(SQLiteHelper.CVA_CODE, UserInfo.getInstance().getCod());

			db.insert(SQLiteHelper.TABLE_PHASE_OBJ_VALUES, null, values);
		}

		// Update all objective fields
		values.clear();

		objective.setStatus(Objective.INACTIVE);

		values.put(SQLiteHelper.TYPE_ID, objective.getTypeId());
		values.put(SQLiteHelper.CVA_CODE, objective.getCvaCode());
		values.put(SQLiteHelper.REGION_ID, objective.getRegionID());
		values.put(SQLiteHelper.NAME, objective.getName());
		values.put(SQLiteHelper.CREATION_DATE, sdf.format(objective.getCreationDate().getTime()));
		values.put(SQLiteHelper.BENEFICIARY_ID, objective.getBeneficiaryId());
		values.put(SQLiteHelper.BENEFICIARY_TYPE, objective.getBeneficiaryType());
		values.put(SQLiteHelper.AUTHORIZATION_START, sdf.format(objective.getAuthorizationStart().getTime()));
		values.put(SQLiteHelper.AUTHORIZATION_END, sdf.format(objective.getAuthorizationEnd().getTime()));
		values.put(SQLiteHelper.ESTIMATION_VALUE, objective.getEstimationValue());
		values.put(SQLiteHelper.ADDRESS, objective.getAddress());
		values.put(SQLiteHelper.ZIP, objective.getZip());
		values.put(SQLiteHelper.GPS, objective.getGps());
		values.put(SQLiteHelper.STAGE_ID, objective.getStageId());
		values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
		values.put(SQLiteHelper.EXPIRATION_PHASE, sdf.format(objective.getExpirationPhase().getTime()));
		values.put(SQLiteHelper.STATUS, objective.getStatus());

		db.update(SQLiteHelper.TABLE_OBJECTIVES, values, SQLiteHelper.ID + " = " + Integer.toString(objective.getId()),
				null);

		LogUtils logUtils = new LogUtils(context);
		logUtils.logObjectiveChanged(objective);

		db.close();
		cursor.close();
	}

	/**
	 * Used to populate objective name AutoCompleteTextView suggestions
	 *
	 * @param cvaCode
	 *            The current CVA code for which objective names are searched
	 * @param searchedName
	 *            The search string used to filter the results
	 * @param mode
	 *            Active or inactive objects
	 * @return List of compatible objective names
	 */
	public ArrayList<String> getObjectiveNames(String cvaCode, String searchedName, int mode) {
		ArrayList<String> results = new ArrayList<String>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT o." + SQLiteHelper.NAME + " FROM " + SQLiteHelper.TABLE_OBJECTIVES + " o" + " WHERE o."
				+ SQLiteHelper.CVA_CODE + " IN (" + cvaCode + ")" + " AND o." + SQLiteHelper.NAME + " LIKE '%"
				+ searchedName + "%'" + " AND o." + SQLiteHelper.STATUS + " = " + Integer.toString(mode);

		Cursor cursor = db.rawQuery(query, null);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				results.add(cursor.getString(0));
				cursor.moveToNext();
			}

			cursor.close();
			db.close();

			return results;
		}

		cursor.close();
		db.close();
		return results;
	}
}
