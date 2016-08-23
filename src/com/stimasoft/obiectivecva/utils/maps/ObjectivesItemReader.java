package com.stimasoft.obiectivecva.utils.maps;

import android.content.Context;

import com.stimasoft.obiectivecva.models.db_utilities.ObjectiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ObjectivesItemReader {

    /*
     * This matches only once in whole input,
     * so Scanner.next returns whole InputStream as a String.
     * http://stackoverflow.com/a/5445161/2183804
     */
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";

    private ObjectiveData objectiveData;

    public List<ObjectiveItem> read(InputStream inputStream) throws JSONException {

        List<ObjectiveItem> items = new ArrayList<ObjectiveItem>();

        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {

            JSONObject object = array.getJSONObject(i);

            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");

            String title = object.getString("title");
            String snippet = object.getString("snippet");

            items.add(new ObjectiveItem(lat, lng, title, snippet));
        }

        return items;
    }

    public List<ObjectiveItem> getMapData(Context context, HashMap params){
        // List<ObjectiveItem> items = new ArrayList<ObjectiveItem>();
        // return items;
        return new ObjectiveData(context).readMapData(params);
    }
}
