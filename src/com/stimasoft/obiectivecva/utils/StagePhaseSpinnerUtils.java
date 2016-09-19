package com.stimasoft.obiectivecva.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.widget.Spinner;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.adapters.PhaseSpinnerAdapter;
import com.stimasoft.obiectivecva.adapters.StageSpinnerAdapter;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_utilities.PhaseData;
import com.stimasoft.obiectivecva.models.db_utilities.StageData;

import java.util.ArrayList;

/**
 * Created by filip on 03/07/2015.
 * //TODO @Filip documentation here where needed
 */
public class StagePhaseSpinnerUtils {
    private Context context;
    private Activity activity;

    public StagePhaseSpinnerUtils(Context context) {
        this.context = context;
        this.activity = (Activity) context;
    }

//    public ArrayList<Stage> populateStagesSpinner(Spinner spinner) {
//        StageData stageData = new StageData(context);
//
//        ArrayList<Stage> stages = stageData.getAllStages();
//
//        StageSpinnerAdapter adapter = new StageSpinnerAdapter(context,
//                android.R.layout.simple_spinner_item,
//                stages);
//
//        spinner.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        return stages;
//    }

    public Pair<ArrayList<Stage>, Integer> populateAvailableStagesSpinner(Spinner spinner, int ObjectiveId, int hierarchyLimit, int initialSelection) {
        StageData stageData = new StageData(context);
        ArrayList<Stage> stages;

//        if(ObjectiveId > 0)
//        {
//            stages = stageData.getAvailableStages(ObjectiveId);
//        }else {
//            stages = stageData.getAllStages();
//        }

        stages = stageData.getAllStages(hierarchyLimit);
        
        //stage and phase spinners, Author: Alin
        stages.add(0,new Stage(0,"Selectati stadiul",0));
        

        StageSpinnerAdapter adapter = new StageSpinnerAdapter(context,
                R.layout.spinner_dropdown_item,
                stages);

        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(initialSelection < 0){
            spinner.setSelection(0);
        }
        else {
            spinner.setSelection(initialSelection);
        }
        return new Pair(stages, initialSelection);
    }

    public ArrayList<Phase> populatePhasesSpinner(Spinner spinner, int selectedStage) {
        PhaseData phaseData = new PhaseData(context);

        ArrayList<Phase> phases = phaseData.getAllPhasesForStage(selectedStage);

        PhaseSpinnerAdapter adapter = new PhaseSpinnerAdapter(context,
                R.layout.spinner_dropdown_item,
                phases);

        spinner.setAdapter(adapter);

        return phases;
    }

    public ArrayList<Phase> populateAvailablePhasesSpinner(Spinner spinner, int objectiveId, int selectedStage) {
        PhaseData phaseData = new PhaseData(context);

        ArrayList<Phase> phases = phaseData.getAvailabelePhasesForStage(objectiveId,selectedStage);
        
        //stages and phases spinner, Author: Alin
        phases.add(0,new Phase(0,"Selectati faza",0));
        
        PhaseSpinnerAdapter adapter = new PhaseSpinnerAdapter(context,
                R.layout.spinner_dropdown_item,
                phases);

        spinner.setAdapter(adapter);

        return phases;
    }

    public ArrayList<Phase> populatePhasesSpinner(Spinner spinner, int selectedStage, int selectedPhase) {
        PhaseData phaseData = new PhaseData(context);

        ArrayList<Phase> phases = phaseData.getAllPhasesForStage(selectedStage);

        PhaseSpinnerAdapter adapter = new PhaseSpinnerAdapter(context,
                android.R.layout.simple_spinner_item,
                phases);

        spinner.setAdapter(adapter);

        spinner.setSelection(selectedPhase, true);
        return phases;
    }


    // get current option from spinner by it's name
    public int getStagePosition(Spinner spinner, int stageID)
    {
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            int spinnerOption = ((Stage)spinner.getAdapter().getItem(i)).getId();
            if (spinnerOption == stageID){
                index = i;
                break;
            }
        }
        return index;
    }

    // get current option from spinner by it's name
    public int getPhasePosition(Spinner spinner, int phaseID)
    {
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            int spinnerOption = ((Phase)spinner.getAdapter().getItem(i)).getId();
            if (spinnerOption == phaseID){
                index = i;
                break;
            }
        }
        return index;
    }
}

