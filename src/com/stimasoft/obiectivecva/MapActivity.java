package com.stimasoft.obiectivecva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;
import com.stimasoft.obiectivecva.adapters.RegionSpinnerAdapter;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.models.db_utilities.RegionData;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.FilterUtils;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.Setup;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;
import com.stimasoft.obiectivecva.utils.StagePhaseSpinnerUtils;
import com.stimasoft.obiectivecva.utils.maps.ObjectDialog;
import com.stimasoft.obiectivecva.utils.maps.ObjectInfoWindowDialog;
import com.stimasoft.obiectivecva.utils.maps.ObjectiveClusterRenderer;
import com.stimasoft.obiectivecva.utils.maps.ObjectiveItem;
import com.stimasoft.obiectivecva.utils.maps.ObjectivesItemReader;
import com.stimasoft.obiectivecva.utils.ui.DatePickerDialogFragment;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


//implements GoogleMap.OnMarkerClickListener
public class MapActivity extends AppCompatActivity implements ObjectDialog.Communicator, ObjectInfoWindowDialog.InfoWinCommunicator,
                                                            DatePickerDialogFragment.SetLimitsInterface{

    private GoogleMap map;
    private ClusterManager<ObjectiveItem> mClusterManager;

    private boolean filtersExpanded = false;
    private boolean flagLaunchedFromList = false;

    private final LatLng LOCATION_BUCHAREST = new LatLng(44.4378258, 26.0946376);
    private final LatLng LOCATION_ILFOV = new LatLng(44.491659, 26.1391725);
    private final LatLng LOCATION_ROMANIA = new LatLng(45.9419466, 25.0094284);

 // stage and phase filter, Author: Alin
 	private int lastSelectedStageId = -1;
 // stage and phase filter, Author: Alin
 	private boolean objectiveTypeChanged = false;
    
    LinearLayout filters;

    HashMap<String, Marker> lastOpenedMarkers = new HashMap<String, Marker>();

    Marker GPS_location, infoWindowMarker = null;

    LatLng newMarkerPosition;

    int year_x, month_x, day_x;

    GPSTracker gps;

    static final int ZOOM_COUNTRY = 7;
    static final int ZOOM_COUNTY = 10;
    static final int ZOOM_CITY = 12;
    static final int ZOOM_SECTOR = 13;
    static final int ZOOM_PLACE = 22;

    User user;

    HashMap<String, Pair<String, String>> map_filter_params = new HashMap<String, Pair<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        user = sharedPrefHelper.getUserDetails();

        Bundle bundle = getIntent().getExtras();
        flagLaunchedFromList = bundle.getBoolean(Constants.KEY_LIST_LAUNCH, false);

        map_filter_params = new HashMap<String, Pair<String, String>>();

//        setContentView(R.layout.activity_map);

        // SET UP Google Map



        Boolean populateMap = true;
        switch (user.getUserType()) {
            case User.TYPE_DIRECTOR:
            case User.TYPE_DVA:
                populateMap = false;
                setContentView(R.layout.activity_map_dva);

                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

                Spinner regions = (Spinner) findViewById(R.id.spinner_filter_region);
                populateRegionsSpinner(regions);


                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_ROMANIA, ZOOM_COUNTRY);
                map.animateCamera(update);

                break;
            default:
                setContentView(R.layout.activity_map);
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        // define slide menu options
        final DrawerLayout drawerLayoutLeft = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navView_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        Setup setup = new Setup(this);
        setup.setupToolbar(toolbar);
        setup.setupDrawer(drawerLayoutLeft, navView, toolbar);
        setup.setupDrawerHeader(navView, user);
        setup.setupDrawerMenu(navView, drawerLayoutLeft, R.id.drawer_menu_obiective);
        filters = (LinearLayout) findViewById(R.id.layout_filters);

     // stage and phase objective, Author: Alin

     		final Spinner spinnerStageObject = (Spinner) findViewById(R.id.spinner_filter_stage_objective);
     		final Spinner spinnerPhaseObject = (Spinner) findViewById(R.id.spinner_filter_phase_objective);

     		final StagePhaseSpinnerUtils spsUtils = new StagePhaseSpinnerUtils(this);

     		spsUtils.populateAvailableStagesSpinner(spinnerStageObject, -1, 0, spinnerStageObject.getSelectedItemPosition());
     		lastSelectedStageId = ((Stage) spinnerStageObject.getSelectedItem()).getId();

     		spinnerStageObject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

     			@Override
     			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
     				// TODO Auto-generated method stub
     				int selectedStageId = ((Stage) adapterView.getSelectedItem()).getId();
     				int selectedPhasePosition = spinnerPhaseObject.getSelectedItemPosition();

     				if (selectedStageId != lastSelectedStageId || objectiveTypeChanged) {
     					spsUtils.populateAvailablePhasesSpinner(spinnerPhaseObject, -1, selectedStageId);

     					if (selectedStageId == lastSelectedStageId) {
     						spinnerPhaseObject.setSelection(selectedPhasePosition);
     					}
     					lastSelectedStageId = selectedStageId;
     					objectiveTypeChanged = false;
     				}

     			}

     			@Override
     			public void onNothingSelected(AdapterView<?> adapterView) {
     				// TODO Auto-generated method stub

     			}

     		});

     		// end stage and phase objective
        
        
        ImageButton hideFilters = (ImageButton) findViewById(R.id.button_hideFilters);
        hideFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawers();
            }
        });

        Button applyFilters = (Button) findViewById(R.id.button_applyFilters);
        applyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard();

                FilterUtils filterUtils = new FilterUtils(MapActivity.this);

                HashMap<String, Pair<String, String>> whereFilters = filterUtils.generateFilterWhere();

                SharedPrefHelper filter_param = new SharedPrefHelper(MapActivity.this);
                filter_param.setFilters(whereFilters);

                readItems(whereFilters);

                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawers();
            }
        });

        Button clearFilters = (Button) findViewById(R.id.button_clearFilters);
        clearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Setup setup = new Setup(MapActivity.this);
                sharedPrefHelper.clearFilters();
                map_filter_params = new HashMap<String, Pair<String, String>>();
                map_filter_params.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
                setup.clearFilters();

                //TODO @Filip trebuie sa resetezi harta la modu initial fara filtre aici

                readItems(map_filter_params);

                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawers();
            }
        });

        //Hardcoded for ongoing objectives
        setup.setupFilters(filters, 1, null);

        // setare Cluster Manager - pentru afisarea marker-elor grupate
        mClusterManager = new ClusterManager<ObjectiveItem>(this, map);
        mClusterManager.setRenderer(new ObjectiveClusterRenderer(this, map, mClusterManager));
        map.setOnCameraChangeListener(mClusterManager);

        // display zoom in/out button on map
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);
        
        map.setMyLocationEnabled(true);
        
        
      //add location button click listener
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                //TODO: Any custom actions
            	
            	getGPSLocation();
            	
                return true;
            }
        });
        
        // populate map
        //if(user.getUserType() == 2) map_filter_params.put("cod_dva", user.getCode());
        if(populateMap)
        {
            SharedPrefHelper params = new SharedPrefHelper(this);
            HashMap <String, Pair<String, String>> tmp_params = params.getFilters();
            if(tmp_params != null) {
                readItems(tmp_params);
            }else {
                readItems(map_filter_params);
            }
            //readItems(map_filter_params);
        }


        Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        // Reset map elements on click
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastOpenedMarkers.clear();
                hideKeyboard();
            }
        });

        // Long click available only for CVA user
        if (user.getUserType() == User.TYPE_CVA) {

            // Call DialogBox to create new Objective
            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    // Flag to check if selected location is in marker area, for editing
                    Boolean checkMarkerEdit = false;

                    // check if marker is editable
                    MarkerManager.Collection markerCollection = mClusterManager.getMarkerCollection();  // read markers from Cluster Manager
                    Collection<Marker> markers = markerCollection.getMarkers();                         // create collection of markers

                    // check each marker for selected location
                    for (Marker m : markers) {
                        // if marker is in the selected area do Edit Objective
                        if ((Math.abs(m.getPosition().latitude - latLng.latitude) < 0.000015) && (Math.abs(m.getPosition().longitude - latLng.longitude) < 0.000015)) {
                            //TODO: Andrei - call Edit Objective Activity
                            Intent i = new Intent(getApplicationContext(), AddEditObjective.class);
                            i.putExtra(Constants.KEY_COORDINATES, m.getPosition().latitude + "," + m.getPosition().longitude);
                            i.putExtra(Constants.KEY_PURPOSE, Constants.VALUE_EDIT);
                            startActivityForResult(i, Constants.CODE_EDIT_FROM_MAP);

                            checkMarkerEdit = true;
                            break;
                        }
                    }

                    // if ther is no marker in this area, create new one. Call dialog box
                    if (!checkMarkerEdit) {
                        newMarkerPosition = latLng;

                        FragmentManager manager = getFragmentManager();
                        ObjectDialog ObjDialog = new ObjectDialog();

                        ObjDialog.show(manager, "NewObjective");
                    }
                }
            });
        }


        // Display custom Info Window on click
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.getTitle() != null) {

                    // Zoom on marker position
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ZOOM_PLACE);
                    map.animateCamera(update);

                    // Check if info window is already displayed
                    if (lastOpenedMarkers.get(marker.getId()) == null) {
                        lastOpenedMarkers.put(marker.getId(), marker);       // add markers to list
                        marker.showInfoWindow();
                        return true;
                    } else {
                        marker.hideInfoWindow();
                        lastOpenedMarkers.remove(marker.getId());           // remove marker from list
                        return true;
                    }
                }
                marker.hideInfoWindow();
                // Zoom on marker position
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ZOOM_PLACE);
                map.animateCamera(update);
                return true;
            }
        });

        // Create custom Info Window -  popup window for Marker item
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file popup_layout
                View v = getLayoutInflater().inflate(R.layout.popup_layout, null);

                // Getting the position from the marker
                //LatLng latLng = marker.getPosition();

                // Getting reference to the TextView to set title
                TextView title = (TextView) v.findViewById(R.id.txtTitle);
                title.setText(marker.getTitle());

                // Check if marker has no details, do not populate Info Window
                if (marker.getTitle() != null && marker.getSnippet() != null) {


                    JsonElement snippet = new JsonParser().parse(marker.getSnippet());
                    
                    // Set information for Objective Number, Author: Alin
                    if (snippet.getAsJsonObject().get("id") != null)
                    {
                    	String idVal = snippet.getAsJsonObject().get("id").getAsString();
                    	TextView idNo = (TextView) v.findViewById(R.id.txtNumber);
                    	idNo.setText(idVal);
                    }
                    // End Objective Number
                    
                    // Set information for Stage
                    if (snippet.getAsJsonObject().get("stage") != null) {
                        String stageVal = snippet.getAsJsonObject().get("stage").getAsString();

                        TextView stage = (TextView) v.findViewById(R.id.txtStage);
                        stage.setText(stageVal);
                    }
                    // Set information for Phase
                    if (snippet.getAsJsonObject().get("phase") != null) {
                        String phaseVal = snippet.getAsJsonObject().get("phase").getAsString();
                        TextView phase = (TextView) v.findViewById(R.id.txtPhase);
                        phase.setText(phaseVal);
                    }
                    // Set information for Expiration Phase Date
                    if (snippet.getAsJsonObject().get("expirationPhaseEnd") != null) {
                        String expirationPhaseEndVal = snippet.getAsJsonObject().get("expirationPhaseEnd").getAsString();

                        Date date = null;
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd").parse(expirationPhaseEndVal);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Log.d("ERR_DateFormat:", "ObjectivesData, eroare conersie data -> readMapData()\n" + e.getMessage());
                        }
                        String dateShow = new SimpleDateFormat("dd-MM-yyyy").format(date);

                        TextView expirationPhase = (TextView) v.findViewById(R.id.txtExpirationDate);
                        expirationPhase.setText(dateShow);

                        if (user.getUserType() != User.TYPE_CVA) {
                            TextView txtInfoWindow = (TextView) v.findViewById(R.id.txtInfoWindow);
                            txtInfoWindow.setText("");
                        }
                    }

                }
                // Returning the view containing InfoWindow contents
                return v;

            }
        });

        // change date can do only CVA
        if (user.getUserType() == User.TYPE_CVA) {

            // Display Calendar Dialog to change expiration date
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                    // Get information from marker, extract expiration date and use it like a current date in Calendar Dialog
                    Calendar expPhaseDateStart = new GregorianCalendar();
                    Calendar expPhaseDateEnd = new GregorianCalendar();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    int stageIdVal = 0;
                    int phaseIdVal = 0;
                    String daysVal = null;
                    String stageName = null;
                    String phaseName = null;

                    int objectiveId = 0;


                    if (marker.getSnippet() != null) {

                        JsonElement snippet = new JsonParser().parse(marker.getSnippet());

                        if (snippet.getAsJsonObject().get("stage") != null) {

                            String expirationPhaseStart = snippet.getAsJsonObject().get("expirationPhaseStart").getAsString();
                            String expirationPhaseEnd = snippet.getAsJsonObject().get("expirationPhaseEnd").getAsString();

                            objectiveId = snippet.getAsJsonObject().get("id").getAsInt();

                            stageIdVal = snippet.getAsJsonObject().get("stage_id").getAsInt();
                            phaseIdVal = snippet.getAsJsonObject().get("phase_id").getAsInt();
                            daysVal = snippet.getAsJsonObject().get("days").getAsString();

                            stageName = snippet.getAsJsonObject().get("stage").getAsString();
                            phaseName = snippet.getAsJsonObject().get("phase").getAsString();

                            try {
                                expPhaseDateEnd.setTime(sdf.parse(expirationPhaseEnd));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                expPhaseDateStart.setTime(sdf.parse(expirationPhaseStart));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    FragmentManager manager = getFragmentManager();
                    ObjectInfoWindowDialog ObjDialogInfoWindow = new ObjectInfoWindowDialog();

                    Date alertDateStart = expPhaseDateStart.getTime();
                    String alertDateStartValue = sdf.format(alertDateStart);                        // send parameter string date to dialogBox
                    ObjDialogInfoWindow.setExpirationStart(alertDateStartValue);


                    Date alertDateEnd = expPhaseDateEnd.getTime();
                    String alertDateEndValue = sdf.format(alertDateEnd);                            // send parameter string date to dialogBox
                    ObjDialogInfoWindow.setExpirationEnd(alertDateEndValue);


                    ObjDialogInfoWindow.setObjectiveId(objectiveId);

                    ObjDialogInfoWindow.setStageId(stageIdVal);
                    ObjDialogInfoWindow.setPhaseId(phaseIdVal);


                    ObjDialogInfoWindow.setStageName(stageName);
                    ObjDialogInfoWindow.setPhaseName(phaseName);


                    ObjDialogInfoWindow.setDaysVal(daysVal);

                    ObjDialogInfoWindow.show(manager, "NewObjectiveInfoWindow");

                    infoWindowMarker = marker;    // set current Marker for details regarding Objective
                    marker.hideInfoWindow();    // hide Info Window of the current marker
                }
            });
        }

        int drawerFlag = bundle.getInt(Constants.KEY_FLAG);

        switch(drawerFlag){
            case Constants.FLAG_FILTERS_OPEN:
                drawerLayoutLeft.openDrawer(GravityCompat.END);
                break;

            default: break;
        }

    }


    @Override
    public void onResume(){
        super.onResume();
        Bundle bundle = getIntent().getExtras();

        if(user.getUserType() != User.TYPE_CVA) {
            final DrawerLayout drawerLayoutLeft = (DrawerLayout) findViewById(R.id.drawer_layout);

            int drawerFlag = bundle.getInt(Constants.KEY_FLAG);

            switch (drawerFlag) {
                case Constants.FLAG_FILTERS_OPEN:
                    drawerLayoutLeft.openDrawer(GravityCompat.END);
                    break;

                default:
                    break;
            }

            if(flagLaunchedFromList){
                SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
                HashMap<String, Pair<String, String>> whereFilters = sharedPrefHelper.getFilters();
                whereFilters.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));

                readItems(whereFilters);

            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!flagLaunchedFromList){
            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
            sharedPrefHelper.clearFilters();
        }

    }
    /**
     * Read temporary JSON objectives from file.
     *
     * @throws JSONException
     */
    private void readItems(HashMap<String, Pair<String, String>> map_filter_params) {

        map.clear();                                // reset map from items
        mClusterManager.clearItems();               // reset ClusterManager list of markers

        if(map_filter_params == null)
            map_filter_params = new HashMap<String, Pair<String, String>>();

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Commit changes
        LatLng tmpGps = LOCATION_ROMANIA;
        int tmpZoom = ZOOM_COUNTRY;

        // prepare default loadin of markers

        // get Cod CVA of the current user
        switch (user.getUserType()) {
            case User.TYPE_CVA:
                map_filter_params.put(SQLiteHelper.CVA_CODE, new Pair<String, String>("=", "'" + user.getCode() + "'"));
                break;
            case User.TYPE_DVA:
                // TODO - @Andrei - creem functie pentru DVA ce returneaza codurile CVA-ului din subordine
                //map_filter_params.put(SQLiteHelper.CVA_CODE, new Pair<>(" IN ", "('65987423', '65987423', '31687457', '98756329', '45654789', '30215982', '63026987', '63026987', '98652369')"));
            case User.TYPE_DIRECTOR:
                //
        }

        if (map_filter_params != null) {

            if (map_filter_params.containsKey("map_gps")) {

                Pair<String, String> item = map_filter_params.get("map_gps");
                map_filter_params.remove("map_gps");

                String[] latlong = item.second.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);

                tmpGps = new LatLng(latitude, longitude);
                Log.d("ZOMMACTION", "GPS: " + tmpGps.toString());
            }

            if (map_filter_params.containsKey("map_zoom")) {
                Pair<String, String> item = map_filter_params.get("map_zoom");
                map_filter_params.remove("map_zoom");
                tmpZoom = Integer.parseInt(item.second); //Integer.parseInt(map_filter_params.get("map_zoom"));
                Log.d("ZOMMACTION", "ZOOM: ---- " + tmpZoom);
            }


        }

        //params.put("Obj.regionId","1");
        //params.put("Obj.phaseId","1");
        //params.put("Obj.stageId","1");
        //params.put("Obj.stageId","1");
        // params.put("Obj.expirationPhase", "30-06-2015");
        //params.put("cva_code","30215982");

        map_filter_params.put(SQLiteHelper.STATUS, new Pair<String, String>("=", Integer.toString(1)));

        List<ObjectiveItem> items = new ObjectivesItemReader().getMapData(this, map_filter_params);

        // set cluster with objectives
        mClusterManager.addItems(items);                                                            // add new item to ClusterManager to create marker on Map
        mClusterManager.setRenderer(new ObjectiveClusterRenderer(this, map, mClusterManager));      // render Cluster Manager, not tested yet with bigger number of markers

//        mClusterManager.addItem(new ObjectiveItem(LOCATION_BUCHAREST, "Bucuresti City", "Instalatii;Sanitare;28-06-2015"));
//        mClusterManager.addItem(new ObjectiveItem(LOCATION_ILFOV, "Judetul Ilfov", "Instalatii;Electrice;15-07-2015"));
//        mClusterManager.addItem(new ObjectiveItem(LOCATION_ROMANIA, "Tara Romania", "Instalatii;Incalzire;02-08-2015"));

        // Zoom Google Map to fit markers in the screen
        if (items.size() > 0) {

            LatLngBounds.Builder b = new LatLngBounds.Builder();

            for (ObjectiveItem m : items) {
                b.include(m.getPosition());
            }

            LatLngBounds bounds = b.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500, 500, 10));
        } else {
            Log.d("ZOMMACTION", tmpGps.toString() + " ---- " + tmpZoom);
            // First zoom on map
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(tmpGps, tmpZoom);
            map.animateCamera(update);
        }
    }

    // Check if GPS is available and read current postition
    private void getGPSLocation() {

        if (GPS_location != null) {
            GPS_location.remove();
            GPS_location = null;
        }

        gps = new GPSTracker(MapActivity.this);

        if (gps.isCanGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getlongitude();

            // display text in buttom of the window

            LatLng currentPosition = new LatLng(latitude, longitude);

            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentPosition, ZOOM_PLACE);
            map.animateCamera(update);
            
            map.setMyLocationEnabled(true);
            
        } else {
            gps.showSettingsAlert();
        }
    }

    @Override
    public void onDialogInfoWindowAnswer(HashMap<String, Object> answer) {
        if (answer.containsKey("answer") && ((Boolean) answer.get("answer"))) {
            SharedPrefHelper params = new SharedPrefHelper(this);
            readItems(params.getFilters());
            //String datePicker = answer.get("datePicker").toString();
        }
    }

    // Dialog Box implemented form ObjectDialog interface, read answer, create new Objective
    @Override
    public void onDialogCreateObjectiveAnswer(Boolean answer) {

        if (answer) {
            /*
            // we do not use it like this, we use clustering to add marker to map!
            Marker newMarker =  map.addMarker(new MarkerOptions().position(newMarkerPosition)               // create new marker
                    .title("Obiectiv Nou")
                            //.snippet("Instalatii;Sanitare;12-05-2015")
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker0))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

            listOfMarker.put(newMarker.getId(), newMarker);                                                 // Add new marker to existing list
            */

            Intent i = new Intent(getApplicationContext(), AddEditObjective.class);
            i.putExtra(Constants.KEY_COORDINATES, newMarkerPosition.latitude + "," + newMarkerPosition.longitude);
            i.putExtra(Constants.KEY_PURPOSE, Constants.VALUE_ADD);
            startActivityForResult(i, Constants.CODE_ADD_FROM_MAP);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_map, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();

        if (user.getUserType() == User.TYPE_DVA || user.getUserType() == User.TYPE_DIRECTOR) {
            inflater.inflate(R.menu.menu_map_dva, menu);



            /*
            Spinner regions = (Spinner) findViewById(R.id.spinner_dva_filter_region);
            populateRegionsSpinner(regions);

            regions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    Region region = (Region) parent.getSelectedItem();
                    searchOnMap(region);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            ImageButton btn_search = (ImageButton) findViewById(R.id.btn_search);
            btn_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Spinner spinner = (Spinner) findViewById(R.id.spinner_dva_filter_region);
                    Region region = (Region) spinner.getSelectedItem();
                    searchOnMap(region);
                }
            });

            */
        } else {
            //Toast.makeText(getApplicationContext(), "User Type CVA: " + user.getUserType(), Toast.LENGTH_LONG).show();
            inflater.inflate(R.menu.menu_map_cva, menu);
        }


        return super.onCreateOptionsMenu(menu);
    }
/*
    private void searchOnMap(Region region) {

        hideKeyboard();

        HashMap<String, Pair<String, String>> map_params = new HashMap<>();

        if (region.getId() > 0 && region.getGps() != null) {

            if (region.getCode() == "B") {
                // TODO: @Andrei - trebuie creata functia care ne aduce ID regiunilor pentru Sectoare Bucurestiului
                map_params.put(SQLiteHelper.REGION_ID, new Pair<>("=", "" + region.getId()));
            } else {
                map_params.put(SQLiteHelper.REGION_ID, new Pair<>("=", "" + region.getId()));
            }

            map_params.put("map_gps", new Pair<>("map_gps", "" + region.getGps()));

            String[] VALUES = new String[]{"S1", "S2", "S3", "S4", "S5", "S6"};

            if (Arrays.asList(VALUES).contains(region.getCode())) {
                map_params.put("map_zoom", new Pair<>("map_zoom", "" + ZOOM_SECTOR));
            } else {
                map_params.put("map_zoom", new Pair<>("map_zoom", "" + ZOOM_COUNTY));
            }
        }

        SearchView searchCodCVA = (SearchView) findViewById(R.id.search_cva_cod);
        String cod_cva = searchCodCVA.getQuery().toString().trim();

        if (cod_cva.length() > 0) {
            map_params.put(SQLiteHelper.CVA_CODE, new Pair<>("=", "'" + cod_cva + "'"));
        }
        readItems(map_params);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}


        switch (id) {
	        case R.id.mapModHybrid:
	        	map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	        	return true;
	        	
	        case R.id.mapModNormal:
	        	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	        	return true;
	        	
	        case R.id.mapModTerrain:
	        	map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	        	return true;    
        	
            case R.id.menu_search:
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(findViewById(R.id.navView_drawerRight));
                return true;

            case R.id.menu_my_map:
                /*SharedPrefHelper params = new SharedPrefHelper(this);
                HashMap <String, Pair<String, String>> tmp_params = params.getFilters();
                if(tmp_params != null) {
                    readItems(tmp_params);
                }else {
                    readItems(map_filter_params);
                }*/
                map_filter_params = new HashMap<String, Pair<String, String>>();
                map_filter_params.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
                readItems(map_filter_params);

                return true;

            case R.id.menu_my_location:
                getGPSLocation();
                return true;

            case R.id.action_switch_list:
                if((user.getUserType() == User.TYPE_DVA || user.getUserType() == User.TYPE_DIRECTOR)
                        &&!flagLaunchedFromList)
                {
                    Intent intent = new Intent(getApplicationContext(), Objectives.class);
                    intent.putExtra(Constants.KEY_MAP_LAUNCH, true);
                    intent.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ONGOING);
                    intent.putExtra(Constants.KEY_FLAG, Constants.FLAG_FILTERS_CLOSED);
                    startActivity(intent);
                }else{
                    finish();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<Region> populateRegionsSpinner(Spinner spinner) {
        RegionData regionData = new RegionData(this);
        ArrayList<Region> regions = regionData.getAllRegions();
        regions.add(0, new Region(0, "Orice regiune", "OR"));

        RegionSpinnerAdapter adapter = new RegionSpinnerAdapter(this,
                android.R.layout.simple_spinner_item,
                regions);

        spinner.setAdapter(adapter);

        return regions;
    }

    @Override
    public void onDateSelected(Calendar limit, int modifierPurpose, int modifierType, int changeTarget, int limitTarget) {
        // Change the date in the targeted date field
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);
        TextView dateToChange = (TextView) findViewById(changeTarget);
        dateToChange.setText(sdf.format(limit.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Constants.CODE_ADD_FROM_MAP || requestCode == Constants.CODE_EDIT_FROM_MAP) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                SharedPrefHelper filter_param = new SharedPrefHelper(this);
                readItems(filter_param.getFilters());
            }
        }
    }


    // hide keyboard
    public void hideKeyboard() {
        // Check if no view has focus:

//        View view = this.getCurrentFocus();
//        if (view != null) {
//            Toast.makeText(getApplicationContext(), "Must hide keyboard", Toast.LENGTH_LONG).show();
//            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(MapActivity.this.INPUT_METHOD_SERVICE);
//            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//        }


        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}


