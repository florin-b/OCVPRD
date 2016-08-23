package com.stimasoft.obiectivecva.adapters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.stimasoft.obiectivecva.fragments.NomenclaturesAddPhase;
import com.stimasoft.obiectivecva.fragments.NomenclaturesAddStage;
import com.stimasoft.obiectivecva.fragments.NomenclaturesHome;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Stage;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by andrei on 23/06/2015.
 */
public class NomSlidingTabAdapter extends FragmentStatePagerAdapter {
    public static final String KEY_NOMENCLATURES = "nomenclatures";
    private LinkedHashMap<Stage, List<Phase>> nomenclatures;
    private CharSequence[] Titles; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    public NomSlidingTabAdapter(FragmentManager fm,
                                CharSequence mTitles[],
                                int mNumbOfTabsumb, LinkedHashMap<Stage, List<Phase>> nomenclatures) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.nomenclatures = nomenclatures;

    }
    //This method returns the fragment for the every position in the View Pager
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        Bundle bundle;
        switch (position) {
            case 0:
                NomenclaturesHome nomHome = new NomenclaturesHome();
                bundle = new Bundle();
                bundle.putSerializable(KEY_NOMENCLATURES, nomenclatures);
                nomHome.setArguments(bundle);
                return nomHome;

            case 1:
                NomenclaturesAddStage nomStage = new NomenclaturesAddStage();
                bundle = new Bundle();
                bundle.putSerializable(KEY_NOMENCLATURES, nomenclatures);
                nomStage.setArguments(bundle);
                return nomStage;

            case 2:
                NomenclaturesAddPhase nomPhase = new NomenclaturesAddPhase();
                bundle = new Bundle();
                bundle.putSerializable(KEY_NOMENCLATURES, nomenclatures);
                nomPhase.setArguments(bundle);
                return nomPhase;

            default:
                break;
        }

        return null;
    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}
