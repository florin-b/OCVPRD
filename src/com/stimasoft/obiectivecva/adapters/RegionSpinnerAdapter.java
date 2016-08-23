package com.stimasoft.obiectivecva.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.Region;

import java.util.List;

/**
 * Feeds information from the Region class to the assigned spinner
 */
public class RegionSpinnerAdapter extends ArrayAdapter<Region> {

    public RegionSpinnerAdapter(Context context, int resource, List<Region> objects) {
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
        label.setText(getItem(position).getName());

        return label;
    }

}
