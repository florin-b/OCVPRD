package com.stimasoft.obiectivecva.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Feeds suggestion information to the beneficiary AutoCompleteTextViews
 */
public class AutoCompleteBenefAdapter extends ArrayAdapter<Pair<Integer, String>> {


    public AutoCompleteBenefAdapter(Context context, int resource, List<Pair<Integer, String>> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        TextView nameText = (TextView) convertView.findViewById(android.R.id.text1);
        nameText.setText(getItem(pos).second);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), android.R.layout.simple_dropdown_item_1line, null);
        }
        TextView label = (TextView) convertView.findViewById(android.R.id.text1);
        label.setText(getItem(position).first);

        return label;
    }

}