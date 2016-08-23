package com.stimasoft.obiectivecva.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.User;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Helper method for storing information in the shared preferences
 */
public class SharedPrefHelper {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Context context;

    private static final String PREF_NAME="Obiective_Users";
    private static final int PRIVATE_MODE = 0;

    private static final String KEY_USER = "user";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_AUTHENTICATED = "authenticated";
    private static final String KEY_FILTERS = "filters";

    public SharedPrefHelper(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
        this.context = context;
    }

    /**
     * Stores the user's details in the shared preferences and marks the status as logged in.
     *
     * @param user The user whose details will be stored
     */
    public void logIn(User user){
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER,json);
        editor.putBoolean(KEY_IS_AUTHENTICATED, true);
        editor.commit();
        Toast.makeText(context, context.getString(R.string.authenticator_logIn_message) + user.getName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Removes the user's details from the shared preferences and marks the status as logged out
     */
    public void logOut(){
        editor.remove(KEY_USER);
        editor.putBoolean(KEY_IS_AUTHENTICATED, false);
        editor.commit();
    }

    /**
     * Checks if any user is logged in
     *
     * @return True if the user is logged in or false if not
     */
    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(KEY_IS_AUTHENTICATED, false);
    }

    /**
     * @return The user details stored in shared preferences as an instance of the User class
     */
    public User getUserDetails(){
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_USER, "");
        return gson.fromJson(json, User.class);
    }

    /**
     * Commits to shared preferences the filters HashMap si that it may persist between activities
     *
     * @param filters The filter arguments to be commited to shared preferences.
     */
    public void setFilters(HashMap<String, Pair<String,String>> filters){
        Gson gson = new Gson();
        String json = gson.toJson(filters);
        editor.putString(KEY_FILTERS,json);
        editor.commit();
    }

    /**
     * @return The stored filters or a new hashmap if there aren't any
     */
    public HashMap<String, Pair<String, String>> getFilters(){
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Pair<String,String>>>(){}.getType();

        String filtersString =sharedPreferences.getString(KEY_FILTERS, "");

        HashMap<String, Pair<String, String>> result = gson.fromJson(filtersString, type);

        if(result != null)
            return result;

        else return new HashMap<String, Pair<String, String>>();
    }

    /**
     * Deletes all the stored filters
     */
    public void clearFilters(){
        editor.remove(KEY_FILTERS);
        editor.commit();
    }
}
