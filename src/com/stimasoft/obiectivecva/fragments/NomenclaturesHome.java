package com.stimasoft.obiectivecva.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.adapters.ExpListNomAdapter;
import com.stimasoft.obiectivecva.adapters.NomSlidingTabAdapter;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Stage;

import java.util.LinkedHashMap;
import java.util.List;

public class NomenclaturesHome extends Fragment {
    private LinkedHashMap<Stage, List<Phase>> nomenclatures;
    private ExpListNomAdapter adapter;

    public NomenclaturesHome() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_nomenclatures_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialise your views

        Bundle bundle = this.getArguments();
        nomenclatures = (LinkedHashMap) bundle.getSerializable(NomSlidingTabAdapter.KEY_NOMENCLATURES);

        ExpandableListView expListView = (ExpandableListView) view.findViewById(R.id.expListView_nomenclatures);

        adapter = new ExpListNomAdapter(getActivity(), nomenclatures);

        expListView.setAdapter(adapter);

        expListView.setOnChildClickListener(new ChildClickListener());

        expListView.setOnItemLongClickListener(new LongClickListener());

    }

    private class ChildClickListener implements ExpandableListView.OnChildClickListener{

        @Override
        public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long l) {
            ExpListNomAdapter tmpAdapter = adapter;

            RelativeLayout cardContainer = (RelativeLayout) getView().findViewById(R.id.relativeLayout_infoCardContainer);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);

            View phaseCard = cardContainer.findViewById(R.id.coordinator_phase_card);
            View stageCard = cardContainer.findViewById(R.id.coordinator_stage_card);

            if (phaseCard == null) {
                inflater.inflate(R.layout.phase_details_card, cardContainer, true);
                if(stageCard != null)
                    cardContainer.removeView(stageCard);
            }

            FloatingActionButton phaseFab = (FloatingActionButton) cardContainer.findViewById(R.id.fab_editPhase);
            phaseFab.setOnClickListener(new EditPhaseClickListener());

            TextView nameText = (TextView) cardContainer.findViewById(R.id.label_phase_name);
            TextView hierarchyText = (TextView) cardContainer.findViewById(R.id.value_phase_hierarchy);
            TextView stageText = (TextView) cardContainer.findViewById(R.id.value_phase_stageParent);
            TextView daysText = (TextView) cardContainer.findViewById(R.id.value_phase_days);
            TextView statusText = (TextView) cardContainer.findViewById(R.id.value_phase_status);

            Phase phase = (Phase) tmpAdapter.getChild(groupPosition, childPosition);
            Stage stage = (Stage) tmpAdapter.getGroup(groupPosition);

            nameText.setText(phase.getName());
            hierarchyText.setText(Integer.toString(phase.getHierarchy()));
            stageText.setText(stage.getName());
            daysText.setText(Integer.toString(phase.getDays()));
            statusText.setText(Integer.toString(phase.getStatus()));

            return true;
        }
    }

    private class LongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            int itemType = ExpandableListView.getPackedPositionType(id);
            long packedPosition;
            int groupPosition, childPosition;

            switch(itemType){
                case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
                    packedPosition = ((ExpandableListView) parent).getExpandableListPosition(position);

                    childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                    groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

                    Log.d("DBG", "Am apasat copilul " + childPosition + " si grupul " + groupPosition);
                    //do your per-item callback here
                    return true; //true if we consumed the click, false if not

                case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
                    packedPosition = ((ExpandableListView) parent).getExpandableListPosition(position);

                    groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

                    RelativeLayout cardContainer = (RelativeLayout) getView().findViewById(R.id.relativeLayout_infoCardContainer);
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

                    View stageCard = cardContainer.findViewById(R.id.coordinator_stage_card);
                    View phaseCard = cardContainer.findViewById(R.id.coordinator_phase_card);

                    if (stageCard == null) {
                        inflater.inflate(R.layout.stage_details_card, cardContainer, true);
                        if(phaseCard != null)
                            cardContainer.removeView(phaseCard);
                    }
                    Log.wtf("WTF","WTF");
                    TextView nameText = (TextView) cardContainer.findViewById(R.id.label_stage_name);
                    TextView hierarchyText = (TextView) cardContainer.findViewById(R.id.value_stage_hierarchy);
                    TextView statusText = (TextView) cardContainer.findViewById(R.id.value_stage_status);

                    Stage stage = (Stage) adapter.getGroup(groupPosition);

                    nameText.setText(stage.getName());
                    hierarchyText.setText(Integer.toString(stage.getHierarchy()));
                    statusText.setText(Integer.toString(stage.getStatus()));

                    Log.d("DBG", "Am long-apasat butonu "+ groupPosition);
                    return true; //true if we consumed the click, false if not

                default: return true;
            }
        }
    }

    private class EditPhaseClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
//            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.nomenclature_viewPager);
//            viewPager.setCurrentItem(1,true);
        }
    }
}
