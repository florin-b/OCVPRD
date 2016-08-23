package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.utils.Utils;
import com.stimasoft.obiectivecva.models.db_classes.Beneficiary;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for handling beneficiary database operations
 */
public class BeneficiaryData {

	private SQLiteHelper dbHelper;
	private Context context;

	public BeneficiaryData(Context context) {
		this.context = context;
		this.dbHelper = new SQLiteHelper(context);
	}

	/**
	 * Adds a new beneficiary to the database
	 *
	 * @param beneficiary
	 *            The beneficiary that is to be added
	 * @return The added beneficiary complete with database row id
	 */
	public Beneficiary addBeneficiary(Beneficiary beneficiary) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(SQLiteHelper.ID, Utils.getIntRandom());
		values.put(SQLiteHelper.REGION_ID, beneficiary.getRegionID());
		values.put(SQLiteHelper.NAME, beneficiary.getName());
		values.put(SQLiteHelper.TYPE, beneficiary.getType());
		values.put(SQLiteHelper.CUI, beneficiary.getCui());
		values.put(SQLiteHelper.NR_RC, beneficiary.getNrRc());
		values.put(SQLiteHelper.CNP, beneficiary.getCnp());
		values.put(SQLiteHelper.STATUS, beneficiary.getStatus());
		values.put(SQLiteHelper.CVA_CODE, UserInfo.getInstance().getCod());

		long result = db.insert(SQLiteHelper.TABLE_BENEFICIARIES, null, values);

		Log.d("DBG", "Am primit result de la add de user: " + Long.toString(result));

		String query = "SELECT * FROM " + SQLiteHelper.TABLE_BENEFICIARIES + " b" + " WHERE b." + SQLiteHelper.NAME + " = '" + beneficiary.getName() + "'"
				+ " AND b." + SQLiteHelper.TYPE + " = " + beneficiary.getType() + " AND b." + SQLiteHelper.CUI + " = '" + beneficiary.getCui() + "'" + " AND b."
				+ SQLiteHelper.NR_RC + " = '" + beneficiary.getNr_rc() + "'" + " AND b." + SQLiteHelper.CNP + " = '" + beneficiary.getCnp() + "'" + " AND b."
				+ SQLiteHelper.STATUS + " = " + beneficiary.getStatus();

		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();

		Beneficiary addedBeneficiary;

		if (cursor.getCount() > 0) {
			addedBeneficiary = new Beneficiary(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getInt(6));

			
		} else {
			cursor.close();
			db.close();
			return null;
		}

		cursor.close();
		db.close();

		return addedBeneficiary;
	}

	/**
	 * Method used to populate beneficiary AutoCompleteTextView suggestions.
	 *
	 * @param name
	 *            The name with which the database will be searched
	 * @param type
	 *            The beneficiary type
	 * @return An array containing all the (BeneficiaryId, BeneficiaryName)
	 *         pairs that match the query
	 */
	public ArrayList<Pair<Integer, String>> getBeneficiaryAC(String name, int type) {
		ArrayList<Pair<Integer, String>> possibleBeneficiaries = new ArrayList<Pair<Integer, String>>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT b." + SQLiteHelper.ID + ", b." + SQLiteHelper.NAME + " FROM " + SQLiteHelper.TABLE_BENEFICIARIES + " b" + " WHERE 1 = 1"
				+ " AND b." + SQLiteHelper.NAME + " LIKE '%" + name + "%'";

		if (type != -1) {
			query += " AND b." + SQLiteHelper.TYPE + " = " + Integer.toString(type);
		}

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			possibleBeneficiaries.add(new Pair<Integer, String>(cursor.getInt(0), cursor.getString(1)));

			cursor.moveToNext();
		}

		cursor.close();
		db.close();

		return possibleBeneficiaries;
	}

	/**
	 * Searches the database for the beneficiary with the provided row id
	 *
	 * @param id
	 *            The row id that is used for the database query
	 * @return The beneficiary that has the provided Id
	 */
	public Beneficiary getBeneficiaryById(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Beneficiary beneficiary = null;

		String query = "SELECT * FROM " + SQLiteHelper.TABLE_BENEFICIARIES + " b" + " WHERE b." + SQLiteHelper.ID + " = " + Integer.toString(id);

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			beneficiary = new Beneficiary(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5),
					cursor.getString(6), cursor.getInt(7));

			

			cursor.moveToNext();
		}

		cursor.close();
		db.close();

		return beneficiary;
	}

	/**
	 * Checks if a certain beneficiary exists
	 *
	 * @param whereArgs
	 *            Key-Value pairs that define the (column = value) query "WHERE"
	 *            arguments
	 * @return The id of the matching beneficiary or -1 if no beneficiary was
	 *         found
	 */
	public int beneficiaryExists(HashMap<String, String> whereArgs) {
		int benefId = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "SELECT b." + SQLiteHelper.ID + " FROM " + SQLiteHelper.TABLE_BENEFICIARIES + " b" + " WHERE 1 = 1";

		for (Map.Entry<String, String> entry : whereArgs.entrySet()) {
			query += " AND b." + entry.getKey() + " = '" + entry.getValue() + "'";
		}

		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			benefId = cursor.getInt(0);
			cursor.close();
			db.close();
			return benefId;
		}

		cursor.close();
		db.close();
		return benefId;
	}
}
