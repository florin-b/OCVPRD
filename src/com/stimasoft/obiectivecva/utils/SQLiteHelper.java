package com.stimasoft.obiectivecva.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.arabesque.obiectivecva.ObjectivePhase;
import com.arabesque.obiectivecva.utils.Utils;
import com.stimasoft.obiectivecva.models.db_classes.Beneficiary;
import com.stimasoft.obiectivecva.models.db_classes.Branch;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_classes.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class used to generate the default database and populate it.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "CVA_DATABASE";

	// Common
	public static final String FILIALA = "filiala";

	public static final String ID = "ID"; // All tables use this
	public static final String NAME = "name"; // Objectives & Beneficiaries &
												// Stages & Phases
	public static final String STATUS = "status"; // Objectives & Beneficiaries
	public static final String HIERARCHY = "hierarchy"; // Stages & Phases
	public static final String CREATION_DATE = "creation_date"; // Objectives &
																// Change_Logs
	public static final String EXPIRATION_PHASE = "expiration_phase"; // Objectives
																		// &
																		// Change_Logs
	public static final String STAGE_ID = "stage_id"; // Objectives &
														// Change_Logs
	public static final String PHASE_ID = "phase_id"; // Objectives &
														// Change_Logs
	public static final String OBJECTIVE_ID = "objective_id"; // PhaseObjValues
																// & Change_Logs
	public static final String CODE = "code"; // Users & Regions
	public static final String BRANCH_CODE = "branch_code"; // Objectives &
															// Users
	public static final String REGION_ID = "region_id";

	// Objectives table
	public static final String TABLE_OBJECTIVES = "OBJECTIVES";
	public static final String TYPE_ID = "type_id";
	public static final String CVA_CODE = "cva_code";
	public static final String BENEFICIARY_ID = "beneficiary_id";
	public static final String BENEFICIARY_TYPE = "beneficiary_type";
	public static final String AUTHORIZATION_START = "authorization_start";
	public static final String AUTHORIZATION_END = "authorization_end";
	public static final String ESTIMATION_VALUE = "estimation_value";
	public static final String ADDRESS = "address";
	public static final String ZIP = "zip";
	public static final String GPS = "gps";

	public static final String STATUS_ID = "status_id";
	public static final String CATEGORY_ID = "category_id";

	public static final String EXEC_NAME = "exec_name";
	public static final String EXEC_CUI = "exec_cui";
	public static final String EXEC_NRC = "exec_nrc";

	// Meserias fields Alin
	public static final String MESER_NAME = "meser_name";
	public static final String MESER_SURNAME = "meser_surname";
	public static final String MESER_TEL = "meser_tel";
	// End Meserias fields Alin

	public static final String TEL_BENEF = "tel_benef";

	// Beneficiaries table
	public static final String TABLE_BENEFICIARIES = "BENEFICIARIES";

	public static final String BENEFICIARY_NAME = "beneficiary_name";
	public static final String TYPE = "type";
	public static final String CUI = "cui";
	public static final String NR_RC = "nr_rc";
	public static final String CNP = "cnp";

	// Stages table
	public static final String TABLE_STAGES = "STAGES";

	// Change Logs table
	public static final String TABLE_CHANGE_LOGS = "CHANGE_LOGS";

	public static final String DETAILS = "details";

	// Phases table
	public static final String TABLE_PHASES = "PHASES";

	public static final String DAYS = "days";

	// Phase Objective Values table
	public static final String TABLE_PHASE_OBJ_VALUES = "PHASE_OBJECTIVE_VALUES";
	public static final String PHASE_START = "phase_start";
	public static final String PHASE_END = "phase_end";
	// Regions table
	public static final String TABLE_REGIONS = "REGIONS";

	// Users table
	public static final String TABLE_USERS = "USERS";

	public static final String SURNAME = "surname";

	// Branches table
	public static final String TABLE_BRANCHES = "BRANCHES";

	public static final String REGION_CODE = "region_code";

	private Context context;

	///////////////////////////////////////////////////////

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createDB(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_OBJECTIVES);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BENEFICIARIES);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANGE_LOGS);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PHASES);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_STAGES);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PHASE_OBJ_VALUES);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_REGIONS);

		this.onCreate(sqLiteDatabase);
	}

	/**
	 * Creates the database tables and defines their relations
	 *
	 * @param db
	 *            SQLite database reference
	 */
	void createDB(SQLiteDatabase db) {
		// SQL statement to create user table
		String CREATE_OBJECTIVES_TABLE = "CREATE TABLE " + TABLE_OBJECTIVES + " ( " + ID + " INTEGER  , " + TYPE_ID + " INTEGER NOT NULL, " + CVA_CODE
				+ " TEXT NOT NULL, " + REGION_ID + " INTEGER NOT NULL, " + NAME + " TEXT NOT NULL, " + CREATION_DATE + " TEXT NOT NULL, " + BENEFICIARY_ID
				+ " INTEGER NOT NULL, " + BENEFICIARY_TYPE + " INTEGER NOT NULL, " + AUTHORIZATION_START + " TEXT NOT NULL, " + AUTHORIZATION_END
				+ " TEXT NOT NULL, " + ESTIMATION_VALUE + " REAL, " + ADDRESS + " TEXT, " + ZIP + " INTEGER, " + GPS + " TEXT , " + STAGE_ID
				+ " INTEGER NOT NULL, " + PHASE_ID + " INTEGER NOT NULL, " + EXPIRATION_PHASE + " TEXT NOT NULL, " + STATUS + " TEXT, " + STATUS_ID
				+ " INTEGER NOT NULL, " + CATEGORY_ID + " INTEGER NOT NULL, " + EXEC_NAME + " TEXT NOT NULL, " + EXEC_CUI + " TEXT NOT NULL, " + EXEC_NRC
				+ " TEXT NOT NULL, " + MESER_NAME + " TEXT NOT NULL, " + MESER_SURNAME + " TEXT NOT NULL, " + MESER_TEL + " TEXT NOT NULL, " + TEL_BENEF
				+ " TEXT NOT NULL, " + FILIALA + " TEXT NOT NULL, " + " FOREIGN KEY (" + BENEFICIARY_ID + ") REFERENCES " + TABLE_BENEFICIARIES + " (" + ID
				+ "), " + " FOREIGN KEY (" + REGION_ID + ") REFERENCES " + TABLE_REGIONS + " (" + CODE + "), " + " FOREIGN KEY (" + STAGE_ID + ") REFERENCES "
				+ TABLE_STAGES + " (" + ID + "), " + " FOREIGN KEY (" + PHASE_ID + ") REFERENCES " + TABLE_PHASES + " (" + ID
				+ "), PRIMARY KEY (ID, GPS, CVA_CODE) )"; // Added: MESER_NAME,
															// MESER_SURNAME,
															// MESER_TEL,
															// Author: Alin

		db.execSQL(CREATE_OBJECTIVES_TABLE);

		String CREATE_BENEFICIARIES_TABLE = "CREATE TABLE " + TABLE_BENEFICIARIES + " ( " + ID + " INTEGER , " + REGION_ID + " INTEGER NOT NULL, " + NAME
				+ " TEXT NOT NULL, " + TYPE + " INTEGER, " + CUI + " TEXT, " + NR_RC + " TEXT, " + CNP + " INTEGER, " + STATUS + " TEXT, " + CVA_CODE
				+ " TEXT NOT NULL, PRIMARY KEY (ID, NAME) )";

		db.execSQL(CREATE_BENEFICIARIES_TABLE);

		String CREATE_STAGES_TABLE = "CREATE TABLE " + TABLE_STAGES + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + NAME + " TEXT NOT NULL, "
				+ HIERARCHY + " INTEGER NOT NULL, " + STATUS + " TEXT )";

		db.execSQL(CREATE_STAGES_TABLE);

		String CREATE_CHANGE_LOGS_TABLE = "CREATE TABLE " + TABLE_CHANGE_LOGS + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + CREATION_DATE
				+ " TEXT NOT NULL, " + OBJECTIVE_ID + " INTEGER NOT NULL, " + EXPIRATION_PHASE + " TEXT, " + STAGE_ID + " INTEGER, " + PHASE_ID + " INTEGER, "
				+ DETAILS + " TEXT, " + " FOREIGN KEY (" + OBJECTIVE_ID + ") REFERENCES " + TABLE_OBJECTIVES + " (" + ID + "), " + " FOREIGN KEY (" + STAGE_ID
				+ ") REFERENCES " + TABLE_STAGES + " (" + ID + "), " + " FOREIGN KEY (" + PHASE_ID + ") REFERENCES " + TABLE_PHASES + " (" + ID + ") )";

		db.execSQL(CREATE_CHANGE_LOGS_TABLE);

		String CREATE_PHASES_TABLE = "CREATE TABLE " + TABLE_PHASES + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + STAGE_ID + " TEXT, " + NAME
				+ " INTEGER NOT NULL, " + DAYS + " TEXT NOT NULL, " + HIERARCHY + " INTEGER NOT NULL, " + STATUS + " INTEGER, " + " FOREIGN KEY (" + STAGE_ID
				+ ") REFERENCES " + TABLE_STAGES + " (" + ID + ") )";

		db.execSQL(CREATE_PHASES_TABLE);

		String CREATE_PHASE_OBJ_VAL_TABLE = "CREATE TABLE " + TABLE_PHASE_OBJ_VALUES + " ( " + ID + " INTEGER  , " + PHASE_ID + " INTEGER, " + OBJECTIVE_ID
				+ " INTEGER, " + DAYS + " INTEGER, " + PHASE_START + " TEXT NOT NULL, " + PHASE_END + " TEXT NOT NULL, " + CVA_CODE + " TEXT NOT NULL, "
				+ " UNIQUE (" + PHASE_ID + ", " + OBJECTIVE_ID + ", " + CVA_CODE + "), " + " FOREIGN KEY (" + PHASE_ID + ") REFERENCES " + TABLE_PHASES + " ("
				+ ID + "), " + " FOREIGN KEY (" + OBJECTIVE_ID + ") REFERENCES " + TABLE_OBJECTIVES + " (" + ID + "), PRIMARY KEY (ID, CVA_CODE) )";

		db.execSQL(CREATE_PHASE_OBJ_VAL_TABLE);

		String CREATE_REGIONS_TABLE = "CREATE TABLE " + TABLE_REGIONS + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + NAME + " TEXT NOT NULL, "
				+ CODE + " TEXT NOT NULL ," + GPS + " TEXT)";

		db.execSQL((CREATE_REGIONS_TABLE));

		String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + CODE + " TEXT NOT NULL, "
				+ TYPE_ID + " INTEGER NOT NULL, " + BRANCH_CODE + " TEXT NOT NULL, " + NAME + " TEXT NOT NULL, " + SURNAME + " TEXT NOT NULL )";

		db.execSQL(CREATE_USERS_TABLE);

		String CREATE_TABLE_BRANCHES = "CREATE TABLE " + TABLE_BRANCHES + " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + NAME
				+ " TEXT NOT NULL, " + REGION_CODE + " TEXT NOT NULL )";

		db.execSQL(CREATE_TABLE_BRANCHES);

		populateDB(db);

	}

	/**
	 * Populates the database with mock data
	 *
	 * @param db
	 *            SQLite database reference
	 */
	void populateDB(SQLiteDatabase db) {

		populateStages(db);

		populatePhases(db);

		// populateBeneficiaries(db);

		// populateObjectives(db);

		populateRegions(db);

		populateUsers(db);

		populateBranches(db);

	}

	public void clearLocalTables() {

		SQLiteDatabase db = this.getReadableDatabase();

		db.execSQL("DELETE FROM " + TABLE_OBJECTIVES);

		db.execSQL("DELETE FROM " + TABLE_BENEFICIARIES);

		db.execSQL("DELETE FROM " + TABLE_PHASE_OBJ_VALUES);

		db.close();

	}

	// Obsolete
	public void addObjective(Objective objective) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(ID, Utils.getIntRandom());
		values.put(TYPE_ID, objective.getTypeId());
		values.put(REGION_ID, objective.getRegionID());
		values.put(CVA_CODE, objective.getCvaCode());
		values.put(NAME, objective.getName());

		String creationDate = objective.getCreationDateInfo(Calendar.YEAR) + "-" + (objective.getCreationDateInfo(Calendar.MONTH) + 1) + "-"
				+ objective.getCreationDateInfo(Calendar.DAY_OF_MONTH) + " "
				+ String.format("%02d:%02d", objective.getCreationDateInfo(Calendar.HOUR_OF_DAY), objective.getCreationDateInfo(Calendar.MINUTE));

		values.put(CREATION_DATE, creationDate);
		values.put(BENEFICIARY_ID, objective.getBeneficiaryId());
		values.put(BENEFICIARY_TYPE, objective.getBeneficiaryType());

		String autStart = objective.getAuthorizationStartInfo(Calendar.YEAR) + "-" + (objective.getAuthorizationStartInfo(Calendar.MONTH) + 1) + "-"
				+ objective.getAuthorizationStartInfo(Calendar.DAY_OF_MONTH) + " "
				+ String.format("%02d:%02d", objective.getAuthorizationStartInfo(Calendar.HOUR_OF_DAY), objective.getAuthorizationStartInfo(Calendar.MINUTE));

		values.put(AUTHORIZATION_START, autStart);

		String autEnd = objective.getAuthorizationEndInfo(Calendar.YEAR) + "-" + (objective.getAuthorizationEndInfo(Calendar.MONTH) + 1) + "-"
				+ objective.getAuthorizationEndInfo(Calendar.DAY_OF_MONTH) + " "
				+ String.format("%02d:%02d", objective.getAuthorizationEndInfo(Calendar.HOUR_OF_DAY), objective.getAuthorizationEndInfo(Calendar.MINUTE));

		values.put(AUTHORIZATION_END, autEnd);

		values.put(ESTIMATION_VALUE, objective.getEstimationValue());
		values.put(ADDRESS, objective.getAddress());
		values.put(ZIP, objective.getZip());
		values.put(GPS, objective.getGps());
		values.put(STAGE_ID, objective.getStageId());
		values.put(PHASE_ID, objective.getPhaseId());

		String expPhase = objective.getExpirationPhaseInfo(Calendar.YEAR) + "-" + (objective.getExpirationPhaseInfo(Calendar.MONTH) + 1) + "-"
				+ objective.getExpirationPhaseInfo(Calendar.DAY_OF_MONTH) + " "
				+ String.format("%02d:%02d", objective.getExpirationPhaseInfo(Calendar.HOUR_OF_DAY), objective.getExpirationPhaseInfo(Calendar.MINUTE));

		values.put(EXPIRATION_PHASE, expPhase);
		values.put(STATUS, objective.getStatus());

		values.put(STATUS_ID, objective.getStatusId());
		values.put(CATEGORY_ID, objective.getCategoryId());

		db.insert(TABLE_OBJECTIVES, null, values);

		db.close();
	}

	public ArrayList<Objective> getAllObjectives() {
		ArrayList<Objective> objectives = new ArrayList<Objective>();

		SQLiteDatabase db = this.getReadableDatabase();

		String queryObjectives = "SELECT * " + "FROM " + TABLE_OBJECTIVES;

		Cursor cursorObjectives = db.rawQuery(queryObjectives, null);

		cursorObjectives.moveToFirst();

		while (!cursorObjectives.isAfterLast()) {

			// Parse the date fields
			String creationDateString = cursorObjectives.getString(5);

			Calendar creationDate = new GregorianCalendar();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				creationDate.setTime(sdf.parse(creationDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String autStartDateString = cursorObjectives.getString(8);

			Calendar autStartDate = new GregorianCalendar();
			try {
				autStartDate.setTime(sdf.parse(autStartDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String autEndDateString = cursorObjectives.getString(9);

			Calendar autEndDate = new GregorianCalendar();
			try {
				autEndDate.setTime(sdf.parse(autEndDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String expPhaseDateString = cursorObjectives.getString(16);

			Calendar expPhaseDate = new GregorianCalendar();
			try {
				expPhaseDate.setTime(sdf.parse(expPhaseDateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Objective objective = new Objective(cursorObjectives.getInt(0), cursorObjectives.getInt(1), cursorObjectives.getString(2),
					cursorObjectives.getInt(3), cursorObjectives.getString(4), creationDate, cursorObjectives.getInt(6), cursorObjectives.getInt(7),
					autStartDate, autEndDate, cursorObjectives.getFloat(10), cursorObjectives.getString(11), cursorObjectives.getInt(12),
					cursorObjectives.getString(13), cursorObjectives.getInt(14), cursorObjectives.getInt(15), expPhaseDate, cursorObjectives.getInt(17));

			objective.setStatusId(cursorObjectives.getInt(18));
			objective.setCategoryId(cursorObjectives.getInt(19));

			objective.setNumeExecutant(cursorObjectives.getString(20));
			objective.setCuiExecutant(cursorObjectives.getString(21));
			objective.setNrcExecutant(cursorObjectives.getString(22));

			// Meserias fields Alin
			objective.setNumeMeserias(cursorObjectives.getString(23));
			objective.setPrenMeserias(cursorObjectives.getString(24));
			objective.setTelMeserias(cursorObjectives.getString(25));
			// End Meserias fields Alin

			objective.setTelBenef(cursorObjectives.getString(23));

			objective.setFiliala(cursorObjectives.getString(24));

			objectives.add(objective);

			cursorObjectives.moveToNext();
		}

		cursorObjectives.close();
		db.close();

		return objectives;
	}

	public ArrayList<ObjectivePhase> getAllObjectivesPhases() {

		ArrayList<ObjectivePhase> phases = new ArrayList<ObjectivePhase>();

		SQLiteDatabase db = this.getReadableDatabase();

		String sqlString = "select id, phase_id, objective_id, days, phase_start, phase_end from " + TABLE_PHASE_OBJ_VALUES;

		Cursor cursorPhases = db.rawQuery(sqlString, null);

		cursorPhases.moveToFirst();

		while (!cursorPhases.isAfterLast()) {

			phases.add(new ObjectivePhase(cursorPhases.getInt(0), cursorPhases.getInt(1), cursorPhases.getInt(2), cursorPhases.getInt(3),
					cursorPhases.getString(4), cursorPhases.getString(5)));

			cursorPhases.moveToNext();

		}

		return phases;

	}

	public ArrayList<Phase> getAllPhases() {

		ArrayList<Phase> phases = new ArrayList<Phase>();

		SQLiteDatabase db = this.getReadableDatabase();

		String queryPhases = "SELECT  ID, STAGE_ID, NAME, DAYS, HIERARCHY, STATUS, STAGE_ID " + "FROM " + TABLE_PHASES;

		Cursor cursorPhases = db.rawQuery(queryPhases, null);

		cursorPhases.moveToFirst();

		while (!cursorPhases.isAfterLast()) {

			phases.add(new Phase(cursorPhases.getInt(0), cursorPhases.getInt(1), cursorPhases.getString(2), cursorPhases.getInt(3), cursorPhases.getInt(4),
					cursorPhases.getInt(5)));

			cursorPhases.moveToNext();

		}

		return phases;
	}

	public ArrayList<Beneficiary> getAllBeneficiaries() {

		ArrayList<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		SQLiteDatabase db = this.getReadableDatabase();

		String queryBeneficiaries = "SELECT  ID, REGION_ID, NAME, TYPE, CUI, NR_RC, CNP, STATUS  " + "FROM " + TABLE_BENEFICIARIES;

		Cursor cursorBeneficiaries = db.rawQuery(queryBeneficiaries, null);

		cursorBeneficiaries.moveToFirst();

		while (!cursorBeneficiaries.isAfterLast()) {

			Beneficiary beneficiary = new Beneficiary(cursorBeneficiaries.getInt(0), cursorBeneficiaries.getInt(1), cursorBeneficiaries.getString(2),
					cursorBeneficiaries.getInt(3), cursorBeneficiaries.getString(4), cursorBeneficiaries.getString(5), cursorBeneficiaries.getString(6),
					cursorBeneficiaries.getInt(7));

			beneficiaries.add(beneficiary);

			cursorBeneficiaries.moveToNext();

		}

		cursorBeneficiaries.close();
		db.close();

		return beneficiaries;
	}

	void populateStages(SQLiteDatabase db) {
		ContentValues values;

		// Add Stages
		ArrayList<Stage> stages = new ArrayList<Stage>();

		stages.add(new Stage("Structura de rezistenta", 1));
		stages.add(new Stage("Instalatii", 2));
		stages.add(new Stage("Finisaje interioare", 3));
		stages.add(new Stage("Finisaje exterioare", 4));

		for (Stage stage : stages) {
			values = new ContentValues();

			values.put(NAME, stage.getName());
			values.put(HIERARCHY, stage.getHierarchy());
			values.put(STATUS, stage.getStatus());

			db.insert(TABLE_STAGES, null, values);
		}

	}

	void populatePhases(SQLiteDatabase db) {
		ContentValues values;

		ArrayList<Phase> phases = new ArrayList<Phase>();

		phases.add(new Phase("Fundatie", 7, 1, 1));
		phases.add(new Phase("Elevatie", 7, 2, 1));
		phases.add(new Phase("Placa nivel zero", 4, 3, 1));
		phases.add(new Phase("Zidarie parter", 5, 4, 1));
		phases.add(new Phase("Placa peste parter", 10, 5, 1));
		phases.add(new Phase("Zidarie etaj", 5, 6, 1));
		phases.add(new Phase("Placa peste etaj", 10, 7, 1));
		phases.add(new Phase("Mansarda", 4, 8, 1));
		phases.add(new Phase("Acoperis", 7, 9, 1));

		phases.add(new Phase("Sanitare", 17, 1, 2));
		phases.add(new Phase("Electrice", 14, 2, 2));
		phases.add(new Phase("Incalzire", 14, 3, 2));

		phases.add(new Phase("Pardoseli", 7, 1, 3));
		phases.add(new Phase("Pereti", 14, 2, 3));
		phases.add(new Phase("Tavane", 14, 3, 3));
		phases.add(new Phase("Tamplarie interioara", 3, 4, 3));

		phases.add(new Phase("Fatada", 7, 1, 4));
		phases.add(new Phase("Tamplarie", 3, 2, 4));
		phases.add(new Phase("Amenajari exterioare", 14, 3, 4));

		for (Phase phase : phases) {
			values = new ContentValues();

			values.put(STAGE_ID, phase.getStageId());
			values.put(NAME, phase.getName());
			values.put(DAYS, phase.getDays());
			values.put(HIERARCHY, phase.getHierarchy());
			values.put(STATUS, phase.getStatus());

			db.insert(TABLE_PHASES, null, values);
		}
	}

	void populateBeneficiaries(SQLiteDatabase db) {
		ContentValues values;

		ArrayList<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		beneficiaries.add(new Beneficiary(1, 1, "Gigel", 0, "Cui1", "NrRc1", "1234567891234", 0));
		beneficiaries.add(new Beneficiary(2, 2, "Dorel", 0, "Cui2", "NrRc2", "6547965479624", 0));
		beneficiaries.add(new Beneficiary(3, 3, "Costel", 1, "Cui3", "NrRc2", "6589621475658", 1));
		beneficiaries.add(new Beneficiary(4, 3, "Mirel", 1, "Cui4", "NrRc5", "112332", 1));

		for (Beneficiary beneficiary : beneficiaries) {
			values = new ContentValues();

			values.put(REGION_ID, beneficiary.getRegionID());
			values.put(NAME, beneficiary.getName());
			values.put(TYPE, beneficiary.getType());
			values.put(CUI, beneficiary.getCui());
			values.put(NR_RC, beneficiary.getNrRc());
			values.put(CNP, Long.parseLong(beneficiary.getCnp()));
			values.put(STATUS, beneficiary.getStatus());

			db.insert(TABLE_BENEFICIARIES, null, values);
		}
	}

	public void populateObjectivesPhases(ArrayList<ObjectivePhase> phases) {
		ContentValues values;

		SQLiteDatabase db = this.getReadableDatabase();
		try {

			db.execSQL("DELETE FROM " + TABLE_PHASE_OBJ_VALUES);

			for (ObjectivePhase phase : phases) {
				values = new ContentValues();

				values.put(ID, phase.getId());
				values.put(PHASE_ID, phase.getPhase_id());
				values.put(OBJECTIVE_ID, phase.getObjective_id());
				values.put(DAYS, phase.getDays());
				values.put(PHASE_START, phase.getPhase_start());
				values.put(PHASE_END, phase.getPhase_end());
				values.put(CVA_CODE, phase.getCvaCode());

				db.insert(TABLE_PHASE_OBJ_VALUES, null, values);
			}
		} catch (Exception ex) {
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
		}

		db.close();
	}

	public void populateBeneficiaries(ArrayList<Beneficiary> beneficiaries) {
		ContentValues values;

		SQLiteDatabase db = this.getReadableDatabase();

		try {

			db.execSQL("DELETE FROM " + TABLE_BENEFICIARIES);

			for (Beneficiary beneficiary : beneficiaries) {
				values = new ContentValues();

				values.put(ID, beneficiary.getId());
				values.put(REGION_ID, beneficiary.getRegionID());
				values.put(NAME, beneficiary.getName());
				values.put(TYPE, beneficiary.getType());
				values.put(CUI, beneficiary.getCui());
				values.put(NR_RC, beneficiary.getNrRc());
				values.put(CNP, Long.parseLong(beneficiary.getCnp()));
				values.put(STATUS, beneficiary.getStatus());
				values.put(CVA_CODE, beneficiary.getCvaCode());

				db.insert(TABLE_BENEFICIARIES, null, values);
			}
		} catch (Exception ex) {
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
		}
		db.close();
	}

	public void populateObjectives(ArrayList<Objective> objectives) {
		ContentValues values;
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		SQLiteDatabase db = this.getReadableDatabase();

		try {

			db.execSQL("DELETE FROM " + TABLE_OBJECTIVES);

			for (Objective objective : objectives) {
				int addedObjectiveId = -1;
				int addedPhaseId = -1;
				values = new ContentValues();

				values.put(ID, objective.getId());
				values.put(TYPE_ID, objective.getTypeId());
				values.put(CVA_CODE, objective.getCvaCode());
				values.put(REGION_ID, objective.getRegionID());
				values.put(NAME, objective.getName());
				values.put(CREATION_DATE, sdf.format(objective.getCreationDate().getTime()));
				values.put(BENEFICIARY_ID, objective.getBeneficiaryId());
				values.put(BENEFICIARY_TYPE, objective.getBeneficiaryType());
				values.put(AUTHORIZATION_START, sdf.format(objective.getAuthorizationStart().getTime()));
				values.put(AUTHORIZATION_END, sdf.format(objective.getAuthorizationEnd().getTime()));
				values.put(ESTIMATION_VALUE, objective.getEstimationValue());
				values.put(ADDRESS, objective.getAddress());
				values.put(ZIP, objective.getZip());
				values.put(GPS, objective.getGps());
				values.put(STAGE_ID, objective.getStageId());
				values.put(PHASE_ID, objective.getPhaseId());
				values.put(EXPIRATION_PHASE, sdf.format(objective.getExpirationPhase().getTime()));
				values.put(STATUS, objective.getStatus());

				values.put(STATUS_ID, objective.getStatusId());
				values.put(CATEGORY_ID, objective.getCategoryId());

				values.put(EXEC_NAME, objective.getNumeExecutant());
				values.put(EXEC_CUI, objective.getCuiExecutant());
				values.put(EXEC_NRC, objective.getNrcExecutant());

				// Meserias fields, Author: Alin
				values.put(MESER_NAME, objective.getNumeMeserias());
				values.put(MESER_SURNAME, objective.getPrenMeserias());
				values.put(MESER_TEL, objective.getTelMeserias());
				// End Meserias fields

				values.put(TEL_BENEF, objective.getTelBenef());

				values.put(FILIALA, objective.getFiliala());

				db.insert(TABLE_OBJECTIVES, null, values);

				values.clear();

			}
		} catch (Exception ex) {
			Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
		}
		db.close();

	}

	void populateObjectives(SQLiteDatabase db) {

		ContentValues values;
		ArrayList<Objective> objectives = new ArrayList<Objective>();

		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		objectives.add(new Objective(Objective.TYPE_NEW, "00083035", 1, "Casa 1, Bucuresti", Calendar.getInstance(), 1, Beneficiary.TYPE_INDIVIDUAL,
				Calendar.getInstance(), Calendar.getInstance(), (float) 1.2, "Oras1, Strada1, 123", 1, "44.4378258,26.0946376", 1, 3, Calendar.getInstance(),
				1));

		objectives.add(
				new Objective(Objective.TYPE_NEW, "00083035", 1, "Casa 2, Ifov", Calendar.getInstance(), 2, Beneficiary.TYPE_INDIVIDUAL, Calendar.getInstance(),
						Calendar.getInstance(), (float) 1.3, "Oras2, Strada2, 123", 1, "44.6154213,26.121148", 2, 11, Calendar.getInstance(), 1));

		objectives.add(
				new Objective(Objective.TYPE_NEW, "00083035", 2, "Casa 3, Romania", Calendar.getInstance(), 3, Beneficiary.TYPE_LEGAL, Calendar.getInstance(),
						Calendar.getInstance(), (float) 1.4, "Oras3, Strada3, N/A", 1, "45.9419466,25.0094284", 3, 15, Calendar.getInstance(), 1));

		objectives.add(new Objective(Objective.TYPE_RENOVATION, "00083035", 2, "Piata Romana", Calendar.getInstance(), 3, Beneficiary.TYPE_LEGAL,
				Calendar.getInstance(), Calendar.getInstance(), (float) 1.4, "Oras4, N/A, N/A", 1, "44.4457872,26.0975808", 3, 16, Calendar.getInstance(), 1));

		objectives
				.add(new Objective(Objective.TYPE_NEW, "00083035", 2, "Piata Unirii", Calendar.getInstance(), 3, Beneficiary.TYPE_LEGAL, Calendar.getInstance(),
						Calendar.getInstance(), (float) 1.6, "AdresaCasei3, N/A, N/A", 1, "44.4266008,26.1000115", 4, 18, Calendar.getInstance(), 1));

		objectives
				.add(new Objective(Objective.TYPE_NEW, "00083035", 2, "Piata Muncii", Calendar.getInstance(), 3, Beneficiary.TYPE_LEGAL, Calendar.getInstance(),
						Calendar.getInstance(), (float) 1.8, "Bucuresti, Colentina, 2", 1, "44.4312413,26.1389255", 3, 14, Calendar.getInstance(), 1));

		objectives.add(
				new Objective(Objective.TYPE_RENOVATION, "00083035", 2, "Ploiesti", Calendar.getInstance(), 3, Beneficiary.TYPE_LEGAL, Calendar.getInstance(),
						Calendar.getInstance(), (float) 2.4, "Bucuresti, Colentina, 2B", 1, "44.9321812,26.0098925", 2, 12, Calendar.getInstance(), 1));

		for (Objective objective : objectives) {
			int addedObjectiveId = -1;
			int addedPhaseId = -1;
			values = new ContentValues();

			values.put(TYPE_ID, objective.getTypeId());
			values.put(CVA_CODE, objective.getCvaCode());
			values.put(REGION_ID, objective.getRegionID());
			values.put(NAME, objective.getName());
			values.put(CREATION_DATE, sdf.format(objective.getCreationDate().getTime()));
			values.put(BENEFICIARY_ID, objective.getBeneficiaryId());
			values.put(BENEFICIARY_TYPE, objective.getBeneficiaryType());
			values.put(AUTHORIZATION_START, sdf.format(objective.getAuthorizationStart().getTime()));
			values.put(AUTHORIZATION_END, sdf.format(objective.getAuthorizationEnd().getTime()));
			values.put(ESTIMATION_VALUE, objective.getEstimationValue());
			values.put(ADDRESS, objective.getAddress());
			values.put(ZIP, objective.getZip());
			values.put(GPS, objective.getGps());
			values.put(STAGE_ID, objective.getStageId());
			values.put(PHASE_ID, objective.getPhaseId());
			values.put(EXPIRATION_PHASE, sdf.format(objective.getExpirationPhase().getTime()));
			values.put(STATUS, objective.getStatus());

			db.insert(TABLE_OBJECTIVES, null, values);

			values.clear();

			String query = "SELECT o." + SQLiteHelper.ID + ", o." + PHASE_ID + " FROM " + SQLiteHelper.TABLE_OBJECTIVES + " o" + " WHERE o." + SQLiteHelper.GPS
					+ " = '" + objective.getGps() + "'";

			Cursor cursor = db.rawQuery(query, null);
			cursor.moveToFirst();

			if (cursor.getCount() > 0) {
				addedObjectiveId = cursor.getInt(0);
				addedPhaseId = cursor.getInt(1);
			}

			if (addedObjectiveId > 0 && addedPhaseId > 0) {

				query = "SELECT p." + DAYS + " FROM " + TABLE_PHASES + " p" + " WHERE p." + ID + " = " + Integer.toString(addedPhaseId);

				cursor = db.rawQuery(query, null);
				cursor.moveToFirst();

				if (cursor.getCount() > 0) {
					values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
					values.put(SQLiteHelper.OBJECTIVE_ID, addedObjectiveId);

					Calendar phaseStart = objective.getExpirationPhase();
					phaseStart.add(Calendar.DAY_OF_MONTH, 0 - cursor.getInt(0));

					values.put(SQLiteHelper.PHASE_START, sdf.format(phaseStart.getTime()));
					values.put(SQLiteHelper.PHASE_END, sdf.format(objective.getExpirationPhase().getTime()));

					values.put(SQLiteHelper.DAYS, cursor.getInt(0));

					db.insert(SQLiteHelper.TABLE_PHASE_OBJ_VALUES, null, values);
				}
			}

			cursor.close();
		}

	}

	void populateRegions(SQLiteDatabase db) {
		ContentValues values;

		ArrayList<Region> regions = new ArrayList<Region>();

		regions.add(new Region("Bucuresti", "B", "44.4378258,26.0946376"));
		regions.add(new Region("Sector 1", "S1", "44.4854224,26.0596078"));
		regions.add(new Region("Sector 2", "S2", "44.459612,26.1491118"));
		regions.add(new Region("Sector 3", "S3", "44.4181666,26.1606045"));
		regions.add(new Region("Sector 4", "S4", "44.3818986,26.1252308"));
		regions.add(new Region("Sector 5", "S5", "44.4036971,26.0472455"));
		regions.add(new Region("Sector 6", "S6", "44.4428962,26.0184909"));

		regions.add(new Region("Alba", "AB", "46.0183605,23.4619914"));
		regions.add(new Region("Arad", "AR", "46.2929325,21.7250217"));
		regions.add(new Region("Arges™", "AG", "44.9885395,24.876464"));
		regions.add(new Region("Bacau", "BC", "46.426328,26.7384955"));
		regions.add(new Region("Bihor", "BH", "46.99637,22.1220965"));
		regions.add(new Region("Bistrita-Nasaud", "BN", "47.1794491,24.5072859"));
		regions.add(new Region("Botosani", "BT", "47.8541515,26.7248419"));
		regions.add(new Region("Braila", "BR", "45.117761,27.6306426"));
		regions.add(new Region("Brasov", "BV", "45.7770686,25.3672595"));
		regions.add(new Region("Buzau", "BZ", "45.265432,26.7539611"));
		regions.add(new Region("Calarasi", "CL", "44.3151148,27.1387881"));
		regions.add(new Region("Caras-Severin", "CS", "45.1297465,22.0311305"));
		regions.add(new Region("Cluj", "CJ", "46.8833875,23.4289284"));
		regions.add(new Region("Constanta", "CT", "44.2678909,28.1112393"));
		regions.add(new Region("Covasna", "CV", "45.8993605,25.9461815"));
		regions.add(new Region("Dambovita", "DB", "44.9220405,25.5583155"));
		regions.add(new Region("Dolj", "DJ", "44.213976,23.5514841"));
		regions.add(new Region("Galati", "GL", "45.7741075,27.7254533"));
		regions.add(new Region("Giurgiu", "GR", "44.1217135,25.9272244"));
		regions.add(new Region("Gorj", "GJ", "44.961481,23.2098995"));
		regions.add(new Region("Harghita", "HR", "46.6505765,25.5657181"));
		regions.add(new Region("Hunedoara", "HD", "45.7869275,22.9711744"));
		regions.add(new Region("Ialomita", "IL", "44.597452,27.205447"));
		regions.add(new Region("Iasi", "IS", "47.206221,27.3104319"));
		regions.add(new Region("Ilfov", "IF", "44.491659,26.1391725"));
		regions.add(new Region("Maramures", "MM", "47.672324,24.010443"));
		regions.add(new Region("Menedinti", "MH", "44.6033161,22.7292993"));
		regions.add(new Region("Mures", "MS", "46.609407,24.635826"));
		regions.add(new Region("Neamt›", "NT", "46.989248,26.4545725"));
		regions.add(new Region("Olt", "OT", "44.2895053,24.3682144"));
		regions.add(new Region("Prahova", "PH", "45.103626,26.0284564"));
		regions.add(new Region("Salaj", "SJ", "47.157105,23.1671606"));
		regions.add(new Region("Satu Mare", "SM", "47.788701,22.8392465"));
		regions.add(new Region("Sibiu", "SB", "45.7834291,24.1545875"));
		regions.add(new Region("Suceava", "SV", "47.5327515,25.8128711"));
		regions.add(new Region("Teleorman", "TR", "44.0683502,25.149686"));
		regions.add(new Region("Timis", "TM", "45.690891,21.4039436"));
		regions.add(new Region("Tulcea", "TL", "45.0492427,28.8560667"));
		regions.add(new Region("Valcea", "VL", "45.0395556,24.0490709"));
		regions.add(new Region("Vaslui", "VS", "46.652146,27.7420509"));
		regions.add(new Region("Vrancea", "VN", "45.7852405,26.9663801"));

		for (Region region : regions) {
			values = new ContentValues();

			values.put(NAME, region.getName());
			values.put(CODE, region.getCode());
			values.put(GPS, region.getGps());

			db.insert(TABLE_REGIONS, null, values);
		}
	}

	void populateBranches(SQLiteDatabase db) {
		ContentValues values;

		ArrayList<Branch> branches = new ArrayList<Branch>();

		branches.add(new Branch("B01", "B"));
		branches.add(new Branch("AG02", "AG"));
		branches.add(new Branch("BV03", "BV"));
		branches.add(new Branch("MM04", "MM"));
		branches.add(new Branch("CT05", "CT"));
		branches.add(new Branch("CL06", "CL"));
		branches.add(new Branch("BC07", "BC"));
		branches.add(new Branch("CJ08", "CJ"));
		branches.add(new Branch("VN09", "VN"));

		for (Branch branch : branches) {
			values = new ContentValues();

			values.put(NAME, branch.getName());
			values.put(REGION_CODE, branch.getRegionCode());

			db.insert(TABLE_BRANCHES, null, values);
		}
	}

	void populateUsers(SQLiteDatabase db) {
		ContentValues values;

		ArrayList<User> users = new ArrayList<User>();

		users.add(new User(0, "B01", "Bogdan", "Fotescu", "65987423"));

		users.add(new User(1, "AG02", "Filip", "Fedeles", "13698745"));
		users.add(new User(1, "BV03", "Andrei", "Filimon", "31687457"));

		users.add(new User(2, "MM04", "Petruta", "Don", "98756329"));
		users.add(new User(2, "CT05", "Dorian", "Cristescu", "45654789"));
		users.add(new User(2, "CL06", "Anton", "Marcu", "30215982"));
		users.add(new User(2, "BC07", "Victor", "Popescu", "63026987"));
		users.add(new User(2, "CJ08", "Ion", "Ionescu", "10230005"));
		users.add(new User(2, "VN09", "Maria", "Marinescu", "98652369"));

		for (User user : users) {
			values = new ContentValues();

			values.put(CODE, user.getCode());
			values.put(TYPE_ID, user.getUserType());
			values.put(BRANCH_CODE, user.getBranchCode());
			values.put(NAME, user.getName());
			values.put(SURNAME, user.getSurName());

			db.insert(TABLE_USERS, null, values);
		}
	}

	// This is here just so the database initializes.
	public void doSomething() { // TODO: Delete this when all is said and done

		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
		} catch (Exception e) {
			db = super.getWritableDatabase();
		}

		db.close();
	}

	public LinkedHashMap<Stage, List<Phase>> getNomenclatures() {
		LinkedHashMap<Stage, List<Phase>> nomenclatures = new LinkedHashMap<Stage, List<Phase>>();

		SQLiteDatabase db = this.getReadableDatabase();

		String queryStages = "SELECT * " + "FROM " + TABLE_STAGES + " ORDER BY " + TABLE_STAGES + "." + HIERARCHY;

		Cursor cursorStages = db.rawQuery(queryStages, null);

		cursorStages.moveToFirst();

		while (!cursorStages.isAfterLast()) {
			int stageID = cursorStages.getInt(0);
			Log.d("DBG", "am luat Stage cu id " + stageID);
			List<Phase> phases = new ArrayList<Phase>();

			Stage newStage = new Stage(cursorStages.getInt(0), cursorStages.getString(1), cursorStages.getInt(2), cursorStages.getInt(3));

			String queryPhases = "SELECT * FROM " + TABLE_PHASES + " WHERE " + TABLE_PHASES + "." + STAGE_ID + " = " + Integer.toString(stageID) + " ORDER BY "
					+ TABLE_PHASES + "." + HIERARCHY;

			Cursor cursorPhases = db.rawQuery(queryPhases, null);
			cursorPhases.moveToFirst();

			while (!cursorPhases.isAfterLast()) {
				Log.d("DBG", "---Am luat Phase cu id " + cursorPhases.getInt(0));
				phases.add(new Phase(cursorPhases.getInt(0), cursorPhases.getInt(1), cursorPhases.getString(2), cursorPhases.getInt(3), cursorPhases.getInt(4),
						cursorPhases.getInt(5)));

				cursorPhases.moveToNext();
			}
			cursorPhases.close();

			cursorStages.moveToNext();
			nomenclatures.put(newStage, phases);
		}

		cursorStages.close();
		return nomenclatures;
	}

}
