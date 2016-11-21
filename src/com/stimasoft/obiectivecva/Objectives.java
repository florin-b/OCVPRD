package com.stimasoft.obiectivecva;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.arabesque.comparators.AdressComparatorAsc;
import com.arabesque.comparators.AdressComparatorDesc;
import com.arabesque.comparators.BenefComparatorAsc;
import com.arabesque.comparators.BenefComparatorDesc;
import com.arabesque.comparators.ConstructorComparatorAsc;
import com.arabesque.comparators.ConstructorComparatorDesc;
import com.arabesque.comparators.DenumireComparatorAsc;
import com.arabesque.comparators.DenumireComparatorDesc;
import com.arabesque.comparators.JudetComparatorAsc;
import com.arabesque.comparators.JudetComparatorDesc;
import com.arabesque.comparators.PhaseExpComparatorAsc;
import com.arabesque.comparators.PhaseExpComparatorDesc;
import com.arabesque.comparators.PhaseNameComparatorAsc;
import com.arabesque.comparators.PhaseNameComparatorDesc;
import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.Utils;
import com.stimasoft.obiectivecva.adapters.ObjectiveListAdapter;
import com.stimasoft.obiectivecva.listeners.PaginatedRecyclerOnScrollListener;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.models.db_classes.ObjectiveLite;
import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.models.db_utilities.ObjectiveData;
import com.stimasoft.obiectivecva.models.db_utilities.RegionData;
import com.stimasoft.obiectivecva.models.db_utilities.UserData;
import com.stimasoft.obiectivecva.notifications.ServiceSendNotification;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.FilterUtils;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.Setup;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;
import com.stimasoft.obiectivecva.utils.StagePhaseSpinnerUtils;
import com.stimasoft.obiectivecva.utils.ui.DatePickerDialogFragment;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Objectives extends AppCompatActivity
		implements PaginatedRecyclerOnScrollListener.RecyclerEndListener, DatePickerDialogFragment.SetLimitsInterface {

	private ObjectiveListAdapter objAdapter;
	private User user;

	private HashMap<String, Pair<String, String>> defaultWhereArgs;
	private HashMap<String, String> defaultOrderArgs;
	private Pair<String, String> currentTableOrder;

	//Add by Petru  
	TextView headerName, benefName, constrName, addrName, cityName, phaseName, phaseExp;
	//End by Petru 
	
	private int mode;

	int visibleItemCount;
	boolean loading = true;
	int totalItemCount;
	int pastVisiblesItems;

	private int currentOffset;

	// stage and phase filter, Author: Alin
	private int lastSelectedStageId = -1;
	private int lastSelectedPhaseId = -1;

	private Bundle bundle;

	private boolean doubleBackToExitPressedOnce = false;
	private boolean flagLaunchedFromMap = false;
	// stage and phase filter, Author: Alin
	private boolean objectiveTypeChanged = false;

	private ArrayList<ObjectiveLite> currentObjectives;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DBG", "Calling onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_objectives);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// Add by Petru
		headerName = (TextView) findViewById(R.id.textView_objectivesHeader_name);
		benefName = (TextView) findViewById(R.id.textView_objectivesBeneficiary_name);
		constrName = (TextView) findViewById(R.id.textView_objectivesConstructor_name);
		addrName = (TextView) findViewById(R.id.textView_objectivesAddress_detail);
		cityName = (TextView) findViewById(R.id.textView_objectivesCity_name);
		phaseName = (TextView) findViewById(R.id.textView_objectivesPhase_detail);
		phaseExp = (TextView) findViewById(R.id.textView_objectivesHeader_phaseExp);
		// End by Petru

		SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
		user = sharedPrefHelper.getUserDetails();

		currentOffset = 0;
		Intent intent = getIntent();
		bundle = intent.getExtras();

		flagLaunchedFromMap = bundle.getBoolean(Constants.KEY_MAP_LAUNCH, false);

		mode = bundle.getInt(Constants.OBJECTIVES_MODE);

		switch (mode) {
		case Constants.OBJECTIVES_ONGOING:
			setupActiveView();
			break;

		case Constants.OBJECTIVES_ARCHIVE:
			setupInactiveView();
			break;

		default:
			setupActiveView();
			break;
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!flagLaunchedFromMap && mode == Constants.OBJECTIVES_ONGOING) {
			SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
			sharedPrefHelper.clearFilters();
			Log.d("DBG", "Destroyed filters");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		ObjectiveData objectiveData = new ObjectiveData(this);
		Setup setup = new Setup(this);

		currentObjectives = new ArrayList<ObjectiveLite>();

		SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
		HashMap<String, Pair<String, String>> whereArgs = new HashMap<String, Pair<String, String>>();

		final LinearLayout filters = (LinearLayout) findViewById(R.id.layout_filters);

		switch (mode) {
		case Constants.OBJECTIVES_ONGOING:
			whereArgs = sharedPrefHelper.getFilters();
			whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
			break;

		case Constants.OBJECTIVES_ARCHIVE:
			whereArgs = new HashMap<String, Pair<String, String>>(defaultWhereArgs);
			whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
			break;

		default:
			break;
		}

		setup.setupFilters(filters, mode, whereArgs);

		HashMap<String, String> order = new HashMap<String, String>();
		order.put(currentTableOrder.first, currentTableOrder.second);

		if (UserInfo.getInstance().getTipUser().equals("CV")) {

			currentObjectives = objectiveData.getListObjectives(UserInfo.getInstance().getCod(), 0, whereArgs, order);

			objAdapter = new ObjectiveListAdapter(this, currentObjectives, mode);

			RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
			objectivesList.setAdapter(objAdapter);

		} else if ((user.getUserType() == User.TYPE_DVA && flagLaunchedFromMap) || mode == Constants.OBJECTIVES_ARCHIVE) {
			UserData userData = new UserData(this);

			String userCodes = null;

			currentObjectives = objectiveData.getListObjectives(userCodes, 0, whereArgs, order);

			objAdapter = new ObjectiveListAdapter(this, currentObjectives, mode);

			RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
			objectivesList.setAdapter(objAdapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_objectives, menu);

		switch (mode) {
		case Constants.OBJECTIVES_ARCHIVE:

			MenuItem mapViewItem = menu.findItem(R.id.action_switch_map);
			mapViewItem.setVisible(false);
			this.invalidateOptionsMenu();

			break;

		case Constants.OBJECTIVES_ONGOING:
			MenuItem archiveItem = menu.add("Arhiva");
			archiveItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_archive_white_48dp));
			archiveItem.setTitle("Arhiva");
			archiveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);

			archiveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem menuItem) {
					Intent i = new Intent(getApplicationContext(), Objectives.class);
					i.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ARCHIVE);
					startActivity(i);

					return true;
				}
			});

		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will automatically
		// handle clicks on the Home/Up button, so long as you specify a parent
		// activity in AndroidManifest.xml.

		int id = item.getItemId();

		switch (id) {
		case R.id.action_switch_map:
			if (flagLaunchedFromMap) {
				finish();
			} else {
				Intent intent = new Intent(getApplicationContext(), MapActivity.class);
				intent.putExtra(Constants.KEY_FLAG, Constants.FLAG_FILTERS_CLOSED);
				intent.putExtra(Constants.KEY_LIST_LAUNCH, true);

				startActivity(intent);
			}
			return true;

		case R.id.action_filter:
			DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawerLayout.openDrawer(findViewById(R.id.navView_drawerRight));
			return true;

		case android.R.id.home:
			Log.d("DBG", "Fired back button event");
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Populates the targeted spinner with all of the regions in the database
	 *
	 * @param spinner
	 *            Spinner targeted for population
	 * @return The regions with which the spinner was populated
	 */
	private ArrayList<Region> populateRegionsSpinner(Spinner spinner) {
		RegionData regionData = new RegionData(this);
		ArrayList<Region> regions = regionData.getAllRegions();
		regions.add(0, new Region(0, "Orice regiune", "OR"));

		RegionSpinnerAdapter adapter = new RegionSpinnerAdapter(this, android.R.layout.simple_spinner_item, regions);

		spinner.setAdapter(adapter);

		return regions;
	}

	/**
	 * Sets up the UI and filters for viewing of the active objectives
	 */
	private void setupActiveView() {

		this.setTitle(getString(R.string.objective_title_active));

		final ObjectiveData objectiveData = new ObjectiveData(this);

		currentTableOrder = new Pair<String, String>(SQLiteHelper.EXPIRATION_PHASE, FilterUtils.ASCENDING);

		defaultOrderArgs = new HashMap<String, String>();
		defaultOrderArgs.put(SQLiteHelper.EXPIRATION_PHASE, FilterUtils.ASCENDING);

		final SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);

		defaultWhereArgs = sharedPrefHelper.getFilters();
		if (defaultWhereArgs == null) {
			defaultWhereArgs = new HashMap<String, Pair<String, String>>();
		}

		defaultWhereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));

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

		final RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
		objectivesList.setAdapter(objAdapter);

		final LinearLayoutManager lLManager = new LinearLayoutManager(this);
		objectivesList.setLayoutManager(lLManager);
		objectivesList.setItemAnimator(new DefaultItemAnimator());

		objectivesList.addOnScrollListener(new PaginatedRecyclerOnScrollListener(this, lLManager));

		Spinner regionSpinner = (Spinner) findViewById(R.id.spinner_filter_region);
		populateRegionsSpinner(regionSpinner);

		final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navView = (NavigationView) findViewById(R.id.navView_drawer);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		// final LinearLayout filters = (LinearLayout)
		// findViewById(R.id.layout_filters);

		ImageButton hideFilters = (ImageButton) findViewById(R.id.button_hideFilters);
		hideFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Setup setup = new Setup(Objectives.this);
				setup.hideKeyboard();

				DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				drawerLayout.closeDrawers();
			}
		});

		Button applyFilters = (Button) findViewById(R.id.button_applyFilters);
		applyFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO @FIlip cum se lanseaza filtrarea

				Setup setup = new Setup(Objectives.this);
				setup.hideKeyboard();

				FilterUtils filterUtils = new FilterUtils(view.getContext());

				HashMap<String, Pair<String, String>> whereFilters = filterUtils.generateFilterWhere();
				whereFilters.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));

				sharedPrefHelper.setFilters(whereFilters);
				defaultWhereArgs = whereFilters;

				ObjectiveData objData = new ObjectiveData(getApplicationContext());

				HashMap<String, String> orderBy = new HashMap<String, String>();
				orderBy.put(currentTableOrder.first, currentTableOrder.second);

				ArrayList<ObjectiveLite> objectives = new ArrayList<ObjectiveLite>();

				if (user.getUserType() == User.TYPE_CVA) {
					objectives = objData.getListObjectives(user.getCode(), 0, whereFilters, orderBy);
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());

					objectives = objData.getListObjectives(null, 0, whereFilters, orderBy);
				}
				objAdapter = new ObjectiveListAdapter(view.getContext(), objectives, mode);
				objectivesList.setAdapter(objAdapter);

				drawerLayout.closeDrawers();

			}
		});

		Button clearFilters = (Button) findViewById(R.id.button_clearFilters);
		clearFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Setup setup = new Setup(Objectives.this);
				setup.hideKeyboard();

				sharedPrefHelper.clearFilters();
				defaultWhereArgs = new HashMap<String, Pair<String, String>>();
				defaultWhereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
				setup.clearFilters();

				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				objAdapter = new ObjectiveListAdapter(Objectives.this, objectiveData.getListObjectives(userFilter, 0, defaultWhereArgs, defaultOrderArgs),
						mode);

				RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
				objectivesList.setAdapter(objAdapter);

				DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				drawerLayout.closeDrawers();
			}
		});

		// **** Objective Name ****
		// Modified By: Petru 
		//in modul de afisare lista a obiectivelor se doreste sa se poata ordona afisarea (crescator/descrescator) dupa toate campurile afisate,

		headerName.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();

				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));

				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}
				
			
				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// Add by Petru

					Collections.sort(currentObjectives, new DenumireComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) headerName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					//End by Petru 
					
					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new DenumireComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) headerName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

	
		benefName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();

				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));

				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}
				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// Add by Petru

					Collections.sort(currentObjectives, new BenefComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) benefName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new BenefComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) benefName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// **** Constructor ****

		constrName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// Add by Petru

					for (int i = 0; i < currentObjectives.size(); i++) {
						ObjectiveLite objectiveLite = currentObjectives.get(i);
						if (objectiveLite.getConstructorName().equals(" ")) {
							objectiveLite.setConstructorName("Regie proprie");
						}

					}

					Collections.sort(currentObjectives, new ConstructorComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) constrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					for (int i = 0; i < currentObjectives.size(); i++) {
						ObjectiveLite objectiveLite = currentObjectives.get(i);
						if (objectiveLite.getConstructorName().equals(" ")) {
							objectiveLite.setConstructorName("Regie proprie");
						}

					}

					Collections.sort(currentObjectives, new ConstructorComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) constrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;
				}

			}

		});

		// **** Address ****
		addrName.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new AdressComparatorDesc());

					objAdapter.setObjectives(currentObjectives);
					objAdapter.notifyDataSetChanged();

					((TextView) addrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new AdressComparatorAsc());

					objAdapter.setObjectives(currentObjectives);
					objAdapter.notifyDataSetChanged();

					((TextView) addrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// **** City ****
		cityName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// add

					Collections.sort(currentObjectives, new JudetComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					// end add
					((TextView) cityName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new JudetComparatorAsc());
					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);
					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) cityName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// Phase Name
		phaseName.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}
				hideAll();

		
				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// Add by Petru

					Collections.sort(currentObjectives, new PhaseNameComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) phaseName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// add by florin

					Collections.sort(currentObjectives, new PhaseNameComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);

					objectivesList.setAdapter(objAdapter);

					// objAdapter.setObjectives(currentObjectives);
					// objAdapter.notifyAll();
					// end by Florin

					((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// Phase Expiration ****

		phaseExp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.ACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new PhaseExpComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) phaseExp).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// add by florin

					Collections.sort(currentObjectives, new PhaseExpComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);

					objectivesList.setAdapter(objAdapter);

					// objAdapter.setObjectives(currentObjectives);
					// objAdapter.notifyAll();
					// end by Florin

					((TextView) phaseExp).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}
			}

		});

		// End Task by Petru

		FloatingActionButton fabAddObjective = (FloatingActionButton) findViewById(R.id.fab_addObjective);

		if (user.getUserType() == User.TYPE_DVA) {
			fabAddObjective.setVisibility(View.GONE);
		} else {
			fabAddObjective.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent i = new Intent(getApplicationContext(), AddEditObjective.class);
					i.putExtra(Constants.KEY_PURPOSE, Constants.VALUE_ADD);
					startActivityForResult(i, Constants.CODE_ADD_FROM_LIST);
				}
			});
		}

		Setup setup = new Setup(this);
		setup.setupToolbar(toolbar);
		setup.setupDrawer(drawerLayout, navView, toolbar);
		setup.setupDrawerHeader(navView, user);
		setup.setupDrawerMenu(navView, drawerLayout, R.id.drawer_menu_obiective);

		int drawerFlag = bundle.getInt(Constants.KEY_FLAG);

		switch (drawerFlag) {
		case Constants.FLAG_FILTERS_OPEN:
			drawerLayout.openDrawer(GravityCompat.END);
			break;

		default:
			break;
		}

	}

	
	//Add by Petru 
	// Am pus toate campurile null si am folosit  	((TextView) View).setCompoundDrawablesWithIntrinsicBounds
	//(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null); pentru a mi seta vizibila iconita ptr coloana selectata
	
	private void hideAll() {
		// TODO Auto-generated method stub
		headerName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		benefName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		constrName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		addrName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		cityName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		phaseName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		phaseExp.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
	}

	//End by Petru 
	
	/**
	 * Sets up the UI and filters for viewing of the archived objectives
	 */

	private void setupInactiveView() {
		Log.d("DBG", "Setting up Inactive View");
		this.setTitle(R.string.objective_title_archive);

		final ObjectiveData objectiveData = new ObjectiveData(this);
		
	

		currentTableOrder = new Pair<String, String>(SQLiteHelper.EXPIRATION_PHASE, FilterUtils.ASCENDING);

		defaultOrderArgs = new HashMap<String, String>();
		defaultOrderArgs.put(SQLiteHelper.EXPIRATION_PHASE, FilterUtils.ASCENDING);

		final SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);

		// defaultWhereArgs = sharedPrefHelper.getFilters();
		// if (defaultWhereArgs == null) {
		defaultWhereArgs = new HashMap<String, Pair<String, String>>();
		// }

		defaultWhereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));

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

		final RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
		objectivesList.setAdapter(objAdapter);
		objectivesList.setLayoutManager(new LinearLayoutManager(this));
		objectivesList.setItemAnimator(new DefaultItemAnimator());

		TextView expPhaseHeader = (TextView) findViewById(R.id.textView_objectivesHeader_phaseExp);
		// expPhaseHeader.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this,
		// R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

		Spinner regionSpinner = (Spinner) findViewById(R.id.spinner_filter_region);
		populateRegionsSpinner(regionSpinner);

		final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navView = (NavigationView) findViewById(R.id.navView_drawer);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		final LinearLayout filters = (LinearLayout) findViewById(R.id.layout_filters);

		ImageButton hideFilters = (ImageButton) findViewById(R.id.button_hideFilters);
		hideFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Setup setup = new Setup(Objectives.this);
				setup.hideKeyboard();

				DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				drawerLayout.closeDrawers();
			}
		});

		Button applyFilters = (Button) findViewById(R.id.button_applyFilters);
		applyFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO @FIlip cum se lanseaza filtrarea

				Setup setup = new Setup(Objectives.this);
				setup.hideKeyboard();

				FilterUtils filterUtils = new FilterUtils(view.getContext());

				HashMap<String, Pair<String, String>> whereFilters = filterUtils.generateFilterWhere();
				whereFilters.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				Log.d("DBG", "Clicked apply filters in InactiveView");
				defaultWhereArgs = new HashMap<String, Pair<String, String>>(whereFilters);
				// Do not commit archive filters
				// sharedPrefHelper.setFilters(whereFilters);
				// defaultWhereArgs = whereFilters;

				ObjectiveData objData = new ObjectiveData(getApplicationContext());

				HashMap<String, String> orderBy = new HashMap<String, String>();
				orderBy.put(currentTableOrder.first, currentTableOrder.second);

				ArrayList<ObjectiveLite> objectives = new ArrayList<ObjectiveLite>();

				if (user.getUserType() == User.TYPE_CVA) {
					objectives = objData.getListObjectives(user.getCode(), 0, whereFilters, orderBy);
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());

					objectives = objData.getListObjectives(userData.generateCvaCodesString(userData.getUsersForDva(user.getCode())), 0, whereFilters, orderBy);
				}

				objAdapter = new ObjectiveListAdapter(view.getContext(), objectives, mode);
				objectivesList.setAdapter(objAdapter);

				drawerLayout.closeDrawers();

			}
		});

		Button clearFilters = (Button) findViewById(R.id.button_clearFilters);
		clearFilters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Setup setup = new Setup(Objectives.this);
				setup.hideKeyboard();

				sharedPrefHelper.clearFilters();
				defaultWhereArgs = new HashMap<String, Pair<String, String>>();
				defaultWhereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				setup.clearFilters();

				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				objAdapter = new ObjectiveListAdapter(Objectives.this, objectiveData.getListObjectives(userFilter, 0, defaultWhereArgs, defaultOrderArgs),
						mode);

				RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
				objectivesList.setAdapter(objAdapter);

				DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
				drawerLayout.closeDrawers();
			}
		});
		// #returnhere

		// Add arhive Petru 

		headerName.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();

				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));

				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// Add by Petru

					Collections.sort(currentObjectives, new DenumireComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) headerName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new DenumireComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) headerName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		benefName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();

				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));

				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new BenefComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) benefName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new BenefComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) benefName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// **** Constructor ****

		constrName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					for (int i = 0; i < currentObjectives.size(); i++) {
						ObjectiveLite objectiveLite = currentObjectives.get(i);
						if (objectiveLite.getConstructorName().equals(" ")) {
							objectiveLite.setConstructorName("Regie proprie");
						}

					}

					Collections.sort(currentObjectives, new ConstructorComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) constrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					for (int i = 0; i < currentObjectives.size(); i++) {
						ObjectiveLite objectiveLite = currentObjectives.get(i);
						if (objectiveLite.getConstructorName().equals(" ")) {
							objectiveLite.setConstructorName("Regie proprie");
						}

					}

					Collections.sort(currentObjectives, new ConstructorComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) constrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;
				}

			}

		});

		// **** Address ****
		addrName.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new AdressComparatorDesc());
					
					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) addrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new AdressComparatorAsc());

					
					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) addrName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// **** City ****
		cityName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// add

					Collections.sort(currentObjectives, new JudetComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					// end add
					((TextView) cityName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new JudetComparatorAsc());
					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);
					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) cityName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// Phase Name
		phaseName.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}
				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// Add by Petru

					Collections.sort(currentObjectives, new PhaseNameComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) phaseName).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// add by florin

					Collections.sort(currentObjectives, new PhaseNameComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);

					objectivesList.setAdapter(objAdapter);

					// objAdapter.setObjectives(currentObjectives);
					// objAdapter.notifyAll();
					// end by Florin

					((TextView) view).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}

			}

		});

		// Phase Expiration ****

		phaseExp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());
				HashMap<String, Pair<String, String>> whereArgs = sharedPrefHelper.getFilters();
				whereArgs.put(SQLiteHelper.STATUS, new Pair<String, String>(FilterUtils.EQUALS, Integer.toString(Objective.INACTIVE)));
				String userFilter = "";

				if (user.getUserType() == User.TYPE_CVA) {
					userFilter = user.getCode();
				} else if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(getApplicationContext());
					userFilter = userData.generateCvaCodesString(userData.getUsersForDva(user.getCode()));
				}

				hideAll();

				if (currentTableOrder.second.equals(FilterUtils.ASCENDING)) {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.DESCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					Collections.sort(currentObjectives, new PhaseExpComparatorDesc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
					objectivesList.setAdapter(objAdapter);

					((TextView) phaseExp).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_down_white_24dp), null, null, null);

					defaultOrderArgs = order;

				} else {
					currentTableOrder = new Pair<String, String>(SQLiteHelper.NAME, FilterUtils.ASCENDING);

					HashMap<String, String> order = new HashMap<String, String>();
					order.put(currentTableOrder.first, currentTableOrder.second);

					// add by florin

					Collections.sort(currentObjectives, new PhaseExpComparatorAsc());

					objAdapter = new ObjectiveListAdapter(Objectives.this, currentObjectives, mode);

					RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);

					objectivesList.setAdapter(objAdapter);

					// objAdapter.setObjectives(currentObjectives);
					// objAdapter.notifyAll();
					// end by Florin

					((TextView) phaseExp).setCompoundDrawablesWithIntrinsicBounds(
							ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_drop_up_white_24dp), null, null, null);

					defaultOrderArgs = order;

				}
			}

		});

		// End add Arhive

		FloatingActionButton fabAddObjective = (FloatingActionButton) findViewById(R.id.fab_addObjective);
		fabAddObjective.setVisibility(View.GONE);

		Setup setup = new Setup(this);
		setup.disableDrawer(drawerLayout);
		setup.setupToolbarBack(toolbar);
		// setup.setupDrawer(drawerLayout, navView, toolbar);
		// setup.setupDrawerHeader(navView, user);
		// setup.setupDrawerMenu(navView, drawerLayout,
		// R.id.drawer_menu_obiective);

	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		if (drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout.isDrawerOpen(GravityCompat.END)) { 
			// replace this with actual function which returns if the	 drawer is open
			drawerLayout.closeDrawers(); // replace this with actual function
											// which closes drawer
		} else if (user.getUserType() == User.TYPE_CVA && mode == Constants.OBJECTIVES_ONGOING) {

			if (doubleBackToExitPressedOnce) {

				super.onBackPressed();
				SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
				sharedPrefHelper.logOut();

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(Constants.OBJECTIVE_EXPIRES_NOTIFICATION);

				boolean notifsDisabled = disableNotifications();

				Toast.makeText(this, getString(R.string.toast_loggedOut_notifs), Toast.LENGTH_SHORT).show();

				launchLiteSFA();

				return;
			}

			this.doubleBackToExitPressedOnce = true;
			Toast.makeText(this, getString(R.string.toast_warn_logOut), Toast.LENGTH_SHORT).show();

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;
				}
			}, 2000);
		} else {
			super.onBackPressed();
		}
	}

	private void launchLiteSFA() {
		if (Utils.isPackageInstalled("my.logon.screen", getApplicationContext())) {
			Intent nextScreen = getPackageManager().getLaunchIntentForPackage("my.logon.screen");
			nextScreen.putExtra("UserInfo", Utils.serializeUserInfo());
			startActivity(nextScreen);
			System.exit(0);
		}
	}

	/**
	 * Called when the user has scrolled to the list's bottom and retrieves more
	 * objectives
	 *
	 * @param page
	 *            The page offset for the objective fetch
	 */
	@Override
	public void loadMoreObjectives(int page) {
		ObjectiveData objectiveData = new ObjectiveData(this);

		objAdapter.addObjectives(objectiveData.getListObjectives(user.getCode(), page, defaultWhereArgs, defaultOrderArgs));
	}

	@Override
	public void onDateSelected(Calendar limit, int modifierPurpose, int modifierType, int changeTarget, int limitTarget) {
		// Change the date in the targeted date field
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);
		TextView dateToChange = (TextView) findViewById(changeTarget);
		dateToChange.setText(sdf.format(limit.getTime()));
	}

	private class RegionSpinnerAdapter extends ArrayAdapter<Region> {

		public RegionSpinnerAdapter(Context context, int resource, List<Region> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
			}

			TextView nameText = (TextView) convertView.findViewById(R.id.spinner_text);
			nameText.setText(getItem(pos).getName());

			return convertView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getContext(), R.layout.spinner_dropdown_item, null);
			}
			TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
			label.setText(getItem(position).getName());

			return label;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Hide the leftover keyboard regardless
		Setup setup = new Setup(this);
		setup.hideKeyboard();

		// Check which request we're responding to
		if (requestCode == Constants.CODE_ADD_FROM_LIST || requestCode == Constants.CODE_EDIT_FROM_LIST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				ObjectiveData objectiveData = new ObjectiveData(getApplicationContext());

				HashMap<String, String> curentOrderArgs = new HashMap<String, String>();
				curentOrderArgs.put(currentTableOrder.first, currentTableOrder.second);

				objAdapter = new ObjectiveListAdapter(this, objectiveData.getListObjectives(user.getCode(), 0, defaultWhereArgs, curentOrderArgs), mode);

				RecyclerView objectivesList = (RecyclerView) findViewById(R.id.listView_consObjectives);
				objectivesList.setAdapter(objAdapter);
			}
		}
	}

	private boolean disableNotifications() {

		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		Intent serviceIntent = new Intent(this, ServiceSendNotification.class);

		PendingIntent registeredPendingIntent = PendingIntent.getService(this, Constants.ALARM_REQUEST_CODE, serviceIntent, PendingIntent.FLAG_NO_CREATE);

		if (registeredPendingIntent != null) {
			alarmManager.cancel(registeredPendingIntent);
			registeredPendingIntent.cancel();

			return true;
		}

		return false;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

}
// https://www.javacodegeeks.com/2013/03/difference-between-comparator-and-comparable-in-java.html