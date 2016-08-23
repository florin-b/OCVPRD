package com.stimasoft.obiectivecva.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arabesque.obiectivecva.UserInfo;
import com.arabesque.obiectivecva.beans.BeanDateTabele;
import com.arabesque.obiectivecva.enums.EnumFiliale;
import com.arabesque.obiectivecva.enums.EnumJudete;
import com.arabesque.obiectivecva.enums.EnumStadiuObiectiv;
import com.arabesque.obiectivecva.listeners.AsyncTaskListener;
import com.arabesque.obiectivecva.listeners.AsyncTaskWSCall;
import com.arabesque.obiectivecva.listeners.OperatiiAgentListener;
import com.arabesque.obiectivecva.model.Agent;
import com.arabesque.obiectivecva.model.OperatiiAgent;
import com.arabesque.obiectivecva.model.OperatiiTabele;
import com.stimasoft.obiectivecva.Nomenclatures;
import com.stimasoft.obiectivecva.Objectives;
import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.adapters.AutoCompleteBenefAdapter;
import com.stimasoft.obiectivecva.adapters.AutoCompleteNameAdapter;
import com.stimasoft.obiectivecva.listeners.EditDateClickListener;
import com.stimasoft.obiectivecva.models.db_classes.Beneficiary;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.models.db_utilities.BeneficiaryData;
import com.stimasoft.obiectivecva.models.db_utilities.ObjectiveData;
import com.stimasoft.obiectivecva.models.db_utilities.UserData;
import com.stimasoft.obiectivecva.utils.ui.RoundedDrawable;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Utility class used to setup ui elements
 */
public class Setup implements OperatiiAgentListener, AsyncTaskListener {

	private Context activityContext;
	private static AppCompatActivity activity;

	private Spinner spinnerFiliale;
	private Spinner spinnerConsilieri;
	private Spinner spinnerStatusObiect;

	private OperatiiAgent opAgenti;

	private static final String POPULATE_TABLES = "getListObiectiveCVA";
	private String codFiliala;

	public Setup() {

	}

	public Setup(Context context) {
		this();
		activityContext = context;
		activity = (AppCompatActivity) context;

		opAgenti = OperatiiAgent.getInstance();
		opAgenti.setOperatiiAgentListener(this);

	}

	/**
	 * Enables the toolbar button used to launch the drawer
	 *
	 * @param toolbar
	 *            The provided toolbar
	 * @return Reference to the modified toolbar
	 */
	public Toolbar setupToolbar(Toolbar toolbar) {
		activity.setSupportActionBar(toolbar);

		activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
		activity.getSupportActionBar().setHomeButtonEnabled(true);

		return toolbar;
	}

	/**
	 * Enables back navigation on the given toolbar
	 *
	 * @param toolbar
	 *            The provided toolbar
	 * @return Reference to the modified toolbar
	 */
	public Toolbar setupToolbarBack(Toolbar toolbar) {
		activity.setSupportActionBar(toolbar);

		activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		return toolbar;
	}

	/**
	 * Sets up the size and behaviour of the left hand side drawer
	 *
	 * @param drawerLayout
	 *            The layout containing both drawers
	 * @param drawer
	 *            The left handside drawer
	 * @param toolbar
	 *            The toolbar
	 */
	public void setupDrawer(final DrawerLayout drawerLayout, NavigationView drawer, Toolbar toolbar) {

		// Lock right drawer in closed position
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

		// Specify that the left drawer will open when the toolbar home button
		// is pressed
		final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_closed) {

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				// Enable right drawer touch controls after it was open
				if (drawerView == activity.findViewById(R.id.navView_drawerRight)) {
					drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
				}

				activity.invalidateOptionsMenu();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				// Disable right drawer touch controls after it was closed
				drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

				hideKeyboard();
				super.onDrawerClosed(drawerView);
				activity.invalidateOptionsMenu();
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);

		drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				drawerToggle.syncState();
			}
		});

		// Make the round profile picture
		Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_contact_picture);
		RoundedDrawable roundAvatar = new RoundedDrawable(bm);
		ImageView avatar = (ImageView) activity.findViewById(R.id.imageView_avatar);
		avatar.setImageDrawable(roundAvatar);

		// Resize the navigation drawer
		DisplayMetrics displaymetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		float density = activity.getResources().getDisplayMetrics().density;
		int width = displaymetrics.widthPixels;

		int widthDp = (int) (width / density);

		NavigationView drawerRight = (NavigationView) activity.findViewById(R.id.navView_drawerRight);

		if (widthDp - 56 > 320) {
			// drawer.setLayoutParams(new
			// DrawerLayout.LayoutParams((int)(280*density),
			// ViewGroup.LayoutParams.MATCH_PARENT));
			android.support.v4.widget.DrawerLayout.LayoutParams drawerParams = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();

			drawerParams.width = (int) (320 * density);
			drawer.setLayoutParams(drawerParams);

		} else {
			android.support.v4.widget.DrawerLayout.LayoutParams drawerParams = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
			drawerParams.width = (int) ((widthDp - 56) * density);
			drawer.setLayoutParams(drawerParams);

		}

	}

	/**
	 * Setup the drawer navigation menu. Specify which menu item will be
	 * disabled and what actions are attached to the enabled menu items.
	 *
	 * @param navViewLeft
	 *            The drawer containing the menu
	 * @param drawerLayout
	 *            The layout that contains the drawer
	 * @param disabledButton
	 *            The id of the menu item that will be disabled
	 */
	public void setupDrawerMenu(NavigationView navViewLeft, final DrawerLayout drawerLayout, int disabledButton) {

		navViewLeft.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			// This method will trigger on item Click of navigation menu
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				Intent i;
				// Checking if the item is in checked state or not, if not make
				// it in checked state
				if (menuItem.isChecked())
					menuItem.setChecked(false);
				else
					menuItem.setChecked(true);

				// Closing drawer on item click
				drawerLayout.closeDrawers();

				// Check to see which item was being clicked and perform
				// appropriate action
				switch (menuItem.getItemId()) {
				case R.id.drawer_menu_nomenclatoare:
					i = new Intent(activityContext, Nomenclatures.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);
					return true;

				case R.id.drawer_menu_obiective:
					i = new Intent(activityContext, Objectives.class);
					i.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ONGOING);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(i);
					return true;

				// case R.id.drawer_menu_rapoarte:
				// Toast.makeText(activityContext, "Rapoarte Selected",
				// Toast.LENGTH_SHORT).show();
				// i = new Intent(activityContext, Reports.class);
				// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// activity.startActivity(i);
				// return true;

				default:
					Toast.makeText(activityContext, "You are somewhere the drawer can't take you", Toast.LENGTH_SHORT).show();
					return true;
				}
			}
		});

		Menu drawerMenu = navViewLeft.getMenu();

		MenuItem item = drawerMenu.findItem(disabledButton);
		if (item != null)
			item.setEnabled(false);
	}

	/**
	 * Change drawer header text based on the user that has logged in
	 *
	 * @param navView
	 *            The targeted drawer
	 * @param user
	 *            The details of the user with which the header will be
	 *            populated
	 */
	public void setupDrawerHeader(NavigationView navView, User user) {
		TextView textViewUsername = (TextView) navView.findViewById(R.id.drawer_username);
		TextView textViewUsertype = (TextView) navView.findViewById(R.id.drawer_userType);

		textViewUsername.setText(user.getName() + " " + user.getSurName());

		switch (user.getUserType()) {
		case 0:
			textViewUsertype.setText("Director retail");
			break;

		case 1:
			textViewUsertype.setText("Director vanzari");
			break;

		case 2:
			textViewUsertype.setText("Consilier vanzari");
			break;

		default:
			textViewUsertype.setText("User invalid");
			break;
		}
	}

	/**
	 * Lock the drawers in closed position
	 *
	 * @param drawerLayout
	 *            the targeted drawer layout
	 */
	public void disableDrawer(DrawerLayout drawerLayout) {
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	}

	/**
	 * Defines the behavior of the filter UI elements and populates them with
	 * previous filter data if there is any.
	 *
	 * @param layout
	 *            Parent layout of the filter ui elements
	 * @param mode
	 *            Active or inactive autocompletes
	 */
	public void setupFilters(LinearLayout layout, final int mode, HashMap<String, Pair<String, String>> localFilters) {

		// Set date TextView Listener;
		TextView dateAddedStart = (TextView) layout.findViewById(R.id.filter_dateAdded_start);
		TextView dateAddedEnd = (TextView) layout.findViewById(R.id.filter_dateAdded_end);
		TextView authDateStart = (TextView) layout.findViewById(R.id.filter_authDate_start);
		TextView authDateEnd = (TextView) layout.findViewById(R.id.filter_authDate_end);
		TextView authExpDateStart = (TextView) layout.findViewById(R.id.filter_authExpDate_start);
		TextView authExpDateEnd = (TextView) layout.findViewById(R.id.filter_authExpDate_end);

		spinnerFiliale = (Spinner) layout.findViewById(R.id.spinner_objective_filiala);
		populateSpinnerFiliale();

		spinnerConsilieri = (Spinner) layout.findViewById(R.id.spinner_objective_consilier);
		setSpinnerConsilieriListener();

		if (UserInfo.getInstance().getTipUser().equals("CV")) {
			LinearLayout layoutFilterFiliala = (LinearLayout) layout.findViewById(R.id.layout_filter_filiala);
			LinearLayout layoutFilterConsilier = (LinearLayout) layout.findViewById(R.id.layout_filter_consilier);
			layoutFilterFiliala.setVisibility(View.GONE);
			layoutFilterConsilier.setVisibility(View.GONE);

		}

		spinnerStatusObiect = (Spinner) layout.findViewById(R.id.spinner_filter_status);
		populateSpinnerStatus();

		dateAddedStart.setHint("Alegeti o data");
		dateAddedEnd.setHint("Alegeti o data");
		authDateStart.setHint("Alegeti o data");
		authDateEnd.setHint("Alegeti o data");
		authExpDateStart.setHint("Alegeti o data");
		authExpDateEnd.setHint("Alegeti o data");

		dateAddedStart.setOnClickListener(new EditDateClickListener(activityContext, -1, -1, null, Calendar.getInstance(), R.id.filter_dateAdded_start, -1));

		dateAddedEnd.setOnClickListener(new EditDateClickListener(activityContext, -1, -1, null, Calendar.getInstance(), R.id.filter_dateAdded_end, -1));

		authDateStart.setOnClickListener(new EditDateClickListener(activityContext, -1, -1, null, Calendar.getInstance(), R.id.filter_authDate_start, -1));

		authDateEnd.setOnClickListener(new EditDateClickListener(activityContext, -1, -1, null, Calendar.getInstance(), R.id.filter_authDate_end, -1));

		authExpDateStart
				.setOnClickListener(new EditDateClickListener(activityContext, -1, -1, null, Calendar.getInstance(), R.id.filter_authExpDate_start, -1));

		authExpDateEnd.setOnClickListener(new EditDateClickListener(activityContext, -1, -1, null, Calendar.getInstance(), R.id.filter_authExpDate_end, -1));

		RadioGroup beneficiaryRadios = (RadioGroup) layout.findViewById(R.id.radioGroup_filter_beneficiaryType);
		int selectedRadioID = beneficiaryRadios.getCheckedRadioButtonId();

		final LinearLayout juridicDetails = (LinearLayout) layout.findViewById(R.id.layout_juridicaDetails);

		final AutoCompleteTextView acTextViewBenef = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_beneficiary);

		switch (selectedRadioID) {
		case R.id.radio_filter_fizic:
			juridicDetails.setVisibility(View.GONE);
			break;

		case R.id.radio_filter_juridic:
			juridicDetails.setVisibility(View.VISIBLE);
			acTextViewBenef.setNextFocusDownId(R.id.editText_filter_cui);
			acTextViewBenef.setNextFocusRightId(R.id.editText_filter_cui);
			break;

		default:
			break;
		}

		beneficiaryRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio_filter_fizic:
					juridicDetails.setVisibility(View.GONE);
					break;

				case R.id.radio_filter_juridic:
					juridicDetails.setVisibility(View.VISIBLE);
					break;

				default:
					break;
				}
			}
		});

		SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(activityContext);
		final User user = sharedPrefHelper.getUserDetails();

		switch (user.getUserType()) {
		case User.TYPE_DVA:
			LinearLayout regionLayout = (LinearLayout) activity.findViewById(R.id.layout_filter_region);
			regionLayout.setVisibility(View.VISIBLE);
			populateSpinnerRegions();

			LinearLayout cvaLayout = (LinearLayout) activity.findViewById(R.id.layout_filter_cvaCode);
			cvaLayout.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}

		final AutoCompleteTextView actProjectName = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_name);
		actProjectName.setThreshold(3);

		ArrayAdapter<String> nameAutoCompleteAdapter = new AutoCompleteNameAdapter(activity, android.R.layout.simple_dropdown_item_1line,
				new ArrayList<String>());

		actProjectName.setAdapter(nameAutoCompleteAdapter);

		actProjectName.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				ObjectiveData objectiveData = new ObjectiveData(activity);
				ArrayList<String> suggestions = new ArrayList<String>();

				if (actProjectName.isPerformingCompletion()) {
					// An item has been selected from the list.
					return;
				}

				suggestions.clear();

				if (user.getUserType() == User.TYPE_DVA) {
					UserData userData = new UserData(activity);
					suggestions = objectiveData.getObjectiveNames(userData.generateCvaCodesString(userData.getUsersForDva(user.getCode())), editable.toString(),
							mode);
				} else {
					suggestions = objectiveData.getObjectiveNames(user.getCode(), editable.toString(), mode);
				}

				AutoCompleteNameAdapter adapter = new AutoCompleteNameAdapter(activity, android.R.layout.simple_dropdown_item_1line, suggestions);

				actProjectName.setAdapter(adapter);

				adapter.notifyDataSetChanged();
			}
		});

		actProjectName.setOnItemClickListener((new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				actProjectName.setText((String) parent.getItemAtPosition(position));
			}
		}));

		acTextViewBenef.setThreshold(3);

		ArrayAdapter<Pair<Integer, String>> autocompleteAdapter;

		autocompleteAdapter = new AutoCompleteBenefAdapter(activity, android.R.layout.simple_dropdown_item_1line, new ArrayList<Pair<Integer, String>>());

		acTextViewBenef.setAdapter(autocompleteAdapter);

		acTextViewBenef.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				BeneficiaryData benefData = new BeneficiaryData(activity);
				ArrayList<Pair<Integer, String>> suggestions = new ArrayList<Pair<Integer, String>>();

				RadioGroup beneficiaryRadios = (RadioGroup) activity.findViewById(R.id.radioGroup_filter_beneficiaryType);
				int selectedRadioID = beneficiaryRadios.getCheckedRadioButtonId();
				int benefType;

				switch (selectedRadioID) {
				case R.id.radio_filter_fizic:
					benefType = Beneficiary.TYPE_INDIVIDUAL;
					break;

				case R.id.radio_filter_juridic:
					benefType = Beneficiary.TYPE_LEGAL;
					break;

				default:
					benefType = -1;
					break;
				}

				if (acTextViewBenef.isPerformingCompletion()) {
					// An item has been selected from the list.
					return;
				}

				suggestions.clear();

				suggestions = benefData.getBeneficiaryAC(s.toString(), benefType);

				AutoCompleteBenefAdapter adapter = new AutoCompleteBenefAdapter(activity, android.R.layout.simple_dropdown_item_1line, suggestions);

				acTextViewBenef.setAdapter(adapter);

				adapter.notifyDataSetChanged();
			}
		});

		acTextViewBenef.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("DBG", "Am dat click pe ceva din lista");

				BeneficiaryData beneficiaryData = new BeneficiaryData(activity);
				Pair<Integer, String> selection = (Pair<Integer, String>) parent.getItemAtPosition(position);

				Beneficiary beneficiary = beneficiaryData.getBeneficiaryById(selection.first);

				acTextViewBenef.setText(beneficiary.getName());
				if (beneficiary.getType() == 1) {
					EditText cuiText = (EditText) activity.findViewById(R.id.editText_filter_cui);
					EditText nrRcText = (EditText) activity.findViewById(R.id.editText_filter_nrRc);

					RadioButton legalPerson = (RadioButton) activity.findViewById(R.id.radio_filter_juridic);
					legalPerson.setChecked(true);

					cuiText.setText(beneficiary.getCui());
					nrRcText.setText(beneficiary.getNrRc());

				} else {
					RadioButton individualPerson = (RadioButton) activity.findViewById(R.id.radio_filter_fizic);
					individualPerson.setChecked(true);
				}

			}
		});

		// Populate the filters

		HashMap<String, Pair<String, String>> filters;
		switch (mode) {
		case Constants.OBJECTIVES_ONGOING:
			filters = sharedPrefHelper.getFilters();
			break;

		case Constants.OBJECTIVES_ARCHIVE:
			filters = localFilters;
			break;

		default:
			filters = new HashMap<String, Pair<String, String>>();
			break;
		}

		if (filters != null) {

			for (Map.Entry<String, Pair<String, String>> entry : filters.entrySet()) {
				String type;

				String s = entry.getKey();
				if (s.equals(SQLiteHelper.NAME)) {
					String regExName = "'%(.*)%'";
					Pattern patternName = Pattern.compile(regExName);
					Matcher matchName = patternName.matcher(entry.getValue().second);
					matchName.reset();

					if (matchName.find()) {
						actProjectName.setText(matchName.group(1));
					}

				} else if (s.equals(SQLiteHelper.CREATION_DATE)) {
					type = entry.getValue().first;

					if (type.equals(FilterUtils.GEATER_OR_EQUAL)) {
						dateAddedStart
								.setText(changeDateFormat(entry.getValue().second.replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

					} else if (type.equals(FilterUtils.LESS_OR_EQUAL)) {
						dateAddedEnd.setText(changeDateFormat(entry.getValue().second.replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

					} else if (type.equals(FilterUtils.BETWEEN)) {
						String[] splitArray = null;
						try {
							splitArray = entry.getValue().second.split(" AND ");
						} catch (Exception ex) {
							//
						}

						if (splitArray[1] != null) {
							dateAddedStart.setText(changeDateFormat(splitArray[0].replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

							dateAddedEnd.setText(changeDateFormat(splitArray[1].replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));
						}

					}

				} else if (s.equals(SQLiteHelper.BENEFICIARY_NAME)) {
					String regExBene = "'%(.*)%'";
					Pattern patternBene = Pattern.compile(regExBene);
					Matcher matchBene = patternBene.matcher(entry.getValue().second);
					matchBene.reset();

					if (matchBene.find()) {
						acTextViewBenef.setText(matchBene.group(1));
					}

				} else if (s.equals(SQLiteHelper.BENEFICIARY_TYPE)) {
					int benefType = Integer.parseInt(entry.getValue().second);
					if (benefType == Beneficiary.TYPE_LEGAL) {
						RadioButton legalRadio = (RadioButton) activity.findViewById(R.id.radio_filter_juridic);
						legalRadio.setChecked(true);
					} else {
						RadioButton individualRadio = (RadioButton) activity.findViewById(R.id.radio_filter_fizic);
						individualRadio.setChecked(true);
					}

				} else if (s.equals(SQLiteHelper.CUI)) {
					EditText cuiText = (EditText) activity.findViewById(R.id.editText_filter_cui);
					String regExCui = "'%(.*)%'";
					Pattern patternCui = Pattern.compile(regExCui);
					Matcher matchCui = patternCui.matcher(entry.getValue().second);
					matchCui.reset();

					if (matchCui.find()) {
						cuiText.setText(matchCui.group(1));
					}

				} else if (s.equals(SQLiteHelper.NR_RC)) {
					EditText nrRcText = (EditText) activity.findViewById(R.id.editText_filter_nrRc);
					String regExNrRc = "'%(.*)%'";
					Pattern patternNrRc = Pattern.compile(regExNrRc);
					Matcher matchNrRc = patternNrRc.matcher(entry.getValue().second);
					matchNrRc.reset();

					if (matchNrRc.find()) {
						nrRcText.setText(matchNrRc.group(1));
					}

				} else if (s.equals(SQLiteHelper.AUTHORIZATION_START)) {
					type = entry.getValue().first;

					if (type.equals(FilterUtils.GEATER_OR_EQUAL)) {
						authDateStart.setText(changeDateFormat(entry.getValue().second.replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

					} else if (type.equals(FilterUtils.LESS_OR_EQUAL)) {
						authDateEnd.setText(changeDateFormat(entry.getValue().second.replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

					} else if (type.equals(FilterUtils.BETWEEN)) {
						String[] splitArray = null;
						try {
							splitArray = entry.getValue().second.split(" AND ");
						} catch (Exception ex) {
							//
						}

						if (splitArray[1] != null) {
							authDateStart.setText(changeDateFormat(splitArray[0].replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

							authDateEnd.setText(changeDateFormat(splitArray[1].replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));
						}

					}

				} else if (s.equals(SQLiteHelper.AUTHORIZATION_END)) {
					type = entry.getValue().first;
					if (type.equals(FilterUtils.GEATER_OR_EQUAL)) {
						authExpDateStart
								.setText(changeDateFormat(entry.getValue().second.replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

					} else if (type.equals(FilterUtils.LESS_OR_EQUAL)) {
						authExpDateEnd
								.setText(changeDateFormat(entry.getValue().second.replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

					} else if (type.equals(FilterUtils.BETWEEN)) {
						String[] splitArray = null;
						try {
							splitArray = entry.getValue().second.split(" AND ");
						} catch (Exception ex) {
							//
						}

						if (splitArray[1] != null) {
							authExpDateStart.setText(changeDateFormat(splitArray[0].replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));

							authExpDateEnd.setText(changeDateFormat(splitArray[1].replace("'", ""), Constants.DB_DATE_FORMAT, Constants.USER_DATE_FORMAT));
						}

					}

				} else if (s.equals(SQLiteHelper.ESTIMATION_VALUE)) {
					type = entry.getValue().first;
					if (type.equals(FilterUtils.GEATER_OR_EQUAL) || type.equals(FilterUtils.LESS_OR_EQUAL)) {

						EditText estValueStart = (EditText) activity.findViewById(R.id.filter_estValue_start);
						estValueStart.setText(entry.getValue().second.replace("'", ""));
					} else {

						EditText estValueStart = (EditText) activity.findViewById(R.id.filter_estValue_start);
						EditText estValueEnd = (EditText) activity.findViewById(R.id.filter_estValue_end);

						String[] splitArray = null;
						try {
							splitArray = entry.getValue().second.split(" AND ");
						} catch (Exception ex) {
							//
						}

						if (splitArray[1] != null) {
							estValueStart.setText(splitArray[0].replace("'", ""));
							estValueEnd.setText(splitArray[1].replace("'", ""));
						} else {
							estValueEnd.setText(entry.getValue().second);
						}
						// estValueEnd.setText(entry.getValue().second);
					}

				} else if (s.equals(SQLiteHelper.ZIP)) {
					EditText zipText = (EditText) activity.findViewById(R.id.editText_filter_postalCode);
					zipText.setText(entry.getValue().second);

				} else if (s.equals(SQLiteHelper.CVA_CODE)) {
					EditText cvaCodesText = (EditText) activity.findViewById(R.id.editText_filter_cvaCode);
					cvaCodesText.setText(entry.getValue().second);

				}
			}
		}

		setupClearButtons();

	}

	private void populateSpinnerRegions() {

		List<String> listRegions = EnumJudete.getRegionNames();
		listRegions.add(0, "Selectati un judet");
		String[] arrayJud = listRegions.toArray(new String[listRegions.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_spinner_dropdown_item, arrayJud);

		Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_filter_region);

		regionSpinner.setAdapter(dataAdapter);

	}

	private void populateSpinnerFiliale() {
		List<String> listFiliale = EnumFiliale.getFiliale();
		String[] arrayFiliale = listFiliale.toArray(new String[listFiliale.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_spinner_dropdown_item, arrayFiliale);
		spinnerFiliale.setAdapter(dataAdapter);

		setSpinnerFilialeListener(spinnerFiliale);
	}

	private void setSpinnerFilialeListener(Spinner spinnerFiliale) {
		spinnerFiliale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position > 0) {
					codFiliala = EnumFiliale.getCodFiliala(parent.getAdapter().getItem(position).toString());

					populateLocalTables(codFiliala);
					
					

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void populateLocalTables(String codFiliala) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("tipUser", UserInfo.getInstance().getTipUser());
		params.put("codUser", UserInfo.getInstance().getCod());
		params.put("filiala", codFiliala);

		AsyncTaskListener contextListener = (AsyncTaskListener) Setup.this;

		AsyncTaskWSCall call = new AsyncTaskWSCall(activityContext, contextListener, POPULATE_TABLES, params);

		call.getCallResultsFromFragment();
	}

	private void populateListConsilieri(String codFiliala) {
		opAgenti.getListaAgenti(codFiliala, "11", activityContext, true);

	}

	private void populateSpinnerConsilieri(List<Agent> listAgenti) {
		spinnerConsilieri.setVisibility(View.VISIBLE);
		spinnerConsilieri.setAdapter(new ArrayAdapter<Agent>(activityContext, android.R.layout.simple_list_item_1, listAgenti));
	}

	private void setSpinnerConsilieriListener() {
		spinnerConsilieri.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String selectedAgent = ((Agent) spinnerConsilieri.getSelectedItem()).getCod();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	private void populateSpinnerStatus() {
		List<String> listStatus = EnumStadiuObiectiv.getStatusNames();

		String[] arrayStatus = listStatus.toArray(new String[listStatus.size()]);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activityContext, android.R.layout.simple_spinner_dropdown_item, arrayStatus);
		spinnerStatusObiect.setAdapter(dataAdapter);
	}

	/**
	 * Convenience method for asigning functionality to the clear buttons
	 */
	public void setupClearButtons() {
		// Setup the clear buttons

		ImageButton clearFiliala = (ImageButton) activity.findViewById(R.id.button_clearFiliala);
		ImageButton clearConsilier = (ImageButton) activity.findViewById(R.id.button_clearConsilier);
		ImageButton clearStatus = (ImageButton) activity.findViewById(R.id.button_clearStatus);

		ImageButton clearName = (ImageButton) activity.findViewById(R.id.button_clearName);
		ImageButton clearCity = (ImageButton) activity.findViewById(R.id.button_clearCity);
		ImageButton clearAddDates = (ImageButton) activity.findViewById(R.id.button_clearAddDates);
		ImageButton clearRegions = (ImageButton) activity.findViewById(R.id.button_clearRegions);
		ImageButton clearBeneficiary = (ImageButton) activity.findViewById(R.id.button_clearBeneficiary);
		ImageButton clearBenefType = (ImageButton) activity.findViewById(R.id.button_clearBeneficiaryType);
		ImageButton clearCui = (ImageButton) activity.findViewById(R.id.button_clearCui);
		ImageButton clearNrRc = (ImageButton) activity.findViewById(R.id.button_clearNrRc);
		ImageButton clearAuthDates = (ImageButton) activity.findViewById(R.id.button_clearAuthDates);
		ImageButton clearAuthExps = (ImageButton) activity.findViewById(R.id.button_clearAuthExp);
		ImageButton clearEstValues = (ImageButton) activity.findViewById(R.id.button_clearEstValues);
		ImageButton clearZip = (ImageButton) activity.findViewById(R.id.button_clearZip);
		ImageButton clearCvaCodes = (ImageButton) activity.findViewById(R.id.button_clearCvaCodes);

		clearFiliala.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				spinnerFiliale.setSelection(0);
				spinnerConsilieri.setSelection(0);
			}
		});

		clearConsilier.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				spinnerConsilieri.setSelection(0);
			}
		});

		clearStatus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				spinnerStatusObiect.setSelection(0);
			}
		});

		clearName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AutoCompleteTextView acTextViewName = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_name);
				acTextViewName.setText("");
			}
		});

		clearCity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText textCity = (EditText) activity.findViewById(R.id.editText_filter_city);
				textCity.setText("");

			}
		});

		clearAddDates.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TextView addDateStart = (TextView) activity.findViewById(R.id.filter_dateAdded_start);
				TextView addDateEnd = (TextView) activity.findViewById(R.id.filter_dateAdded_end);

				addDateStart.setText("");
				addDateEnd.setText("");
			}
		});

		clearRegions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_filter_region);
				regionSpinner.setSelection(0);
			}
		});

		clearBeneficiary.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AutoCompleteTextView beneficiaryName = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_beneficiary);
				beneficiaryName.setText("");
			}
		});

		clearBenefType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RadioGroup benefTypeRadio = (RadioGroup) activity.findViewById(R.id.radioGroup_filter_beneficiaryType);
				benefTypeRadio.clearCheck();

				LinearLayout legalPersonDetails = (LinearLayout) activity.findViewById(R.id.layout_juridicaDetails);
				legalPersonDetails.setVisibility(View.GONE);
			}
		});

		clearCui.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText cuiText = (EditText) activity.findViewById(R.id.editText_filter_cui);
				cuiText.setText("");
			}
		});

		clearNrRc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText nrRcText = (EditText) activity.findViewById(R.id.editText_filter_nrRc);
				nrRcText.setText("");
			}
		});

		clearAuthDates.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TextView authDateStart = (TextView) activity.findViewById(R.id.filter_authDate_start);
				TextView authDateEnd = (TextView) activity.findViewById(R.id.filter_authDate_end);

				authDateStart.setText("");
				authDateEnd.setText("");
			}
		});

		clearAuthExps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TextView authExpStart = (TextView) activity.findViewById(R.id.filter_authExpDate_start);
				TextView authExpEnd = (TextView) activity.findViewById(R.id.filter_authExpDate_end);

				authExpStart.setText("");
				authExpEnd.setText("");
			}
		});

		clearEstValues.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText estValueStart = (EditText) activity.findViewById(R.id.filter_estValue_start);
				EditText estValueEnd = (EditText) activity.findViewById(R.id.filter_estValue_end);

				estValueStart.setText("");
				estValueEnd.setText("");
			}
		});

		clearZip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText zipText = (EditText) activity.findViewById(R.id.editText_filter_postalCode);
				zipText.setText("");
			}
		});

		clearCvaCodes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EditText cvaCodes = (EditText) activity.findViewById(R.id.editText_filter_cvaCode);
				cvaCodes.setText("");
			}
		});
	}

	/**
	 * Clears the filter UI elements of all data and returns them to their
	 * initial state
	 */
	public void clearFilters() {
		AutoCompleteTextView projectNameText = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_name);
		TextView addDateStartText = (TextView) activity.findViewById(R.id.filter_dateAdded_start);
		TextView addDateEndText = (TextView) activity.findViewById(R.id.filter_dateAdded_end);
		Spinner regionSpinner = (Spinner) activity.findViewById(R.id.spinner_filter_region);
		AutoCompleteTextView beneficiaryName = (AutoCompleteTextView) activity.findViewById(R.id.autoComplete_filter_beneficiary);
		RadioGroup beneficiaryTypeGroup = (RadioGroup) activity.findViewById(R.id.radioGroup_filter_beneficiaryType);
		EditText cuiText = (EditText) activity.findViewById(R.id.editText_filter_cui);
		EditText nrRcText = (EditText) activity.findViewById(R.id.editText_filter_nrRc);
		TextView authBeginStart = (TextView) activity.findViewById(R.id.filter_authDate_start);
		TextView authBeginEnd = (TextView) activity.findViewById(R.id.filter_authDate_end);
		TextView authStopStart = (TextView) activity.findViewById(R.id.filter_authExpDate_start);
		TextView authStopEnd = (TextView) activity.findViewById(R.id.filter_authExpDate_end);
		EditText estValueStart = (EditText) activity.findViewById(R.id.filter_estValue_start);
		EditText estValueEnd = (EditText) activity.findViewById(R.id.filter_estValue_end);
		EditText zipCodeText = (EditText) activity.findViewById(R.id.editText_filter_postalCode);
		EditText cvaCodesText = (EditText) activity.findViewById(R.id.editText_filter_cvaCode);

		EditText textCity = (EditText) activity.findViewById(R.id.editText_filter_city);

		spinnerFiliale = (Spinner) activity.findViewById(R.id.spinner_objective_filiala);
		spinnerConsilieri = (Spinner) activity.findViewById(R.id.spinner_objective_consilier);
		spinnerStatusObiect = (Spinner) activity.findViewById(R.id.spinner_filter_status);

		spinnerFiliale.setSelection(0);
		spinnerConsilieri.setSelection(0);
		spinnerStatusObiect.setSelection(0);

		projectNameText.setText("");
		addDateStartText.setText("");
		addDateEndText.setText("");
		regionSpinner.setSelection(0);
		beneficiaryName.setText("");
		beneficiaryTypeGroup.clearCheck();
		cuiText.setText("");
		nrRcText.setText("");
		authBeginStart.setText("");
		authBeginEnd.setText("");
		authStopStart.setText("");
		authStopEnd.setText("");
		estValueStart.setText("");
		estValueEnd.setText("");
		zipCodeText.setText("");
		cvaCodesText.setText("");
		textCity.setText("");
	}

	/**
	 * Forcefully hides the keyboard
	 */
	public void hideKeyboard() {
		// Check if no view has focus:
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		// Find the currently focused view, so we can grab the correct window
		// token from it.
		View view = activity.getCurrentFocus();
		// If no view currently has focus, create a new one, just so we can grab
		// a window token from it
		if (view == null) {
			view = new View(activity);
		}
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	private String changeDateFormat(String dateString, String oldDateFormat, String newDateFormat) {

		DateFormat fromFormat = new SimpleDateFormat(oldDateFormat);
		fromFormat.setLenient(false);
		DateFormat toFormat = new SimpleDateFormat(newDateFormat);
		toFormat.setLenient(false);
		Date date = null;
		try {
			date = fromFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			Log.d("DBG", "Nu am putut sa schimb formatul datei din filtru " + e.getMessage());
		}
		return toFormat.format(date);
	}

	// Obsolete
	public void hideKeyboardEndActivity() {

		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

	}

	// Obsolete
	public static class CustomDateSetListener implements DatePickerDialog.OnDateSetListener {
		TextView view;

		public CustomDateSetListener(View v) {
			this.view = (TextView) v;
		}

		@Override
		public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
			Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

			view.setText(sdf.format(calendar.getTime()));
		}
	}

	@Override
	public void opAgentComplete(ArrayList<HashMap<String, String>> listAgenti) {
		populateSpinnerConsilieri(opAgenti.getListObjAgenti());

	}

	@Override
	public void onTaskComplete(String methodName, Object result) {
		if (methodName.equals(POPULATE_TABLES)) {

			try {
				OperatiiTabele opTab = new OperatiiTabele();

				BeanDateTabele dateTabele = opTab.deserializeObiective((String) result);

				SQLiteHelper sqLiteDatabase = new SQLiteHelper(activityContext);
				sqLiteDatabase.doSomething();

				sqLiteDatabase.clearLocalTables();
				sqLiteDatabase.populateObjectivesPhases(dateTabele.getListStadii());
				sqLiteDatabase.populateBeneficiaries(dateTabele.getListBeneficiari());
				sqLiteDatabase.populateObjectives(dateTabele.getListObiective());

				populateListConsilieri(codFiliala);

			} catch (Exception ex) {
				Toast.makeText(activityContext, ex.toString(), Toast.LENGTH_SHORT).show();
			}

		}

	}
}
