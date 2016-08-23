package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for handling stage database operations
 */
public class StageData {

    private SQLiteHelper dbHelper;
    private Context context;

    public StageData(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);

    }

    /**
     * Adds a new stage to the database
     *
     * @param stage The stage to be added
     * @return The added stage complete with row id
     */
    public Stage addStage(Stage stage) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        int maxHierarchy = getMaxStageHierarchy(stage, db);
        int minHierarchy = getMinStageHierarchy(stage, db);

        if (stage.getHierarchy() > maxHierarchy) {
            stage.setHierarchy(maxHierarchy + 1);
        } else if (stage.getHierarchy() < minHierarchy) {
            stage.setHierarchy(minHierarchy);
        }

        boolean hierarchyExists = stageHierarchyExists(stage, db);

        if (hierarchyExists) {
            List<Stage> affectedStages = new ArrayList<Stage>();

            String getAffectedStages = "SELECT * FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " +
                    SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(stage.getHierarchy());

            Cursor cursorAffectedStages = db.rawQuery(getAffectedStages, null);
            cursorAffectedStages.moveToFirst();

            // Save affected phases
            while (!cursorAffectedStages.isAfterLast()) {
                affectedStages.add(new Stage(cursorAffectedStages.getInt(0), cursorAffectedStages.getString(1),
                        cursorAffectedStages.getInt(2), cursorAffectedStages.getInt(3)));

                cursorAffectedStages.moveToNext();
            }
            cursorAffectedStages.close();

            for (Stage curentStage : affectedStages) {
                values = new ContentValues();
                values.put(SQLiteHelper.HIERARCHY, curentStage.getHierarchy() + 1);

                db.update(SQLiteHelper.TABLE_STAGES, values, SQLiteHelper.ID + " = " + Integer.toString(curentStage.getId()), null);
            }
        }

        values.put(SQLiteHelper.NAME, stage.getName());
        values.put(SQLiteHelper.HIERARCHY, stage.getHierarchy());
        values.put(SQLiteHelper.STATUS, stage.getStatus());

        db.insert(SQLiteHelper.TABLE_STAGES, null, values);

        String getUpdatedObject = "SELECT * FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " +
                SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " = " + Integer.toString(stage.getHierarchy()) +
                " AND " +
                SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.NAME + " = '" + stage.getName() + "'" +
                " AND " +
                SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.STATUS + " = " + Integer.toString(stage.getStatus());

        Cursor updatedObjectCursor = db.rawQuery(getUpdatedObject, null);

        updatedObjectCursor.moveToFirst();

        Stage addedStage = new Stage(updatedObjectCursor.getInt(0),
                updatedObjectCursor.getString(1), updatedObjectCursor.getInt(2),
                updatedObjectCursor.getInt(3));

        updatedObjectCursor.close();
        db.close();

        return addedStage;

    }

    /**
     * Updates an existing stage
     *
     * @param newStage The new stage details
     * @return The updated stage complete with row id
     */
    public Stage updateStage(Stage newStage) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values;

        Stage oldStage = getStage(newStage.getId(), db);

        int maxHierarchy = getMaxStageHierarchy(newStage, db);
        int minHierarchy = getMinStageHierarchy(newStage, db);

        if (newStage.getHierarchy() > maxHierarchy) {
            newStage.setHierarchy(maxHierarchy);
        } else if (newStage.getHierarchy() < minHierarchy) {
            newStage.setHierarchy(minHierarchy);
        }

        int newHierarchy = newStage.getHierarchy();
        int oldHierarchy = oldStage.getHierarchy();

        if (newHierarchy < oldHierarchy) {
            List<Stage> affectedStages = new ArrayList<Stage>();

            String queryAffectedStages = "SELECT * FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " +
                    SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " <= " + Integer.toString(oldHierarchy) +
                    " AND " +
                    SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(newHierarchy);

            Cursor cursorAffectedStages = db.rawQuery(queryAffectedStages, null);
            cursorAffectedStages.moveToFirst();

            while (!cursorAffectedStages.isAfterLast()) {
                affectedStages.add(new Stage(cursorAffectedStages.getInt(0), cursorAffectedStages.getString(1),
                        cursorAffectedStages.getInt(2), cursorAffectedStages.getInt(3)));

                cursorAffectedStages.moveToNext();
            }

            cursorAffectedStages.close();

            for (Stage stage : affectedStages) {
                values = new ContentValues();

                if (stage.getId() == newStage.getId()) {
                    values.put(SQLiteHelper.HIERARCHY, newStage.getHierarchy());
                    values.put(SQLiteHelper.NAME, newStage.getName());
                    values.put(SQLiteHelper.STATUS, newStage.getStatus());
                } else {
                    values.put(SQLiteHelper.HIERARCHY, stage.getHierarchy() + 1);
                }

                db.update(SQLiteHelper.TABLE_STAGES, values, SQLiteHelper.ID + " = " + Integer.toString(stage.getId()), null);
            }
        } else {
            if (newHierarchy > oldHierarchy) {
                List<Stage> affectedStages = new ArrayList<Stage>();

                String queryAffectedStages = "SELECT * FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " +
                        SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(oldHierarchy) +
                        " AND " +
                        SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " <= " + Integer.toString(newHierarchy);

                Cursor cursorAffectedStages = db.rawQuery(queryAffectedStages, null);
                cursorAffectedStages.moveToFirst();

                while (!cursorAffectedStages.isAfterLast()) {
                    affectedStages.add(new Stage(cursorAffectedStages.getInt(0), cursorAffectedStages.getString(1),
                            cursorAffectedStages.getInt(2), cursorAffectedStages.getInt(3)));

                    cursorAffectedStages.moveToNext();
                }

                cursorAffectedStages.close();

                for (Stage stage : affectedStages) {
                    values = new ContentValues();

                    if (stage.getId() == newStage.getId()) {
                        values.put(SQLiteHelper.HIERARCHY, newStage.getHierarchy());
                        values.put(SQLiteHelper.NAME, newStage.getName());
                        values.put(SQLiteHelper.STATUS, newStage.getStatus());
                    } else {
                        values.put(SQLiteHelper.HIERARCHY, stage.getHierarchy() - 1);
                    }

                    db.update(SQLiteHelper.TABLE_STAGES, values, SQLiteHelper.ID + " = " + Integer.toString(stage.getId()), null);
                }
            } else {
                values = new ContentValues();

                values.put(SQLiteHelper.HIERARCHY, newStage.getHierarchy());
                values.put(SQLiteHelper.NAME, newStage.getName());
                values.put(SQLiteHelper.STATUS, newStage.getStatus());

                db.update(SQLiteHelper.TABLE_STAGES, values, SQLiteHelper.ID + " = " + Integer.toString(newStage.getId()), null);

            }
        }
        Stage updatedStage = getStage(newStage.getId(), db);

        db.close();

        return updatedStage;
    }

    /**
     * Deletes specified stage from the database
     *
     * @param stage The stage to be deleted
     * @return True if delete was successful, False if not
     */
    public boolean deleteStage(Stage stage) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<Stage> affectedStages = new ArrayList<Stage>();

        String queryAffectedStages = "SELECT * FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " +
                SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " > " + Integer.toString(stage.getHierarchy());

        Cursor cursorAffectedStages = db.rawQuery(queryAffectedStages, null);
        cursorAffectedStages.moveToFirst();

        while (!cursorAffectedStages.isAfterLast()) {
            affectedStages.add(new Stage(cursorAffectedStages.getInt(0), cursorAffectedStages.getString(1),
                    cursorAffectedStages.getInt(2), cursorAffectedStages.getInt(3)));

            cursorAffectedStages.moveToNext();
        }

        cursorAffectedStages.close();

        for (Stage affectedStage : affectedStages) {
            ContentValues values = new ContentValues();

            values.put(SQLiteHelper.HIERARCHY, affectedStage.getHierarchy() - 1);


            db.update(SQLiteHelper.TABLE_STAGES, values, SQLiteHelper.ID + " = " + Integer.toString(affectedStage.getId()), null);
        }

        int deletedPhases = db.delete(SQLiteHelper.TABLE_PHASES, SQLiteHelper.STAGE_ID + " = " + stage.getId(), null);
        Log.d("DBG", "Am sters:" + Integer.toString(deletedPhases) + " faze");

        return db.delete(SQLiteHelper.TABLE_STAGES, SQLiteHelper.ID + " = " + stage.getId(), null) > 0;

    }

    /**
     * Provides the highest stage hierarchy in the database
     *
     * @param stage Not used. Can be null
     * @param db SQLiteDatabase reference
     *
     * @return The maximum stage hierarchy
     */
    public int getMaxStageHierarchy(Stage stage, SQLiteDatabase db) {
        String maxStageQuery = "SELECT MAX(" + SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + ") FROM " + SQLiteHelper.TABLE_STAGES;

        Cursor cursorMaxStageQuery = db.rawQuery(maxStageQuery, null);

        cursorMaxStageQuery.moveToFirst();
        return cursorMaxStageQuery.getInt(0);
    }

    /**
     * Provides the minimum stage hierarchy in the database
     *
     * @param stage Not used. Can be null
     * @param db SQLiteDatabase Reference
     *
     * @return The minimum stage hierarchy
     */
    public int getMinStageHierarchy(Stage stage, SQLiteDatabase db) {
        String minStageQuery = "SELECT MIN(" + SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + ") FROM " + SQLiteHelper.TABLE_STAGES;

        Cursor cursorMinStageQuery = db.rawQuery(minStageQuery, null);

        cursorMinStageQuery.moveToFirst();
        return cursorMinStageQuery.getInt(0);
    }

    /**
     * Checks if the curent stage's hierarchy exists
     *
     * @param stage The stage to be checked
     * @param db SQLiteDatabase reference
     *
     * @return True if the hierarchy exists, False if it does not
     */
    public boolean stageHierarchyExists(Stage stage, SQLiteDatabase db) {
        String queryHierarchies = "SELECT " + SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " FROM " + SQLiteHelper.TABLE_STAGES +
                " WHERE " +
                SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY + " = " + Integer.toString(stage.getHierarchy());

        Cursor cursorStages = db.rawQuery(queryHierarchies, null);

        boolean stageHierarchyExists = !(cursorStages == null || cursorStages.getCount() <= 0);

        if (stageHierarchyExists)
            cursorStages.close();

        return stageHierarchyExists;
    }

    /**
     * Gets a stage by id
     *
     * @param id The id of the requested stage
     * @param db SQLiteDatabase reference
     *
     * @return The found stage
     */
    public Stage getStage(int id, SQLiteDatabase db) {
        Stage stage;

        String queryStage = "SELECT * FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " +
                SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.ID + " = " + id;

        Cursor cursorStages = db.rawQuery(queryStage, null);

        cursorStages.moveToFirst();

        stage = new Stage(cursorStages.getInt(0), cursorStages.getString(1),
                cursorStages.getInt(2), cursorStages.getInt(3));

        cursorStages.close();

        return stage;
    }

    /**
     * Gets stages from the database that have the hierarchy higher than the specified limit
     *
     * @param hierarchyLimit Lower stage hierarchy limit
     * @return List of all the stages with the hierarchy greater than the specified limit
     */
    public ArrayList<Stage> getAllStages(int hierarchyLimit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Stage> stages = new ArrayList<Stage>();

        String query = "SELECT s.*, COUNT(p." + SQLiteHelper.STAGE_ID + ")" +
                " FROM " + SQLiteHelper.TABLE_PHASES + " p" +
                " JOIN " + SQLiteHelper.TABLE_STAGES + " s ON p." + SQLiteHelper.STAGE_ID + " = s." + SQLiteHelper.ID +
                " WHERE s." + SQLiteHelper.HIERARCHY + " > " + Integer.toString(hierarchyLimit) +
                " GROUP BY p." + SQLiteHelper.STAGE_ID +
                " ORDER BY s." + SQLiteHelper.HIERARCHY;

        Cursor cursorStages = db.rawQuery(query, null);

        cursorStages.moveToFirst();

        while (!cursorStages.isAfterLast()) {
            int stageID = cursorStages.getInt(0);
            Log.d("DBG", "am luat Stage cu id " + stageID);

            stages.add(new Stage(cursorStages.getInt(0), cursorStages.getString(1),
                    cursorStages.getInt(2), cursorStages.getInt(3)));

            cursorStages.moveToNext();
        }

        cursorStages.close();
        db.close();

        return stages;
    }

    /**
     * //TODO @Filip Documentation? Mai folosim asta?
     * @param ObjectiveId
     * @return
     */
    public ArrayList<Stage> getAvailableStages(int ObjectiveId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Stage> stages = new ArrayList<Stage>();

        String query = "SELECT * " + "FROM " + SQLiteHelper.TABLE_STAGES +
                " WHERE " + SQLiteHelper.HIERARCHY + " >= (SELECT " + SQLiteHelper.HIERARCHY + " FROM " + SQLiteHelper.TABLE_STAGES + " WHERE " + SQLiteHelper.ID + " = " + ObjectiveId + ")  ORDER BY " + SQLiteHelper.TABLE_STAGES + "." + SQLiteHelper.HIERARCHY;

        Cursor cursorStages = db.rawQuery(query, null);

        cursorStages.moveToFirst();

        while (!cursorStages.isAfterLast()) {
            int stageID = cursorStages.getInt(0);
            Log.d("DBG", "am luat Stage cu id " + stageID);

            stages.add(new Stage(cursorStages.getInt(0), cursorStages.getString(1),
                    cursorStages.getInt(2), cursorStages.getInt(3)));

            cursorStages.moveToNext();
        }

        cursorStages.close();
        db.close();

        return stages;
    }
}
