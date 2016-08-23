package com.stimasoft.obiectivecva;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.utils.Setup;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;

public class Reports extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navView_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        User user = sharedPrefHelper.getUserDetails();

        Setup setup = new Setup(this);
        setup.setupToolbar(toolbar);
        setup.setupDrawer(drawerLayout, navView, toolbar);
        //setup.setupDrawerHeader(navView, user);
        //setup.setupDrawerMenu(navView, drawerLayout, R.id.drawer_menu_rapoarte);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reports, menu);
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
}
