package com.stimasoft.obiectivecva.utils.maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by filip on 25/06/2015.
 */

public class ObjectiveClusterRenderer extends DefaultClusterRenderer<ObjectiveItem> {
    public ObjectiveClusterRenderer(Context context, GoogleMap map, ClusterManager<ObjectiveItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(ObjectiveItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getSnippet());

// @TODO - set an appropriate color for marker
        // set icon color
        //expirationPhase
        if(item.getSnippet() != null) {
            JsonElement snippet = new JsonParser().parse(item.getSnippet());

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            if((snippet.getAsJsonObject().get("expired") != null) && (snippet.getAsJsonObject().get("expired").getAsBoolean()))
            {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        }
    }

    @Override
    protected void onClusterItemRendered(ObjectiveItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        //here you have access to the marker itself
    }
}
