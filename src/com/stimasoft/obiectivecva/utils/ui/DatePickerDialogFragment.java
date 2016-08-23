/*
 * Copyright 2012 David Cesarino de Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stimasoft.obiectivecva.utils.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.stimasoft.obiectivecva.listeners.EditDateClickListener;
import com.stimasoft.obiectivecva.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * <p>This class provides a usable {@link DatePickerDialog} wrapped as a {@link DialogFragment},
 * using the compatibility package v4. Its main advantage is handling Issue 34833
 * automatically for you.</p>
 * <p/>
 * <p>Current implementation (because I wanted that way =) ):</p>
 * <p/>
 * <ul>
 * <li>Only two buttons, a {@code BUTTON_POSITIVE} and a {@code BUTTON_NEGATIVE}.
 * <li>Buttons labeled from {@code android.R.string.ok} and {@code android.R.string.cancel}.
 * </ul>
 * <p/>
 * <p><strong>Usage sample:</strong></p>
 * <p/>
 * <pre>class YourActivity extends Activity implements OnDateSetListener
 * <p/>
 * // ...
 * <p/>
 * Bundle b = new Bundle();
 * b.putInt(DatePickerDialogFragment.YEAR, 2012);
 * b.putInt(DatePickerDialogFragment.MONTH, 6);
 * b.putInt(DatePickerDialogFragment.DATE, 17);
 * DialogFragment picker = new DatePickerDialogFragment();
 * picker.setArguments(b);
 * picker.show(getActivity().getSupportFragmentManager(), "fragment_date_picker");</pre>
 *
 * @author davidcesarino@gmail.com
 * @version 2012.0828
 * @see <a href="http://code.google.com/p/android/issues/detail?id=34833">Android Issue 34833</a>
 * @see <a href="http://stackoverflow.com/q/11444238/489607"
 * >Jelly Bean DatePickerDialog — is there a way to cancel?</a>
 */
public class DatePickerDialogFragment extends DialogFragment {

    public interface SetLimitsInterface{
        void onDateSelected(Calendar limit, int modifierPurpose, int modifierType, int changeTarget, int limitTarget);

    }

    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DATE = "Day";
    public static final String VIEW = "View";
    public static final String LIMIT = "limit";
    public static final String PURPOSE = "purpose";
    public static final String TYPE = "type";
    public static final String CHANGE_TARGET = "change_target";
    public static final String LIMIT_TARGET = "limit_target";

    private OnDateSetListener mListener;
    private int changeTarget;
    private int limitTarget;
    private int modifierPurpose;
    private int modifierType;
    private String limitDate;

    private SetLimitsInterface limitListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.limitListener = (SetLimitsInterface) activity;
    }

    @Override
    public void onDetach() {
        this.mListener = null;
        this.limitListener = null;
        super.onDetach();
    }

    @NonNull
    @TargetApi(11)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        int y = b.getInt(YEAR);
        int m = b.getInt(MONTH);
        int d = b.getInt(DATE);

        modifierPurpose = b.getInt(PURPOSE);
        modifierType = b.getInt(TYPE);
        limitDate = b.getString(LIMIT);
        limitTarget = b.getInt(LIMIT_TARGET);
        changeTarget = b.getInt(CHANGE_TARGET);

        // Jelly Bean introduced a bug in DatePickerDialog (and possibly 
        // TimePickerDialog as well), and one of the possible solutions is 
        // to postpone the creation of both the listener and the BUTTON_* .
        // 
        // Passing a null here won't harm because DatePickerDialog checks for a null
        // whenever it reads the listener that was passed here. >>> This seems to be 
        // true down to 1.5 / API 3, up to 4.1.1 / API 16. <<< No worries. For now.
        //
        // See my own question and answer, and details I included for the issue:
        //
        // http://stackoverflow.com/a/11493752/489607
        // http://code.google.com/p/android/issues/detail?id=34833
        //
        // Of course, suggestions welcome.

        final DatePickerDialog picker = new DatePickerDialog(getActivity(),
                getConstructorListener(), y, m, d);

        if (limitDate != null) {
            Calendar limitDateCalendar = new GregorianCalendar();

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT);
            try {
                Date dateLimit = sdf.parse(limitDate);
                limitDateCalendar.setTime(dateLimit);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            switch (modifierType) {
                case EditDateClickListener.TYPE_START:
                    picker.getDatePicker().setMaxDate(limitDateCalendar.getTimeInMillis());
                    break;

                case EditDateClickListener.TYPE_END:
                    picker.getDatePicker().setMinDate(limitDateCalendar.getTimeInMillis());
                    break;

                default:
                    break;
            }
        }

        if (hasJellyBeanAndAbove()) {
            picker.setButton(DialogInterface.BUTTON_POSITIVE,
                    getActivity().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatePicker dp = picker.getDatePicker();

                            Calendar setDate = new GregorianCalendar(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());

                            limitListener.onDateSelected(setDate, modifierPurpose, modifierType, changeTarget, limitTarget);

                        }
                    });

            picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getActivity().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        return picker;
    }

    private static boolean hasJellyBeanAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    private OnDateSetListener getConstructorListener() {
        return hasJellyBeanAndAbove() ? null : mListener;
    }
}
