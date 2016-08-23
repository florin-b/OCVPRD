package com.stimasoft.obiectivecva;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stimasoft.obiectivecva.utils.maps.ObjectDialog;


public class MapPopup extends AppCompatActivity  implements ObjectDialog.Communicator{

    GPSTracker gps;
    Marker GPS_location = null;
    private GoogleMap map;
    LatLng newMarkerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_popup);

        // SET UP Google Map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(map.MAP_TYPE_NORMAL);
        // display zoom in/out button on map
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);


        // Call DialogBox to create new Objective
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Flag to check if selected location is in marker area, for editing
                Boolean checkMarkerEdit = false;

                // check if marker is editable

                    newMarkerPosition = latLng;

                FragmentManager manager = getFragmentManager();
                    ObjectDialog ObjDialog = new ObjectDialog();

                    ObjDialog.show(manager, "NewObjective");

            }
        });

        if (GPS_location != null) {
            GPS_location.remove();
            GPS_location = null;
        }

        gps = new GPSTracker(MapPopup.this);

        if (gps.isCanGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getlongitude();

            LatLng currentPosition = new LatLng(latitude, longitude);

            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentPosition, 12);
            map.animateCamera(update);

        } else {
            gps.showSettingsAlert();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_popup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Dialog Box implemented form ObjectDialog interface, read answer, create new Objective
    @Override
    public void onDialogCreateObjectiveAnswer(Boolean answer) {

        if (answer) {

            // we do not use it like this, we use clustering to add marker to map!
            Marker newMarker =  map.addMarker(new MarkerOptions().position(newMarkerPosition)               // create new marker
                    .title("Obiectiv Nou")
                            //.snippet("Instalatii;Sanitare;12-05-2015")
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker0))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

//TODO: @Andrei - use new GPS position to create Objects: dupa salvarea obiectivului trebuie apelat urmatorul cod.

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(newMarkerPosition, 21);         // Zoom to new marker location
            map.animateCamera(update);

        }

    }
}
