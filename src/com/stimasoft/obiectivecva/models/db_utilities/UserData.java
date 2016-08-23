package com.stimasoft.obiectivecva.models.db_utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Convenience class for handling logging database operations
 */
public class UserData {
    private SQLiteHelper dbHelper;
    private Context context;

    public UserData(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);

    }

    /**
     * Mock method for generating all the users asociated with a DVA
     *
     * @param dvaCode Not used in this release. Dva code used to get the cva list
     * @return List of CVAs associated with the specified DVA
     */
    public ArrayList<User> getUsersForDva(String dvaCode){
        // dvaCode not used yet
        ArrayList<User> cvas = new ArrayList<User>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + SQLiteHelper.TABLE_USERS + " u" +
                        " WHERE u." + SQLiteHelper.TYPE_ID + " = " + "2";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            while(!cursor.isAfterLast()){
                cvas.add(new User(cursor.getInt(0), cursor.getString(1), cursor.getInt(2),
                                    cursor.getString(3), cursor.getString(4), cursor.getString(5)));

                cursor.moveToNext();
            }

            cursor.close();
            db.close();
            return cvas;
        }

        cursor.close();
        db.close();
        return null;
    }

    /**
     * Generates a query value string based on an array list of users
     *
     * @param users The users used to filter
     * @return String that can be appended to an sql query in order to filter by multiple CVAs
     */
    public String generateCvaCodesString(ArrayList<User> users){
        String cvaCodes = "";

        ListIterator<User> iterator = users.listIterator();

        while(iterator.hasNext()){
            if(iterator.nextIndex() == users.size() - 1){
                cvaCodes += iterator.next().getCode();
            }
            else {
                cvaCodes += iterator.next().getCode() + ", ";
            }
        }

        return cvaCodes;
    }
}
