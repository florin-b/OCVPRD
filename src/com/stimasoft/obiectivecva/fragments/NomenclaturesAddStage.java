package com.stimasoft.obiectivecva.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.adapters.NomSlidingTabAdapter;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Stage;

import java.util.LinkedHashMap;
import java.util.List;


public class NomenclaturesAddStage extends Fragment {
    private LinkedHashMap<Stage, List<Phase>> nomenclatures;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_nomenclatures_add_stage, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialise your views

        Log.d("DBG" , "Called stages view created");

        Bundle bundle = this.getArguments();
        nomenclatures = (LinkedHashMap) bundle.getSerializable(NomSlidingTabAdapter.KEY_NOMENCLATURES);


    }
}
