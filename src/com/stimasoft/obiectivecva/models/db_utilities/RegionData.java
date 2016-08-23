package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import java.util.ArrayList;

/**
 * Convenience class for handling region database operations
 */
public class RegionData {

    private SQLiteHelper dbHelper;
    private Context context;

    public RegionData(Context context) {
        this.context = context;
        dbHelper = new SQLiteHelper(context);
    }

    /**
     * Queries the database for all the registered regions
     *
     * @return List of all the regions in the database
     */
    public ArrayList<Region> getAllRegions(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ArrayList<Region> regions = new ArrayList<Region>();

        String query = "SELECT * FROM " + SQLiteHelper.TABLE_REGIONS;

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            regions.add(new Region(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));

            cursor.moveToNext();
        }

        cursor.close();

        return regions;
    }

    /**
     * Gets a region based on a provided id
     *
     * @param id The id of the requested region
     * @return Region with id matching the provided one
     */
    public Region getRegionById(int id){
        Region region;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + SQLiteHelper.TABLE_REGIONS +
                        " WHERE " + SQLiteHelper.TABLE_REGIONS + "." + SQLiteHelper.ID + " = " + Integer.toString(id);

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0){
            region = new Region(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));

            cursor.close();
            db.close();
            return region;
        }

        cursor.close();
        db.close();
        return null;
    }

    /**
     * Gets the region id of the logged in CVA based on the cva's branch code
     *
     * @param branchCode CVA's branch code
     * @return The id of the CVA's region or -1 if no region was found
     */
    public int getCVARegionId(String branchCode){
        int regionId = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT r." + SQLiteHelper.ID +
                        " FROM " + SQLiteHelper.TABLE_REGIONS + " r, " + SQLiteHelper.TABLE_BRANCHES + " b" +
                        " WHERE b." + SQLiteHelper.REGION_CODE + " = r." + SQLiteHelper.CODE +
                        " AND b." + SQLiteHelper.NAME + " = '" + branchCode + "'";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        if(cursor.getCount() > 0)
            regionId = cursor.getInt(0);

        return regionId;
    }
}
