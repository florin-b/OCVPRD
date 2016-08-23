package com.stimasoft.obiectivecva;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.stimasoft.obiectivecva.adapters.ExpListNomAdapter;
import com.stimasoft.obiectivecva.models.db_classes.Phase;
import com.stimasoft.obiectivecva.models.db_classes.Stage;
import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.models.db_utilities.PhaseData;
import com.stimasoft.obiectivecva.models.db_utilities.StageData;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;
import com.stimasoft.obiectivecva.utils.Setup;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;
import com.stimasoft.obiectivecva.utils.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Nomenclatures extends AppCompatActivity {

    private LinkedHashMap<Stage, List<Phase>> nomenclatures;
    private ExpListNomAdapter adapter;
    private ExpandableListView expListView;

    private LinearLayout addNomenclaturesLayout;
    private LinearLayout addNomenclaturesLayoutBottom;

    private Phase currentPhase;
    private Stage currentStage;

    private Setup setup;

    private User user;

    private static final int MODIFIER_STAGE = 0;
    private static final int MODIFIER_PHASE = 1;
    private static final int MODIFIER_HOME = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nomenclatures);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navView_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(this);
        user = sharedPrefHelper.getUserDetails();

        setup = new Setup(this);
        setup.setupToolbar(toolbar);
        setup.setupDrawer(drawerLayout, navView, toolbar);
        setup.setupDrawerHeader(navView, user);
        setup.setupDrawerMenu(navView, drawerLayout, R.id.drawer_menu_nomenclatoare);

        SQLiteHelper db = new SQLiteHelper(this);
        nomenclatures = db.getNomenclatures();

        expListView = (ExpandableListView) findViewById(R.id.expListView_nomenclatures);

        adapter = new ExpListNomAdapter(this, nomenclatures);

        expListView.setAdapter(adapter);

        expListView.setOnChildClickListener(new ChildClickListener());

        expListView.setOnItemLongClickListener(new LongClickListener());

        View addStageView = findViewById(R.id.button_addStageCard);
        addStageView.setOnClickListener(new AddStageClickListener());

        View addPhaseView = findViewById(R.id.button_addPhaseCard);
        addPhaseView.setOnClickListener(new AddPhaseClickListener());

        View addStageBottomView = findViewById(R.id.button_addStageCardBottom);
        addStageBottomView.setOnClickListener(new AddStageClickListener());

        View addPhaseBottomView = findViewById(R.id.button_addPhaseCardBottom);
        addPhaseBottomView.setOnClickListener(new AddPhaseClickListener());

        addNomenclaturesLayout = (LinearLayout) findViewById(R.id.linearLayout_addNomenclatures);
        addNomenclaturesLayoutBottom = (LinearLayout) findViewById(R.id.linearLayout_addNomenclaturesBottom);

        FloatingActionButton fabAddPhase = (FloatingActionButton) findViewById(R.id.fab_addPhase);
        FloatingActionButton fabAddPhaseBottom = (FloatingActionButton) findViewById(R.id.fab_addPhaseBottom);
        FloatingActionButton fabAddStage = (FloatingActionButton) findViewById(R.id.fab_addStage);
        FloatingActionButton fabAddStageBottom = (FloatingActionButton) findViewById(R.id.fab_addStageBottom);

        fabAddPhase.setOnClickListener(new AddPhaseClickListener());
        fabAddPhaseBottom.setOnClickListener(new AddPhaseClickListener());
        fabAddStage.setOnClickListener(new AddStageClickListener());
        fabAddStageBottom.setOnClickListener(new AddStageClickListener());

        if(user.getUserType() != User.TYPE_DVA){
            addNomenclaturesLayout.setVisibility(View.GONE);
            addNomenclaturesLayoutBottom.setVisibility(View.GONE);
            fabAddPhase.setVisibility(View.GONE);
            fabAddPhaseBottom.setVisibility(View.GONE);
            fabAddStage.setVisibility(View.GONE);
            fabAddStageBottom.setVisibility(View.GONE);
            addStageView.setVisibility(View.GONE);
            addPhaseView.setVisibility(View.GONE);
        }
//        List<String> titlesList = new ArrayList<String>();
//        titlesList.add("Nomenclatoare");
//        titlesList.add("Etape");
//        titlesList.add("Faze");

//        CharSequence titles[] = titlesList.toArray(new CharSequence[titlesList.size()]);
//
//        SQLiteHelper db = new SQLiteHelper(this);
//        nomenclatures = db.getNomenclatures();
//        Log.d("DBG", "Am luat nomenclaturi");
//
//        NomSlidingTabAdapter nomSlidingTabAdapter = new NomSlidingTabAdapter(getSupportFragmentManager(),
//                                                                             titles, titlesList.size(), nomenclatures);
//
//        ViewPager viewPager = (ViewPager) findViewById(R.id.nomenclature_viewPager);
//        viewPager.setAdapter(nomSlidingTabAdapter);
//
//        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.nomenclature_slidingTabs);
//        slidingTabLayout.setViewPager(viewPager);
//
//        final List<PagerItemTab> tabs = new ArrayList<>();
//        tabs.add(new PagerItemTab("Nomenclatoare", getResources().getColor(R.color.colorAccent)));
//        tabs.add(new PagerItemTab("Etape", getResources().getColor(R.color.colorAccent)));
//        tabs.add(new PagerItemTab("Faze", getResources().getColor(R.color.colorAccent)));
//
//        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//            @Override
//            public int getIndicatorColor(int position) {
//                return tabs.get(position).getIndicatorColor();
//            }
//
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nomenclatures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class ChildClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long l) {
            ExpListNomAdapter tmpAdapter = adapter;

            currentPhase = (Phase) tmpAdapter.getChild(groupPosition, childPosition);
            currentStage = (Stage) tmpAdapter.getGroup(groupPosition);

            showPhaseDetails();

            return true;
        }

    }

    private class LongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            int itemType = ExpandableListView.getPackedPositionType(id);
            long packedPosition;
            int groupPosition, childPosition;

            switch (itemType) {
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

                    currentStage = (Stage) adapter.getGroup(groupPosition);

                    showStageDetails();

                    return true; //true if we consumed the click, false if not

                default:
                    return true;
            }
        }
    }

    private class EditPhaseClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
            View phaseCard = cardContainer.findViewById(R.id.phase_add_edit_card);

            if (phaseCard == null) {
                cardContainer.removeAllViews();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.phase_add_edit_card, cardContainer, true);
            }

            List<Stage> stages = new ArrayList<Stage>(nomenclatures.keySet());

            int selectedStage = 0;

            for (int i = 0; i < stages.size(); i++) {
                if (stages.get(i) == currentStage) {
                    selectedStage = i;
                }
            }

            Spinner stageSpinner = (Spinner) findViewById(R.id.value_addEditPhase_stageParent);
            stageSpinner.setAdapter(new StageSpinnerAdapter(getApplicationContext(),
                    android.R.layout.simple_spinner_item,
                    stages));
            stageSpinner.setSelection(selectedStage);

            TextView title = (TextView) findViewById(R.id.label_addEditPhase_title);
            title.setText("Editează faza");

            EditText editName = (EditText) findViewById(R.id.value_addEditPhase_name);
            EditText editDays = (EditText) findViewById(R.id.value_addEditPhase_days);
            EditText editHierarchy = (EditText) findViewById(R.id.value_addEditPhase_hierarchy);

            editName.setText(currentPhase.getName());
            editDays.setText(Integer.toString(currentPhase.getDays()));
            editHierarchy.setText(Integer.toString(currentPhase.getHierarchy()));

            FloatingActionButton fabEditPhase = (FloatingActionButton) findViewById(R.id.fab_addEditPhase);
            fabEditPhase.setOnClickListener(new SavePhaseClickListener(SavePhaseClickListener.ACTION_EDIT));

            FloatingActionButton fabCancelEditPhase = (FloatingActionButton) findViewById(R.id.fab_cancelEditPhase);
            fabCancelEditPhase.setOnClickListener(new CancelClickListener(MODIFIER_PHASE));
        }
    }

    private class EditStageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
            View stageCard = cardContainer.findViewById(R.id.stage_add_edit_card);

            if (stageCard == null) {
                cardContainer.removeAllViews();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.stage_add_edit_card, cardContainer, true);
            }

            TextView title = (TextView) findViewById(R.id.label_addEditStage_title);
            title.setText("Editează etapă");

            EditText editName = (EditText) findViewById(R.id.value_addEditStage_name);
            EditText editHierarchy = (EditText) findViewById(R.id.value_addEditStage_hierarchy);

            editName.setText(currentStage.getName());
            editHierarchy.setText(Integer.toString(currentStage.getHierarchy()));

            FloatingActionButton fabEditPhase = (FloatingActionButton) findViewById(R.id.fab_addEditStage);
            fabEditPhase.setOnClickListener(new SaveStageClickListener(SaveStageClickListener.ACTION_EDIT));

            FloatingActionButton fabCancelEditStage = (FloatingActionButton) findViewById(R.id.fab_cancelEditStage);
            fabCancelEditStage.setOnClickListener(new CancelClickListener(MODIFIER_STAGE));
        }
    }

    private class SavePhaseClickListener implements View.OnClickListener {

        public static final int ACTION_EDIT = 0;
        public static final int ACTION_SAVE = 1;
        int action;

        public SavePhaseClickListener(int action) {
            this.action = action;
        }

        @Override
        public void onClick(View view) {

            boolean inputIsValid = true;

            SQLiteHelper db = new SQLiteHelper(getApplicationContext());

            Spinner stageSpinner = (Spinner) findViewById(R.id.value_addEditPhase_stageParent);
            Spinner statusSpinner = (Spinner) findViewById(R.id.value_addEditPhase_status);
            EditText editName = (EditText) findViewById(R.id.value_addEditPhase_name);
            EditText editDays = (EditText) findViewById(R.id.value_addEditPhase_days);
            EditText editHierarchy = (EditText) findViewById(R.id.value_addEditPhase_hierarchy);

            Stage selectedStage = (Stage) stageSpinner.getSelectedItem();

            editName.setError(null);
            editDays.setError(null);
            editHierarchy.setError(null);

            // Validations
            Validator validator = new Validator();

            if (!validator.existsText(editName.getText().toString())) {
                inputIsValid = false;
                editName.setError("Numele este obligatoriu");
            }

            if (!validator.isNumeric(editDays.getText().toString())) {
                inputIsValid = false;
                editDays.setError("Zilele sunt numere întregi");
            }

            if (!validator.existsText(editHierarchy.getText().toString())) {
                inputIsValid = false;
                editHierarchy.setError("Ierarhia este număr întreg");
            }

            if (inputIsValid) {
                PhaseData phaseData = new PhaseData(getApplicationContext());
                StageData stageData = new StageData(getApplicationContext());
                if (action == ACTION_EDIT) {
                    Phase updatedPhase = new Phase(currentPhase.getId(), selectedStage.getId(),
                            editName.getText().toString(),
                            Integer.parseInt(editDays.getText().toString()),
                            Integer.parseInt(editHierarchy.getText().toString()),
                            statusSpinner.getSelectedItemPosition());

                    currentPhase = phaseData.updatePhase(updatedPhase);
                    currentStage = stageData.getStage(currentPhase.getStageId(), db.getWritableDatabase());
                    showPhaseDetails();
                } else {
                    Phase updatedPhase = new Phase(editName.getText().toString(),
                            Integer.parseInt(editDays.getText().toString()),
                            Integer.parseInt(editHierarchy.getText().toString()),
                            selectedStage.getId(),
                            statusSpinner.getSelectedItemPosition());

                    currentPhase = phaseData.addPhase(updatedPhase);
                    currentStage = stageData.getStage(currentPhase.getStageId(), db.getWritableDatabase());
                    showPhaseDetails();
                }

                nomenclatures.clear();
                nomenclatures = db.getNomenclatures();
                adapter.refreshNomenclatures(nomenclatures);

                RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
                View phaseAddEditCard = findViewById(R.id.coordinator_phase_add_edit_card);

                cardContainer.removeView(phaseAddEditCard);

                Setup setup = new Setup(Nomenclatures.this);
                setup.hideKeyboard();
            }
        }
    }

    private class SaveStageClickListener implements View.OnClickListener {

        public static final int ACTION_EDIT = 0;
        public static final int ACTION_SAVE = 1;
        int action;

        public SaveStageClickListener(int action) {
            this.action = action;
        }

        @Override
        public void onClick(View view) {
            boolean inputIsValid = true;

            SQLiteHelper db = new SQLiteHelper(getApplicationContext());

            EditText editName = (EditText) findViewById(R.id.value_addEditStage_name);
            EditText editHierarchy = (EditText) findViewById(R.id.value_addEditStage_hierarchy);
            Spinner statusSpinner = (Spinner) findViewById(R.id.value_addEditStage_status);

            editName.setError(null);
            editHierarchy.setError(null);

            //Validations
            Validator validator = new Validator();

            if (!validator.existsText(editName.getText().toString())) {
                inputIsValid = false;
                editName.setError("Numele este obligatoriu");
            }

            if (!validator.existsText(editHierarchy.getText().toString())) {
                inputIsValid = false;
                editHierarchy.setError("Ierarhia este număr întreg");
            }

            if (inputIsValid) {
                StageData stageData = new StageData(getApplicationContext());
                if (action == ACTION_EDIT) {
                    Stage updatedStage = new Stage(currentStage.getId(), editName.getText().toString(),
                            Integer.parseInt(editHierarchy.getText().toString()),
                            statusSpinner.getSelectedItemPosition());

                    currentStage = stageData.updateStage(updatedStage);
                    showStageDetails();
                } else {
                    Stage updatedStage = new Stage(editName.getText().toString(),
                            Integer.parseInt(editHierarchy.getText().toString()),
                            statusSpinner.getSelectedItemPosition());

                    currentStage = stageData.addStage(updatedStage);
                    showStageDetails();
                }

                nomenclatures.clear();
                nomenclatures = db.getNomenclatures();
                adapter.refreshNomenclatures(nomenclatures);

                RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
                View stageAddEditCard = findViewById(R.id.coordinator_stage_add_edit_card);

                cardContainer.removeView(stageAddEditCard);

                Setup setup = new Setup(Nomenclatures.this);
                setup.hideKeyboard();
            }
        }
    }

    private class AddPhaseClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            addNomenclaturesLayout.setVisibility(View.GONE);

            RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View addStageCard = findViewById(R.id.coordinator_phase_add_edit_card);
            if (addStageCard == null) {
                cardContainer.removeAllViews();
                inflater.inflate(R.layout.phase_add_edit_card, cardContainer, true);
            }

            List<Stage> stages = new ArrayList<Stage>(nomenclatures.keySet());

            Spinner stageSpinner = (Spinner) findViewById(R.id.value_addEditPhase_stageParent);

            stageSpinner.setAdapter(new StageSpinnerAdapter(Nomenclatures.this,
                    android.R.layout.simple_spinner_item,
                    stages));

            stageSpinner.setSelection(0);

            TextView title = (TextView) findViewById(R.id.label_addEditPhase_title);
            title.setText("Adaugă o fază");

            Spinner statusSpinner = (Spinner) findViewById(R.id.value_addEditPhase_status);
            statusSpinner.setSelection(1);

            FloatingActionButton fabEditPhase = (FloatingActionButton) findViewById(R.id.fab_addEditPhase);
            fabEditPhase.setOnClickListener(new SavePhaseClickListener(SavePhaseClickListener.ACTION_SAVE));

            FloatingActionButton fabCancelEditPhase = (FloatingActionButton) findViewById(R.id.fab_cancelEditPhase);
            fabCancelEditPhase.setOnClickListener(new CancelClickListener(MODIFIER_HOME));

            addNomenclaturesLayoutBottom.setVisibility(View.VISIBLE);
        }

    }

    private class AddStageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            addNomenclaturesLayout.setVisibility(View.GONE);

            RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View addStageCard = findViewById(R.id.coordinator_stage_add_edit_card);
            if (addStageCard == null) {
                cardContainer.removeAllViews();
                inflater.inflate(R.layout.stage_add_edit_card, cardContainer, true);
            }

            TextView title = (TextView) findViewById(R.id.label_addEditStage_title);
            title.setText("Adaugă o etapă nouă");

            Spinner statusSpinner = (Spinner) findViewById(R.id.value_addEditStage_status);
            statusSpinner.setSelection(1);

            FloatingActionButton fabEditStage = (FloatingActionButton) findViewById(R.id.fab_addEditStage);
            fabEditStage.setOnClickListener(new SaveStageClickListener(SaveStageClickListener.ACTION_SAVE));

            FloatingActionButton fabCancelEditStage = (FloatingActionButton) findViewById(R.id.fab_cancelEditStage);
            fabCancelEditStage.setOnClickListener(new CancelClickListener(MODIFIER_HOME));

            addNomenclaturesLayoutBottom.setVisibility(View.VISIBLE);
        }

    }

    private class DeleteStageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Setup stp = new Setup();
                    stp.hideKeyboard();

                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
                            StageData stageData = new StageData(getApplicationContext());

                            stageData.deleteStage(currentStage);

                            currentStage = null;

                            switchToView(MODIFIER_HOME);

                            nomenclatures.clear();
                            nomenclatures = db.getNomenclatures();
                            adapter.refreshNomenclatures(nomenclatures);

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Nomenclatures.this, R.style.AlertDialog);
            builder.setTitle(getString(R.string.nomenclatures_delete_title));
            builder.setMessage(getString(R.string.nomenclatures_delete_message) + currentStage.getName() + "?");
            builder.setPositiveButton(getString(R.string.nomenclatures_delete_positive), dialogClickListener);
            builder.setNegativeButton(getString(R.string.nomenclatures_delete_negative), dialogClickListener);

            builder.show();
        }
    }

    private class DeletePhaseClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Setup stp = new Setup();
            stp.hideKeyboard();

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:

                            SQLiteHelper db = new SQLiteHelper(getApplicationContext());
                            PhaseData phaseData = new PhaseData(getApplicationContext());
                            phaseData.deletePhase(currentPhase);

                            currentPhase = null;

                            switchToView(MODIFIER_HOME);

                            nomenclatures.clear();
                            nomenclatures = db.getNomenclatures();
                            adapter.refreshNomenclatures(nomenclatures);

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Nomenclatures.this, R.style.AlertDialog);
            builder.setTitle("Confirmare");
            builder.setMessage("Sunteți sigur(ă) că doriți să ștergeti faza " + currentPhase.getName() + "?");
            builder.setPositiveButton("Da", dialogClickListener);
            builder.setNegativeButton("Nu", dialogClickListener);
            builder.show();

        }
    }

    private class CancelClickListener implements View.OnClickListener {

        private final int modifier;

        public CancelClickListener(int modifier) {
            Setup stp = new Setup();
            stp.hideKeyboard();
            this.modifier = modifier;
        }

        @Override
        public void onClick(View v) {

            Setup stp = new Setup();
            stp.hideKeyboard();

            AlertDialog.Builder builder = new AlertDialog.Builder(Nomenclatures.this, R.style.AlertDialog);
            builder.setTitle("Confirmare");
            builder.setPositiveButton("Da", new CancelDialogClickListener(modifier));
            builder.setNegativeButton("Nu", new CancelDialogClickListener(modifier));
            builder.setMessage("Sunteți sigur(ă) că doriți să anulați modificările?");
            builder.show();


        }
    }

    private class CancelDialogClickListener implements DialogInterface.OnClickListener {

        private int modifier;

        public CancelDialogClickListener(int modifier) {
            super();
            this.modifier = modifier;


            Setup stp = new Setup();
            stp.hideKeyboard();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            Setup stp = new Setup();
            stp.hideKeyboard();

            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    switchToView(modifier);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;

                default:
                    break;
            }
        }
    }

    private class StageSpinnerAdapter extends ArrayAdapter<Stage> {

        public StageSpinnerAdapter(Context context, int resource, List<Stage> objects) {
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

    private void switchToView(int modifier) {
        switch (modifier) {
            case MODIFIER_PHASE:
                showPhaseDetails();
                break;

            case MODIFIER_STAGE:
                showStageDetails();
                break;

            case MODIFIER_HOME:
                RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
                cardContainer.removeAllViews();
                addNomenclaturesLayout.setVisibility(View.VISIBLE);
                addNomenclaturesLayoutBottom.setVisibility(View.GONE);

            default:
                break;
        }
    }

    private void showStageDetails() {
        RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View stageCard = cardContainer.findViewById(R.id.coordinator_stage_card);

        if (stageCard == null) {
            cardContainer.removeAllViews();
            inflater.inflate(R.layout.stage_details_card, cardContainer, true);

        }

        TextView nameText = (TextView) cardContainer.findViewById(R.id.label_stage_name);
        TextView hierarchyText = (TextView) cardContainer.findViewById(R.id.value_stage_hierarchy);
        TextView statusText = (TextView) cardContainer.findViewById(R.id.value_stage_status);

        FloatingActionButton stageFab = (FloatingActionButton) cardContainer.findViewById(R.id.fab_editStage);

        FloatingActionButton deleteStageFab = (FloatingActionButton) cardContainer.findViewById(R.id.fab_deleteStage);

        if(user.getUserType() == User.TYPE_DVA) {
            addNomenclaturesLayout.setVisibility(View.GONE);
            addNomenclaturesLayoutBottom.setVisibility(View.VISIBLE);

            stageFab.setOnClickListener(new EditStageClickListener());
            stageFab.setVisibility(View.VISIBLE);

            deleteStageFab.setOnClickListener(new DeleteStageClickListener());
            deleteStageFab.setVisibility(View.VISIBLE);

        }

        nameText.setText(currentStage.getName());
        hierarchyText.setText(Integer.toString(currentStage.getHierarchy()));

        switch (currentStage.getStatus()) {
            case 0:
                statusText.setText("Inactivă");
                break;

            case 1:
                statusText.setText("Activă");
                break;

            default:
                statusText.setText("Status invalid");
        }

    }

    private void showPhaseDetails() {
        RelativeLayout cardContainer = (RelativeLayout) findViewById(R.id.relativeLayout_infoCardContainer);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View phaseCard = cardContainer.findViewById(R.id.coordinator_phase_card);

        if (phaseCard == null) {
            cardContainer.removeAllViews();
            inflater.inflate(R.layout.phase_details_card, cardContainer, true);
        }

        FloatingActionButton phaseFab = (FloatingActionButton) cardContainer.findViewById(R.id.fab_editPhase);
        FloatingActionButton deletePhaseFab = (FloatingActionButton) cardContainer.findViewById(R.id.fab_deletePhase);

        if(user.getUserType() == User.TYPE_DVA) {
            addNomenclaturesLayoutBottom.setVisibility(View.VISIBLE);
            addNomenclaturesLayout.setVisibility(View.GONE);

            phaseFab.setOnClickListener(new EditPhaseClickListener());
            phaseFab.setVisibility(View.VISIBLE);

            deletePhaseFab.setOnClickListener(new DeletePhaseClickListener());
            deletePhaseFab.setVisibility(View.VISIBLE);
        }

        TextView nameText = (TextView) cardContainer.findViewById(R.id.label_phase_name);
        TextView hierarchyText = (TextView) cardContainer.findViewById(R.id.value_phase_hierarchy);
        TextView stageText = (TextView) cardContainer.findViewById(R.id.value_phase_stageParent);
        TextView daysText = (TextView) cardContainer.findViewById(R.id.value_phase_days);
        TextView statusText = (TextView) cardContainer.findViewById(R.id.value_phase_status);


        nameText.setText(currentPhase.getName());
        hierarchyText.setText(Integer.toString(currentPhase.getHierarchy()));
        stageText.setText(currentStage.getName());
        daysText.setText(Integer.toString(currentPhase.getDays()));

        switch (currentPhase.getStatus()) {
            case 0:
                statusText.setText("Inactivă");
                break;

            case 1:
                statusText.setText("Activă");
                break;

            default:
                statusText.setText("Status invalid");
        }
    }
//    private class PagerItemTab {
//
//        String title;
//        int indicatorColor;
//
//        public PagerItemTab(String title, int indicatorColor) {
//            this.title = title;
//            this.indicatorColor = indicatorColor;
//        }
//
//        public String getTitle() {
//            return title;
//        }
//
//        public void setTitle(String title) {
//            this.title = title;
//        }
//
//        public int getIndicatorColor() {
//            return indicatorColor;
//        }
//
//        public void setIndicatorColor(int indicatorColor) {
//            this.indicatorColor = indicatorColor;
//        }
//    }
}
