package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Convenience class for handling logging database operations
 */
public class LogUtils {
    private SQLiteHelper dbHelper;
    private Context context;

    public LogUtils(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);

    }

    /**
     * Creates a new log entry for the provided objective
     *
     * @param objective The recently changed objective
     */
    public void logObjectiveChanged(Objective objective){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

        values.put(SQLiteHelper.CREATION_DATE, sdf.format(Calendar.getInstance().getTime()));
        values.put(SQLiteHelper.OBJECTIVE_ID, objective.getId());
        values.put(SQLiteHelper.EXPIRATION_PHASE, sdf.format(objective.getExpirationPhase().getTime()));
        values.put(SQLiteHelper.STAGE_ID, objective.getStageId());
        values.put(SQLiteHelper.PHASE_ID, objective.getPhaseId());
        values.put(SQLiteHelper.DETAILS, objective.toString());

        db.insert(SQLiteHelper.TABLE_CHANGE_LOGS, null, values);

        db.close();

    }
}
