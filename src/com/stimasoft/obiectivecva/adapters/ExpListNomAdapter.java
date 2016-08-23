package com.stimasoft.obiectivecva.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Region;
import com.stimasoft.obiectivecva.models.db_classes.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Feeds information to the nomenclatures expandable list
 */
public class ExpListNomAdapter extends BaseExpandableListAdapter {

    private Context context;
    private LinkedHashMap<Stage, List<Phase>> stagesMap;
    private List<Stage> stages;

    public ExpListNomAdapter(Context context,
                             LinkedHashMap<Stage, List<Phase>> stagesMap) {
        this.context = context;
        this.stagesMap = stagesMap;
        this.stages = new ArrayList<Stage>(stagesMap.keySet());
    }

    @Override
    public int getGroupCount() {
        return stagesMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.stagesMap.get(this.stages.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int position) {
        return stages.get(position);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.stagesMap.get(this.stages.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return stages.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return this.stagesMap.get(this.stages.get(groupPosition)).get(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Stage stage = (Stage) getGroup(groupPosition);
        String stageTitle = stage.getName();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.explistview_nom_stage, parent, false);
        }

        convertView.setPadding(50, 0, 50, 0);

        TextView stageTextView = (TextView) convertView.findViewById(R.id.textView_explistview_nom_stage);
        stageTextView.setText(stageTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Phase phase = (Phase) getChild(groupPosition, childPosition);
        String phaseTitle = phase.getName();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.explistview_nom_phase, parent, false);
        }
        TextView phaseTextView = (TextView) convertView.findViewById(R.id.textView_explistview_nom_phase);
        phaseTextView.setText(Integer.toString(phase.getHierarchy()) + " " + phaseTitle);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void refreshNomenclatures(LinkedHashMap<Stage, List<Phase>> stagesMap){
        this.stagesMap = stagesMap;
        this.stages = new ArrayList<Stage>(stagesMap.keySet());
        notifyDataSetChanged();
    }

    /**
     * Created by filip on 02/07/2015.
     */
    private static class RegionSpinnerAdapter extends ArrayAdapter<Region> {

        public RegionSpinnerAdapter(Context context, int resource, List<Region> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
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
}
