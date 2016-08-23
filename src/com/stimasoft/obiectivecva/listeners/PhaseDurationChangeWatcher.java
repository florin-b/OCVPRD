package com.stimasoft.obiectivecva.listeners;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.stimasoft.obiectivecva.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Sets the limit of the phase end date based on the duration provided via interface.
 */
public class PhaseDurationChangeWatcher implements TextWatcher {

    private EditDateClickListener listenerToModify;
    private Context context;
    private AppCompatActivity activity;
    private TextView startDateView, endDateView;
    private PhaseDurationChangedInterface pdcInterface;

    /**
     * Interface designed to change the phase end date if the duration requirements are not met.
     */
    public interface PhaseDurationChangedInterface {
        // you can define any parameter as per your requirement
        void onPhaseDurationChanged(Calendar newLimit, boolean changeDate);
    }

    /**
     * Constructor for the PhaseDurationChangeWatcher
     *
     * @param context The context from which the watcher is called
     * @param listenerToModify The EditDateClickListener of the field which this change affects
     * @param startDateView The view that shows the start date of the phase
     * @param endDateView The view that shows the end date of the phase
     */
    public PhaseDurationChangeWatcher(Context context, EditDateClickListener listenerToModify,
                                      TextView startDateView, TextView endDateView) {

        this.listenerToModify = listenerToModify;
        this.context = context;
        this.activity = (AppCompatActivity) context;
        this.startDateView = startDateView;
        this.endDateView = endDateView;
        pdcInterface = (PhaseDurationChangedInterface) this.activity;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);

        Date startDate = new Date();
        Date endDate = new Date();

        // Parse the start and end dates of the phase using the dates from the provided textViews.
        try {

            startDate = sdf.parse(startDateView.getText().toString());
            endDate = sdf.parse(endDateView.getText().toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get the difference in days between the start and end date
        long diff = endDate.getTime() - startDate.getTime();
        int calculatedDays = (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        if(editable.toString().length() > 0) {
            int inputDays = Integer.parseInt(editable.toString());

            // If the difference in days is smaller than the specified number,
            // add the difference to the current limit
            if (calculatedDays < inputDays && listenerToModify.getLimit() != null) {
                Calendar listenerLimit = listenerToModify.getLimit();
                listenerLimit.add(Calendar.DAY_OF_MONTH, inputDays - calculatedDays);

                // Change the phase end date to the upper limit of the start date.
                pdcInterface.onPhaseDurationChanged(listenerLimit, true);
            }

            // If the difference is larger than the specified number, just set the limit
            // according to the specified duration
            else if(calculatedDays > inputDays && listenerToModify.getLimit()!= null){
                Calendar listenerLimit = new GregorianCalendar();
                listenerLimit.setTime(startDate);
                listenerLimit.add(Calendar.DAY_OF_MONTH, inputDays);

                // Do not change the phase end date
                pdcInterface.onPhaseDurationChanged(listenerLimit, false);

            }

        }
    }
}
