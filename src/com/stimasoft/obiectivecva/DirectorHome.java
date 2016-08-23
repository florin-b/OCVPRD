package com.stimasoft.obiectivecva;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.Setup;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;

public class DirectorHome extends AppCompatActivity {

	private User user;
	private boolean doubleBackToExitPressedOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_director_home);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		NavigationView navView = (NavigationView) findViewById(R.id.navView_drawer);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
		user = sharedPrefHelper.getUserDetails();

		LinearLayout layoutMap = (LinearLayout) findViewById(R.id.relativeLayout_mapButton);
		layoutMap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), MapActivity.class);
				intent.putExtra(Constants.KEY_FLAG, Constants.FLAG_FILTERS_OPEN);
				startActivity(intent);
			}
		});

		LinearLayout layoutList = (LinearLayout) findViewById(R.id.relativeLayout_listButton);
		layoutList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), Objectives.class);
				intent.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ONGOING);
				intent.putExtra(Constants.KEY_FLAG, Constants.FLAG_FILTERS_OPEN);
				startActivity(intent);
			}
		});

		Setup setup = new Setup(this);
		setup.setupToolbar(toolbar);
		setup.setupDrawer(drawerLayout, navView, toolbar);
		setup.setupDrawerHeader(navView, user);
		setup.setupDrawerMenu(navView, drawerLayout, R.id.drawer_menu_obiective);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_director_home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		if (drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout.isDrawerOpen(GravityCompat.END)) { // replace
																												// this
																												// with
																												// actual
																												// function
																												// which
																												// returns
																												// if
																												// the
																												// drawer
																												// is
																												// open
			drawerLayout.closeDrawers(); // replace this with actual function
											// which closes drawer
		} else if (user.getUserType() == User.TYPE_DVA) {

			if (doubleBackToExitPressedOnce) {
				super.onBackPressed();
				SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
				sharedPrefHelper.logOut();

				Toast.makeText(this, getString(R.string.toast_loggedOut), Toast.LENGTH_SHORT).show();

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

}
