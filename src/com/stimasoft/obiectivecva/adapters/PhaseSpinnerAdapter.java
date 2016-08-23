package com.stimasoft.obiectivecva.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.Phase;

import java.util.List;

/**
 * Feeds information from the Phase class to the assigned spinner
 */
public class PhaseSpinnerAdapter extends ArrayAdapter<Phase> {

    public PhaseSpinnerAdapter(Context context, int resource, List<Phase> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        TextView nameText = (TextView) convertView.findViewById(R.id.spinner_text);
        nameText.setText(getItem(pos).getName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.spinner_dropdown_item, null);
        }
        
        TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
        label.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        label.setText(getItem(position).getName());

        return convertView;
    }

}