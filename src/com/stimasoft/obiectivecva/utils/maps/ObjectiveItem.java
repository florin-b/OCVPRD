package com.stimasoft.obiectivecva.utils.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ObjectiveItem implements ClusterItem {
    private final LatLng mPosition;

    private final String title;
    private final String  snippet;

    private double lat = 0;
    private double lng = 0;

    public ObjectiveItem(double v_lat, double v_lng, String v_title, String v_snippet) {
        lat = v_lat;
        lng = v_lng;

        mPosition = new LatLng(v_lat, v_lng);

        title = v_title;
        snippet = v_snippet;
    }

    public ObjectiveItem(LatLng v_position, String v_title, String v_snippet) {
        mPosition = v_position;
        title = v_title;
        snippet = v_snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getSnippet()
    {
        return snippet;
    }

    public String getTitle(){
        return title;
    }
}
