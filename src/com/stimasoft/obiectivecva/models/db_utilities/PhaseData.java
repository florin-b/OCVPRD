package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for handling phase database operations
 */
public class PhaseData {

    private SQLiteHelper dbHelper;
    private Context context;

    public PhaseData(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);

    }

    /**
     * Adds a new phase to the database
     *
     * @param phase The phase to be added
     * @return The added phase complete with row id
     */
    public Phase addPhase(Phase phase) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values;


        int maxHierarchy = getMaxPhaseHierarchy(phase, db);
        int minHierarchy = getMinPhaseHierarchy(phase, db);

        if (phase.getHierarchy() > maxHierarchy) { // No matter the hierarchy, if it's bigger than maximum, assign maximum + 1
            phase.setHierarchy(maxHierarchy + 1);
        } else if (phase.getHierarchy() < minHierarchy) {
            phase.setHierarchy(minHierarchy); // No matter the hierarchy, it it's less than minimum, assign the minimum value
        }

        boolean hierarchyExists = phaseHierarchyExists(phase, db);

        if (hierarchyExists) { // If the new phase replaces an old one, get all the phases that have their hierarchies affected by the new insertion
            List<Phase> affectedPhases = new ArrayList<Phase>();

            String getAffectedPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                    SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(phase.getHierarchy()) +
                    " AND " +
                    SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(phase.getStageId());

            Cursor cursorAffectedPhases = db.rawQuery(getAffectedPhases, null);
            cursorAffectedPhases.moveToFirst();

            // Save affected phases
            while (!cursorAffectedPhases.isAfterLast()) {
                affectedPhases.add(new Phase(cursorAffectedPhases.getInt(0), cursorAffectedPhases.getInt(1),
                        cursorAffectedPhases.getString(2), cursorAffectedPhases.getInt(3),
                        cursorAffectedPhases.getInt(4), cursorAffectedPhases.getInt(5)));

                cursorAffectedPhases.moveToNext();
            }
            cursorAffectedPhases.close();

            // Update the affected phases with incremented hierarchies
            for (Phase curentPhase : affectedPhases) {
                values = new ContentValues();
                values.put(SQLiteHelper.HIERARCHY, curentPhase.getHierarchy() + 1);

                db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(curentPhase.getId()), null);
            }
        }

        // Insert the new phase
        values = new ContentValues();

        values.put(SQLiteHelper.STAGE_ID, phase.getStageId());
        values.put(SQLiteHelper.NAME, phase.getName());
        values.put(SQLiteHelper.DAYS, phase.getDays());
        values.put(SQLiteHelper.HIERARCHY, phase.getHierarchy());
        values.put(SQLiteHelper.STATUS, phase.getStatus());

        db.insert(SQLiteHelper.TABLE_PHASES, null, values);

        String getUpdatedObject = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " = " + Integer.toString(phase.getHierarchy()) +
                " AND " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.NAME + " = '" + phase.getName() + "'" +
                " AND " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.DAYS + " = " + Integer.toString(phase.getDays()) +
                " AND " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STATUS + " = " + Integer.toString(phase.getStatus());

        Cursor updatedObjectCursor = db.rawQuery(getUpdatedObject, null);

        updatedObjectCursor.moveToFirst();

        // Get a reference to the new updated phase
        Phase addedPhase = new Phase(updatedObjectCursor.getInt(0), updatedObjectCursor.getInt(1),
                updatedObjectCursor.getString(2), updatedObjectCursor.getInt(3),
                updatedObjectCursor.getInt(4), updatedObjectCursor.getInt(5));

        updatedObjectCursor.close();
        db.close();

        return addedPhase;
    }

    /**
     * Updates an existing phase
     *
     * @param newPhase The phase to be update (complete with new details)
     * @return The updated phase complete with row id
     */
    public Phase updatePhase(Phase newPhase) {
        ArrayList<Phase> affectedPhases = new ArrayList<Phase>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values;

        Phase dbPhase = getPhase(newPhase.getId(), db);

        int newHierarchy = newPhase.getHierarchy();
        int oldHierarchy = dbPhase.getHierarchy();

        int maxHierarchy = getMaxPhaseHierarchy(newPhase, db);
        int minHierarchy = getMinPhaseHierarchy(newPhase, db);

        if (newPhase.getStageId() != dbPhase.getStageId()) { // If the phase was moved under a new stage

            if (newHierarchy > maxHierarchy) { // If the phase's hierarchy is greater than the new stage's maximum, assign maximum + 1
                // Commit the new phase to the db under it's new stage
                values = new ContentValues();

                values.put(SQLiteHelper.STAGE_ID, newPhase.getStageId());
                values.put(SQLiteHelper.HIERARCHY, maxHierarchy + 1);
                values.put(SQLiteHelper.NAME, newPhase.getName());
                values.put(SQLiteHelper.DAYS, newPhase.getDays());
                values.put(SQLiteHelper.STATUS, newPhase.getStatus());

                db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(newPhase.getId()), null);

                // Update the hierarchies from the old parent stage.
                String getOldPhaseSiblings = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(dbPhase.getStageId()) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(dbPhase.getHierarchy());

                Cursor oldPhaseSiblingsCursor = db.rawQuery(getOldPhaseSiblings, null);
                oldPhaseSiblingsCursor.moveToFirst();

                while (!oldPhaseSiblingsCursor.isAfterLast()) {
                    Phase tempPhase = new Phase(oldPhaseSiblingsCursor.getInt(0), oldPhaseSiblingsCursor.getInt(1),
                            oldPhaseSiblingsCursor.getString(2), oldPhaseSiblingsCursor.getInt(3),
                            oldPhaseSiblingsCursor.getInt(4), oldPhaseSiblingsCursor.getInt(5));

                    values = new ContentValues();

                    values.put(SQLiteHelper.HIERARCHY, tempPhase.getHierarchy() - 1);
                    db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(tempPhase.getId()), null);

                    oldPhaseSiblingsCursor.moveToNext();
                }
                oldPhaseSiblingsCursor.close();

            } else {

                if (newHierarchy < minHierarchy) { // Analog max hierarchy, but for minimum.
                    newPhase.setHierarchy(minHierarchy);
                }

                String getAffectedPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(newPhase.getHierarchy()) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(newPhase.getStageId());

                Cursor cursorAffectedPhases = db.rawQuery(getAffectedPhases, null);
                cursorAffectedPhases.moveToFirst();

                // Save affected phases
                while (!cursorAffectedPhases.isAfterLast()) {
                    affectedPhases.add(new Phase(cursorAffectedPhases.getInt(0), cursorAffectedPhases.getInt(1),
                            cursorAffectedPhases.getString(2), cursorAffectedPhases.getInt(3),
                            cursorAffectedPhases.getInt(4), cursorAffectedPhases.getInt(5)));

                    cursorAffectedPhases.moveToNext();
                }
                cursorAffectedPhases.close();

                for (Phase curentPhase : affectedPhases) {
                    values = new ContentValues();
                    values.put(SQLiteHelper.HIERARCHY, curentPhase.getHierarchy() + 1);

                    db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(curentPhase.getId()), null);
                }

                // Commit the modified phase
                values = new ContentValues();

                values.put(SQLiteHelper.STAGE_ID, newPhase.getStageId());
                values.put(SQLiteHelper.HIERARCHY, newPhase.getHierarchy());
                values.put(SQLiteHelper.NAME, newPhase.getName());
                values.put(SQLiteHelper.DAYS, newPhase.getDays());
                values.put(SQLiteHelper.STATUS, newPhase.getStatus());

                db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(newPhase.getId()), null);

                // Update the hierarchies from the old parent stage.
                String getOldPhaseSiblings = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(dbPhase.getStageId()) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(dbPhase.getHierarchy());

                Cursor oldPhaseSiblingsCursor = db.rawQuery(getOldPhaseSiblings, null);
                oldPhaseSiblingsCursor.moveToFirst();

                while (!oldPhaseSiblingsCursor.isAfterLast()) {
                    Phase tempPhase = new Phase(oldPhaseSiblingsCursor.getInt(0), oldPhaseSiblingsCursor.getInt(1),
                            oldPhaseSiblingsCursor.getString(2), oldPhaseSiblingsCursor.getInt(3),
                            oldPhaseSiblingsCursor.getInt(4), oldPhaseSiblingsCursor.getInt(5));

                    values = new ContentValues();

                    values.put(SQLiteHelper.HIERARCHY, tempPhase.getHierarchy() - 1);
                    db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(tempPhase.getId()), null);

                    oldPhaseSiblingsCursor.moveToNext();
                }
                oldPhaseSiblingsCursor.close();
            }
        } else { // If the stage doesn't change

            if (newHierarchy > maxHierarchy) {
                newPhase.setHierarchy(maxHierarchy);
            } else if (newHierarchy < minHierarchy) {
                newPhase.setHierarchy(minHierarchy);
            }

            // Cascading change with other details
            if (newHierarchy < oldHierarchy) {
                // Get affected phases
                String getAffectedPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " <= " + Integer.toString(oldHierarchy) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(newHierarchy) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(newPhase.getStageId());

                Cursor cursorAffectedPhases = db.rawQuery(getAffectedPhases, null);
                cursorAffectedPhases.moveToFirst();

                // Save affected phases
                while (!cursorAffectedPhases.isAfterLast()) {
                    affectedPhases.add(new Phase(cursorAffectedPhases.getInt(0), cursorAffectedPhases.getInt(1),
                            cursorAffectedPhases.getString(2), cursorAffectedPhases.getInt(3),
                            cursorAffectedPhases.getInt(4), cursorAffectedPhases.getInt(5)));

                    cursorAffectedPhases.moveToNext();
                }
                cursorAffectedPhases.close();

                // If the affected phase is the updated one, update everything, else just update hierarchy
                for (Phase curentPhase : affectedPhases) {
                    values = new ContentValues();

                    if (curentPhase.getId() == newPhase.getId()) {
                        values.put(SQLiteHelper.HIERARCHY, newPhase.getHierarchy());
                        values.put(SQLiteHelper.NAME, newPhase.getName());
                        values.put(SQLiteHelper.DAYS, newPhase.getDays());
                        values.put(SQLiteHelper.STATUS, newPhase.getStatus());
                    } else {
                        values.put(SQLiteHelper.HIERARCHY, curentPhase.getHierarchy() + 1);
                    }

                    db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(curentPhase.getId()), null);
                }

            } else if (newHierarchy > oldHierarchy) {
                // Get affected phases
                String getAffectedPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " >= " + Integer.toString(oldHierarchy) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " <= " + Integer.toString(newHierarchy) +
                        " AND " +
                        SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(newPhase.getStageId());

                // Save affected phases
                Cursor cursorAffectedPhases = db.rawQuery(getAffectedPhases, null);
                cursorAffectedPhases.moveToFirst();

                while (!cursorAffectedPhases.isAfterLast()) {
                    affectedPhases.add(new Phase(cursorAffectedPhases.getInt(0), cursorAffectedPhases.getInt(1),
                            cursorAffectedPhases.getString(2), cursorAffectedPhases.getInt(3),
                            cursorAffectedPhases.getInt(4), cursorAffectedPhases.getInt(5)));

                    cursorAffectedPhases.moveToNext();
                }

                cursorAffectedPhases.close();

                // Save affected phases
                for (Phase curentPhase : affectedPhases) {
                    values = new ContentValues();

                    if (curentPhase.getId() == newPhase.getId()) {
                        values.put(SQLiteHelper.HIERARCHY, newPhase.getHierarchy());
                        values.put(SQLiteHelper.NAME, newPhase.getName());
                        values.put(SQLiteHelper.DAYS, newPhase.getDays());
                        values.put(SQLiteHelper.STATUS, newPhase.getStatus());
                    } else {
                        values.put(SQLiteHelper.HIERARCHY, curentPhase.getHierarchy() - 1);
                    }

                    db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(curentPhase.getId()), null);
                }

            } else {
                values = new ContentValues();

                values.put(SQLiteHelper.HIERARCHY, newPhase.getHierarchy());
                values.put(SQLiteHelper.NAME, newPhase.getName());
                values.put(SQLiteHelper.DAYS, newPhase.getDays());
                values.put(SQLiteHelper.STATUS, newPhase.getStatus());

                db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(newPhase.getId()), null);
            }
        }

        Phase updatedPhase = getPhase(newPhase.getId(), db);

        db.close();

        return updatedPhase;
    }

    /**
     * Gets a phase by a specified Id
     *
     * @param id The id of the searched phase
     * @param db SQLiteDatabase reference
     *
     * @return The found Phase
     */
    public Phase getPhase(int id, SQLiteDatabase db) {
        Phase phase;

        String queryPhase = "SELECT * " + "FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.ID + " = " + id;

        Cursor cursorPhases = db.rawQuery(queryPhase, null);

        cursorPhases.moveToFirst();

        phase = new Phase(cursorPhases.getInt(0), cursorPhases.getInt(1), cursorPhases.getString(2),
                cursorPhases.getInt(3), cursorPhases.getInt(4), cursorPhases.getInt(5));

        cursorPhases.close();

        return phase;
    }

    /**
     * Deletes a phase based on it's id
     *
     * @param phase The phase that is to be deleted
     * @return True if the phase was deleted successfully
     */
    public boolean deletePhase(Phase phase) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Update the hierarchies from the old parent stage.
        String getAffectedPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(phase.getStageId()) +
                " AND " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " > " + Integer.toString(phase.getHierarchy());

        Cursor affectedPhasesCursor = db.rawQuery(getAffectedPhases, null);
        affectedPhasesCursor.moveToFirst();

        while (!affectedPhasesCursor.isAfterLast()) {
            Phase tempPhase = new Phase(affectedPhasesCursor.getInt(0), affectedPhasesCursor.getInt(1),
                    affectedPhasesCursor.getString(2), affectedPhasesCursor.getInt(3),
                    affectedPhasesCursor.getInt(4), affectedPhasesCursor.getInt(5));

            ContentValues values = new ContentValues();

            values.put(SQLiteHelper.HIERARCHY, tempPhase.getHierarchy() - 1);
            db.update(SQLiteHelper.TABLE_PHASES, values, SQLiteHelper.ID + " = " + Integer.toString(tempPhase.getId()), null);

            affectedPhasesCursor.moveToNext();
        }
        affectedPhasesCursor.close();

        return db.delete(SQLiteHelper.TABLE_PHASES, SQLiteHelper.ID + " = " + Integer.toString(phase.getId()), null) > 0;
    }

    /**
     * Checks if the specified phase's hierarchy exists in the database
     *
     * @param phase The phase whose hierarchy will be checked
     * @param db SQLiteDatabase reference
     *
     * @return True if the hierarchy exists, false if it does not
     */
    public boolean phaseHierarchyExists(Phase phase, SQLiteDatabase db) {
        String queryHierarchies = "SELECT " + SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " FROM " + SQLiteHelper.TABLE_PHASES +
                " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + " = " + Integer.toString(phase.getHierarchy()) +
                " AND " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(phase.getStageId());

        Cursor cursorPhases = db.rawQuery(queryHierarchies, null);

        boolean phaseHierarchyExists = !(cursorPhases == null || cursorPhases.getCount() <= 0);

        if (phaseHierarchyExists)
            cursorPhases.close();

        return phaseHierarchyExists;
    }

    /**
     * Gets the highest phase hierarchy in a stage
     *
     * @param phase Stage whose stage will be interrogated for max hierarchy
     * @param db SQLiteDatabase reference
     *
     * @return The maximum hierarchy of the specified phase's stage
     */
    public int getMaxPhaseHierarchy(Phase phase, SQLiteDatabase db) {
        String maxPhaseQuery = "SELECT MAX(" + SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + ") FROM " + SQLiteHelper.TABLE_PHASES +
                " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(phase.getStageId());

        Cursor cursorMaxPhaseQuery = db.rawQuery(maxPhaseQuery, null);

        cursorMaxPhaseQuery.moveToFirst();
        return cursorMaxPhaseQuery.getInt(0);
    }

    /**
     * Gets the lowest phase hierarchy in a stage
     *
     * @param phase Stage whose stage will be interrogated for min hierarchy
     * @param db SQLiteDatabase reference
     *
     * @return The minimum hierarchy of the specified phase's stage
     */
    public int getMinPhaseHierarchy(Phase phase, SQLiteDatabase db) {
        String minPhaseQuery = "SELECT MIN(" + SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY + ") FROM " + SQLiteHelper.TABLE_PHASES +
                " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(phase.getStageId());

        Cursor cursorMinPhaseQuery = db.rawQuery(minPhaseQuery, null);

        cursorMinPhaseQuery.moveToFirst();
        return cursorMinPhaseQuery.getInt(0);
    }

    /**
     * Fetches all the phases that belong to one stage
     *
     * @param stageId The id of the stage for the requested phases
     * @return List of phase children of the specified stage
     */
    public ArrayList<Phase> getAllPhasesForStage(int stageId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Phase> phases = new ArrayList<Phase>();

        String queryPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(stageId) +
                " ORDER BY " + SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY;


        Cursor cursorPhases = db.rawQuery(queryPhases, null);
        cursorPhases.moveToFirst();

        while (!cursorPhases.isAfterLast()) {
            Log.d("DBG", "---Am luat Phase cu id " + cursorPhases.getInt(0));

            phases.add(new Phase(cursorPhases.getInt(0), cursorPhases.getInt(1),
                    cursorPhases.getString(2), cursorPhases.getInt(3),
                    cursorPhases.getInt(4), cursorPhases.getInt(5)));

            cursorPhases.moveToNext();
        }

        cursorPhases.close();
        db.close();

        return phases;
    }

    /**
     * //TODO @Filip Documentation required (asta e aia care ia custom phase durations daca exista in tabel nu?
     * @param objectiveId
     * @param stageId
     * @return
     */
    public ArrayList<Phase> getAvailabelePhasesForStage(int objectiveId, int stageId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Phase> phases = new ArrayList<Phase>();



        String whereQuery = " "+SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(stageId);

        // disable previous phases
        /*
        Cursor cursor = db.rawQuery("SELECT " + SQLiteHelper.HIERARCHY+" FROM "+SQLiteHelper.TABLE_PHASES+" WHERE "+SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.STAGE_ID + " = " + Integer.toString(stageId) +" AND "+SQLiteHelper.ID+" = "+phaseId, null);//db.query(true, SQLiteHelper.TABLE_PHASES, from, where, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("CURSOR", cursor.getString(0).toString());

            whereQuery += " AND " + SQLiteHelper.HIERARCHY + " >= " + cursor.getString(0).toString();

        }

        */

        Map<Integer, Integer> tmp_phases = new HashMap<Integer, Integer>();
        Cursor cursor = db.rawQuery("SELECT p.id, pov.days FROM "+SQLiteHelper.TABLE_PHASE_OBJ_VALUES+" pov " +
                "INNER JOIN phases p ON p.id=pov.phase_id WHERE "+SQLiteHelper.STAGE_ID+"="+stageId+" AND "+SQLiteHelper.OBJECTIVE_ID+" = "+objectiveId, null);


        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Log.d("SpinPhase", " " + cursor.getInt(0) + " = " + cursor.getInt(1));
                tmp_phases.put(cursor.getInt(0), cursor.getInt(1));
                cursor.moveToNext();
            }
        }
        cursor.close();

        Log.d("CURSOR", cursor.toString() + "  \n " + "SELECT p.id, pov.days FROM "+SQLiteHelper.TABLE_PHASE_OBJ_VALUES+" pov " +
                "INNER JOIN phases p ON p.id=pov.phase_id WHERE "+SQLiteHelper.STAGE_ID+"="+stageId+" AND "+SQLiteHelper.OBJECTIVE_ID+" = "+objectiveId);


        String queryPhases = "SELECT * FROM " + SQLiteHelper.TABLE_PHASES + " WHERE " +
                whereQuery +
                "  ORDER BY " + SQLiteHelper.TABLE_PHASES + "." + SQLiteHelper.HIERARCHY;


        Cursor cursorPhases = db.rawQuery(queryPhases, null);
        cursorPhases.moveToFirst();

        while (!cursorPhases.isAfterLast()) {

            int objDays = (tmp_phases.get(cursorPhases.getInt(0)) != null)?tmp_phases.get(cursorPhases.getInt(0)):cursorPhases.getInt(3);

            Log.d("SpinPhase", " " + cursorPhases.getInt(0)+" = "+cursorPhases.getInt(3)+ " / "+objDays+"  => "+ tmp_phases.get(cursorPhases.getInt(0)));

            phases.add(new Phase(cursorPhases.getInt(0), cursorPhases.getInt(1),
                    cursorPhases.getString(2), objDays,cursorPhases.getInt(4),
                    cursorPhases.getInt(5)));

            cursorPhases.moveToNext();
        }


        cursorPhases.close();
        db.close();

        return phases;
    }
}
