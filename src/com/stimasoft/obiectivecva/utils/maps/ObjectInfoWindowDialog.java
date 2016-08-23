package com.stimasoft.obiectivecva.utils.maps;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_utilities.ObjectiveData;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.StagePhaseSpinnerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by filip on 01/07/2015.
 */
public class ObjectInfoWindowDialog extends DialogFragment implements View.OnClickListener{
    InfoWinCommunicator communicator;
    Button btnYes,btnNo;
    DatePicker datePickerEnd, datePickerStart;
    TextView lblPhaseStartDate;
    EditText phaseDuration;
    Spinner stageSpinner, phaseSpinner;

    private int objectiveId;
    private int stageId;
    private int phaseId;

    private String expirationStart;


    private String expirationEnd;

    private boolean initializedStageSpinner = false;

    private String stageName;
    private String phaseName;

    private String daysVal;

    public String getExpirationStart() {
        return expirationStart;
    }

    public void setExpirationStart(String expirationStart) {
        this.expirationStart = expirationStart;
    }

    public String getExpirationEnd() {
        return expirationEnd;
    }

    public void setExpirationEnd(String expirationEnd) {
        this.expirationEnd = expirationEnd;
    }

    public int getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(int objectiveId) {
        this.objectiveId = objectiveId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(int phaseId) {
        this.phaseId = phaseId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }


    public String getDaysVal() {
        return daysVal;
    }

    public void setDaysVal(String daysVal) {
        this.daysVal = daysVal;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        communicator = (InfoWinCommunicator) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);

        getDialog().setTitle(R.string.dialog_infowindow_title);
        View view = inflater.inflate(R.layout.dialog_infowindow, null);

        btnYes = (Button) view.findViewById(R.id.btnInfoWinYes);
        btnYes.setOnClickListener(this);

        btnNo = (Button) view.findViewById(R.id.btnInfoWinNo);
        btnNo.setOnClickListener(this);

        datePickerStart = (DatePicker) view.findViewById(R.id.obj_start);
        datePickerStart.setCalendarViewShown(false);

        datePickerEnd = (DatePicker) view.findViewById(R.id.obj_end);
        datePickerEnd.setCalendarViewShown(false);


        lblPhaseStartDate = (TextView) view.findViewById(R.id.label_objective_phaseStartDate);

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

            // set start calendar
                Calendar expPhaseStartDate = new GregorianCalendar();

                try {
                    if(getExpirationEnd() != null)
                        expPhaseStartDate.setTime(sdf.parse(getExpirationStart()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                datePickerStart.updateDate(expPhaseStartDate.get(Calendar.YEAR), expPhaseStartDate.get(Calendar.MONTH), expPhaseStartDate.get(Calendar.DAY_OF_MONTH));

            // set end date
                Calendar expPhaseDate = new GregorianCalendar();

                try {
                    if(getExpirationEnd() != null)
                    expPhaseDate.setTime(sdf.parse(getExpirationEnd()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                datePickerEnd.updateDate(expPhaseDate.get(Calendar.YEAR), expPhaseDate.get(Calendar.MONTH), expPhaseDate.get(Calendar.DAY_OF_MONTH));

        stageSpinner = (Spinner) view.findViewById(R.id.spnrStage);
        phaseSpinner = (Spinner) view.findViewById(R.id.spnrPhase);

        final StagePhaseSpinnerUtils sp_spinner = new StagePhaseSpinnerUtils(getActivity());

        //TODO @Filip trebuie sa inlocuiesti 0 din functia de mai jos cu 1 daca tipul este "renovare"
        sp_spinner.populateAvailableStagesSpinner(stageSpinner, getStageId(), 0, stageSpinner.getSelectedItemPosition());

        stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selectedStageId = ((Stage) stageSpinner.getSelectedItem()).getId();

                //if(!initializedStageSpinner)
                {
                    //TODO @Andrei modifica in add-edit sa folosesti functia asta:
                    sp_spinner.populateAvailablePhasesSpinner(phaseSpinner, getObjectiveId(), selectedStageId);

                    if(getPhaseId() > 0) {
                        int selectedPhasePosition = sp_spinner.getPhasePosition(phaseSpinner, getPhaseId());
                        phaseSpinner.setSelection(selectedPhasePosition);
                    }

                    int selectedPhaseId = ((Phase) phaseSpinner.getSelectedItem()).getId();

                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
                    Calendar expPhaseStartDate = new GregorianCalendar();
                    Calendar expPhaseEndDate = new GregorianCalendar();

                    if(selectedPhaseId == getPhaseId()) {
                        // set start calendar

                        try {
                            if(getExpirationStart() != null)
                                expPhaseStartDate.setTime(sdf.parse(getExpirationStart()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        datePickerStart.updateDate(expPhaseStartDate.get(Calendar.YEAR), expPhaseStartDate.get(Calendar.MONTH), expPhaseStartDate.get(Calendar.DAY_OF_MONTH));

                        // set new expiration Date, new start
                        try {
                            expPhaseEndDate.setTime(sdf.parse(getExpirationEnd()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        datePickerEnd.updateDate(expPhaseEndDate.get(Calendar.YEAR), expPhaseEndDate.get(Calendar.MONTH), expPhaseEndDate.get(Calendar.DAY_OF_MONTH));

                        datePickerStart.setVisibility(View.GONE);
                        lblPhaseStartDate.setVisibility(View.GONE);
                    }else{

                            // set start calendar
                            try {
                                if(getExpirationEnd() != null)
                                    expPhaseStartDate.setTime(sdf.parse(getExpirationEnd()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datePickerStart.updateDate(expPhaseStartDate.get(Calendar.YEAR), expPhaseStartDate.get(Calendar.MONTH), expPhaseStartDate.get(Calendar.DAY_OF_MONTH));

                            // set new expiration Date, new start
                            try {
                                expPhaseEndDate.setTime(expPhaseStartDate.getTime());
                                //expPhaseEndDate.setTime(sdf.parse(datePickerStart.getYear()+"-"+datePickerStart.getMonth()+"-"+datePickerStart.getDayOfMonth()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            expPhaseEndDate.add(expPhaseStartDate.DATE, Integer.parseInt(phaseDuration.getText().toString()));
                            datePickerEnd.updateDate(expPhaseEndDate.get(Calendar.YEAR), expPhaseEndDate.get(Calendar.MONTH), expPhaseEndDate.get(Calendar.DAY_OF_MONTH));

                        datePickerStart.setVisibility(View.VISIBLE);
                        lblPhaseStartDate.setVisibility(View.VISIBLE);
                    }

                   /* // set start calendar
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
                    Calendar expPhaseStartDate = new GregorianCalendar();

                    try {
                        if(getExpirationEnd() != null)
                            expPhaseStartDate.setTime(sdf.parse(getExpirationStart()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    datePickerStart.updateDate(expPhaseStartDate.get(Calendar.YEAR), expPhaseStartDate.get(Calendar.MONTH), expPhaseStartDate.get(Calendar.DAY_OF_MONTH));*/

                    initializedStageSpinner = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(getStageId() > 0) {

            int selectedStagePosition = sp_spinner.getStagePosition(stageSpinner, getStageId());
            stageSpinner.setSelection(selectedStagePosition);

        }
        /*
        int selectedStageId = ((Stage) stageSpinner.getSelectedItem()).getId();

        //sp_spinner.populatePhasesSpinner(phaseSpinner, selectedStageId);

        sp_spinner.populateAvailablePhasesSpinner(phaseSpinner, getObjectiveId(),selectedStageId, getPhaseId());

        if(getPhaseId() > 0) {
            int selectedPhasePosition = sp_spinner.getPhasePosition(phaseSpinner, getPhaseId());
            phaseSpinner.setSelection(selectedPhasePosition);
        }
        */

        phaseDuration = (EditText) view.findViewById(R.id.txtPahseDay);
        phaseDuration.setText(getDaysVal());

        phaseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int phaseDefaultDuration = ((Phase) phaseSpinner.getSelectedItem()).getDays();
                phaseDuration.setText(Integer.toString(phaseDefaultDuration));
                int selectedPhaseId = ((Phase) phaseSpinner.getSelectedItem()).getId();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
                Calendar expPhaseStartDate = new GregorianCalendar();
                Calendar expPhaseEndDate = new GregorianCalendar();

                if (selectedPhaseId == getPhaseId()) {
                    // set start calendar

                    try {
                        if (getExpirationStart() != null)
                            expPhaseStartDate.setTime(sdf.parse(getExpirationStart()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    datePickerStart.updateDate(expPhaseStartDate.get(Calendar.YEAR), expPhaseStartDate.get(Calendar.MONTH), expPhaseStartDate.get(Calendar.DAY_OF_MONTH));

                    // set new expiration Date, new start
                    try {
                        expPhaseEndDate.setTime(sdf.parse(getExpirationEnd()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    datePickerEnd.updateDate(expPhaseEndDate.get(Calendar.YEAR), expPhaseEndDate.get(Calendar.MONTH), expPhaseEndDate.get(Calendar.DAY_OF_MONTH));

                    datePickerStart.setVisibility(View.GONE);
                    lblPhaseStartDate.setVisibility(View.GONE);
                } else {

                    // set start calendar
                    try {
                        if (getExpirationStart() != null)
                            expPhaseStartDate.setTime(sdf.parse(getExpirationEnd()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    datePickerStart.updateDate(expPhaseStartDate.get(Calendar.YEAR), expPhaseStartDate.get(Calendar.MONTH), expPhaseStartDate.get(Calendar.DAY_OF_MONTH));

                    // set new expiration Date, new start
                    try {
                        //Date alertStartDate = expPhaseStartDate.getTime();
                        //String expirationStartDate = sdf.format(alertStartDate);

                        //expPhaseEndDate.setTime(sdf.parse(datePickerStart.getYear()+"-"+datePickerStart.getMonth()+"-"+datePickerStart.getDayOfMonth()));
                        expPhaseEndDate.setTime(expPhaseStartDate.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    expPhaseEndDate.add(expPhaseStartDate.DATE, Integer.parseInt(phaseDuration.getText().toString()));
                    datePickerEnd.updateDate(expPhaseEndDate.get(Calendar.YEAR), expPhaseEndDate.get(Calendar.MONTH), expPhaseEndDate.get(Calendar.DAY_OF_MONTH));

                    datePickerStart.setVisibility(View.VISIBLE);
                    lblPhaseStartDate.setVisibility(View.VISIBLE);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        datePickerStart.init(datePickerStart.getYear(), datePickerStart.getMonth(), datePickerStart.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
                Calendar expPhaseStartDate = new GregorianCalendar();
                try {
                    if (getExpirationEnd() != null) {
                        //expPhaseStartDate.setTime(sdf.parse(datePickerStart.getYear()+"-"+datePickerStart.getMonth()+"-"+datePickerStart.getDayOfMonth()));
                        expPhaseStartDate.setTime(sdf.parse(getExpirationEnd()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                datePickerStart.setMinDate(expPhaseStartDate.getTime().getTime());

            }
        });

        datePickerEnd.init(datePickerStart.getYear(), datePickerStart.getMonth(), datePickerStart.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);
                Calendar expPhaseStartDate = new GregorianCalendar();
                try {
                    if(getExpirationStart() != null)
                    {
                        //expPhaseStartDate.setTime(sdf.parse(datePickerStart.getYear()+"-"+datePickerStart.getMonth()+"-"+datePickerStart.getDayOfMonth()));
                        expPhaseStartDate.setTime(sdf.parse(getExpirationEnd()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                datePickerEnd.setMinDate(expPhaseStartDate.getTime().getTime());

            }
        });

        return view;
    }


    @Override
    public void onClick(View view) {


//        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
//        im.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        HashMap result = new HashMap();
        if(view.getId() == R.id.btnInfoWinYes)
        {
            result.put("answer", true);

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);


            // Set Objective End Date
            int s_day = datePickerStart.getDayOfMonth();
            int s_month = datePickerStart.getMonth();
            int s_year =  datePickerStart.getYear();

            Calendar expPhaseStartDate = new GregorianCalendar();
            try {
                expPhaseStartDate.set(s_year,s_month,s_day);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Date alertStartDate = expPhaseStartDate.getTime();
            String expirationStartDate = sdf.format(alertStartDate);

            // Set Objective End Date
            int e_day = datePickerEnd.getDayOfMonth();
            int e_month = datePickerEnd.getMonth();
            int e_year =  datePickerEnd.getYear();

            Calendar expPhaseEndDate = new GregorianCalendar();
            try {
                expPhaseEndDate.set(e_year, e_month, e_day);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Date alertEndDate = expPhaseEndDate.getTime();
            String expirationEndDate = sdf.format(alertEndDate);


            int objectiveId = getObjectiveId();
            int selectedStageId = ((Stage) stageSpinner.getSelectedItem()).getId();
            int selectedPhaseId = ((Phase) phaseSpinner.getSelectedItem()).getId();
            int days = Integer.parseInt(phaseDuration.getText().toString());

            ObjectiveData data = new ObjectiveData(getActivity());

            data.setPhaseChanges(objectiveId, selectedStageId, selectedPhaseId, expirationStartDate, expirationEndDate, days);


            Toast.makeText(getActivity(), "Datele au fost modificate cu succes.", Toast.LENGTH_LONG).show();


            communicator.onDialogInfoWindowAnswer(result);
      }
        dismiss();
    }

    public interface InfoWinCommunicator
    {
        void onDialogInfoWindowAnswer(HashMap<String, Object> result);
    }

}

