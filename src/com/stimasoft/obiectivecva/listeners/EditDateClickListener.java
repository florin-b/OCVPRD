package com.stimasoft.obiectivecva.listeners;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.ui.DatePickerDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Used to pass information to a date picker dialog
 * This class is used in order to assure the correct behaviour of the date fields.
 *
 * When a startDateField is modified, the minimum date of it's corresponding endDateField must be changed
 * to the selected date.
 *
 * When an endDateField is modified, the maximum date of it's corresponding startDateField must be changed
 * to the selected date.
 *
 * The default dates for the date picker dialog are also set and updated using this class.
 */
public class EditDateClickListener implements View.OnClickListener {

    private Context context;
    private AppCompatActivity activity;
    private int modifierPurpose;
    private int modifierType;
    private int changeDateTarget;
    private int limitDateTarget;

    private Calendar limit;
    private Calendar defaultDate;

    public static final int PURPOSE_ADD = 0;
    public static final int PURPOSE_EDIT = 1;
    public static final int TYPE_START = 0;
    public static final int TYPE_END = 1;

    /**
     * Constructor for the EditDateClickListener
     *
     * @param context The context from which the date click listener is launched
     * @param modifierPurpose The purpose of the date picker launch (add or edit)
     * @param modifierType What type of date field calls the date picker (start date or end date)
     * @param limit The limit of the date picker (upper or lower, based on modifierType)
     * @param defaultDate The default date selected by the date picker
     * @param changeDateTarget Reference to the date field that will be modified on date selection
     * @param limitDateTarget Reference to the date field that will have a new limit when a date is selected.
     */
    public EditDateClickListener(Context context, int modifierPurpose, int modifierType,
                                 Calendar limit, Calendar defaultDate,
                                 int changeDateTarget, int limitDateTarget) {

        this.context = context;
        this.activity = (AppCompatActivity) context;
        this.modifierPurpose = modifierPurpose;
        this.modifierType = modifierType;
        this.limit = limit;
        this.defaultDate = defaultDate;
        this.changeDateTarget = changeDateTarget;
        this.limitDateTarget = limitDateTarget;
    }

    @Override
    public void onClick(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);

        Bundle b = new Bundle();
        // Send the default date to the date picker dialog
        b.putInt(DatePickerDialogFragment.YEAR, defaultDate.get(Calendar.YEAR));
        b.putInt(DatePickerDialogFragment.MONTH, defaultDate.get(Calendar.MONTH));
        b.putInt(DatePickerDialogFragment.DATE, defaultDate.get(Calendar.DAY_OF_MONTH));

        // Send the view that will be changed on date pick and the view that will have it's limit set
        b.putInt(DatePickerDialogFragment.CHANGE_TARGET, changeDateTarget);
        b.putInt(DatePickerDialogFragment.LIMIT_TARGET, limitDateTarget);

        // Send the limit for the current view
        if (limit != null) {
            b.putString(DatePickerDialogFragment.LIMIT, sdf.format(limit.getTime()));
        }

        // Specify the type of field that calls this listener and the purpose.
        b.putInt(DatePickerDialogFragment.TYPE, modifierType);
        b.putInt(DatePickerDialogFragment.PURPOSE, modifierPurpose);

        // Instantiate and display the date picker dialog
        DatePickerDialogFragment picker = new DatePickerDialogFragment();
        picker.setArguments(b);
        picker.show(activity.getSupportFragmentManager(), "frag_date_picker");

    }

    // Getters and setters
    public Calendar getLimit() {
        return limit;
    }

    public void setLimit(Calendar limit) {
        this.limit = limit;
    }

    public Calendar getDefaultDate() {
        return defaultDate;
    }

    public void setDefaultDate(Calendar defaultDate) {
        this.defaultDate = defaultDate;
    }
}