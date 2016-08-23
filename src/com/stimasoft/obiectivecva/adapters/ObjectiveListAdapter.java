package com.stimasoft.obiectivecva.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.arabesque.obiectivecva.enums.EnumJudete;
import com.stimasoft.obiectivecva.AddEditObjective;
import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.ObjectiveLite;
import com.stimasoft.obiectivecva.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Feeds information to the objectives list
 */
public class ObjectiveListAdapter extends RecyclerView.Adapter<ObjectiveListAdapter.ViewHolder> {
	private ArrayList<ObjectiveLite> objectives;
	private Context context;
	private int mode;

	public ObjectiveListAdapter(Context context, ArrayList<ObjectiveLite> objectives, int mode) {
		this.objectives = objectives;
		this.context = context;
		this.mode = mode;

	}

	@Override
	public ObjectiveListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// create a new view
		View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.objective_list_item, parent, false);

		// create ViewHolder

		return new ViewHolder(itemLayoutView, context, new ViewHolder.ObjectiveClickInterface() {
			@Override
			public void onListObjectiveClick(View caller, int objectiveId, String cvaCode) {
				Intent i = new Intent(caller.getContext(), AddEditObjective.class);
				i.putExtra(Constants.OBJECTIVES_MODE, mode);
				i.putExtra(Constants.KEY_ID, objectiveId);
				i.putExtra(Constants.KEY_PURPOSE, Constants.VALUE_EDIT);
				i.putExtra(Constants.CVA_CODE, cvaCode);
				((Activity) context).startActivityForResult(i, Constants.CODE_EDIT_FROM_LIST);
			}
		});
	}

	/*
	 * ********************************************* If the Phase of an Active
	 * Object has expired Add a warning near the expired date
	 * ********************************************* Created By: Alin,
	 * Publisher: Alin; Created: 04.06.2016
	 */

	public boolean isPhaseExpired(int position)
	{
		//Added date verification for Objective Phase
		//Author: Alin
		Date formDate = null;
		Date Phase = null;
				
		Calendar c = Calendar.getInstance();		 
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.USER_DATE_FORMAT, Locale.US);
		String formatDate = sdf.format(c.getTime());
		String PhaseDate = objectives.get(position).getExpPhaseDateString();
		try {
			formDate = sdf.parse(formatDate);
			Phase = sdf.parse(PhaseDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(formDate.compareTo(Phase) > 0)
		{
			return true;
		}
		return false;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {

		// - get data from your itemsData at this position
		// - replace the contents of the view with that itemsData
		viewHolder.objective = objectives.get(position);
		viewHolder.objectiveNameText.setText(objectives.get(position).getName());
		viewHolder.objectivePhaseText.setText(objectives.get(position).getPhaseName());
		viewHolder.objectiveExpPhaseText.setText(objectives.get(position).getExpPhaseDateString());
		viewHolder.objectiveBeneficiary.setText(objectives.get(position).getBeneficiaryName());
		viewHolder.objectiveConstructorType.setText(getConstructor(objectives.get(position).getConstructorName()));
		
		//Added condition if Obj. Phase is Expired, make a warning sign visible
		//Author: Alin
		if(isPhaseExpired(position))
		{
			viewHolder.objectiveExpired.setVisibility(View.VISIBLE);
		}
		else
		{
			viewHolder.objectiveExpired.setVisibility(View.INVISIBLE);
		}
		viewHolder.objectiveCity.setText(objectives.get(position).getCity());
		viewHolder.objectiveRegion.setText(EnumJudete.getNumeJudet(objectives.get(position).getRegionCode()));

	}

	private String getLocalitate(String adresa) {
		if (adresa.contains(",")) {
			return adresa.split(",")[0];
		} else {
			return adresa;
		}
	}

	private String getConstructor(String constructor) {
		return constructor.trim().length() > 0 ? constructor : "Regie proprie";
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		public ObjectiveLite objective;
		public Context context;
		public ObjectiveClickInterface clickInterface;

		public TextView objectiveNameText;
		public TextView objectivePhaseText;
		public TextView objectiveExpPhaseText;
		public TextView objectiveBeneficiary;
		public TextView objectiveConstructorType;
		public TextView objectiveCity;
		public TextView objectiveRegion;
		public ImageView objectiveExpired; //Added ImageView, Author: Alin

		public ViewHolder(View itemLayoutView, Context context, ObjectiveClickInterface clickInterface) {
			super(itemLayoutView);
			this.context = context;
			this.clickInterface = clickInterface;

			itemLayoutView.setOnClickListener(this);

			objectiveNameText = (TextView) itemLayoutView.findViewById(R.id.textView_objective_name);
			objectivePhaseText = (TextView) itemLayoutView.findViewById(R.id.textView_objective_phase);
			objectiveExpPhaseText = (TextView) itemLayoutView.findViewById(R.id.textView_objective_phaseExp);
			objectiveBeneficiary = (TextView) itemLayoutView.findViewById(R.id.textView_objective_beneficiary);
			objectiveConstructorType = (TextView) itemLayoutView.findViewById(R.id.textView_objective_constructor_type);

			objectiveCity = (TextView) itemLayoutView.findViewById(R.id.textView_objective_city);
			objectiveRegion = (TextView) itemLayoutView.findViewById(R.id.textView_objective_region);
			objectiveExpired = (ImageView) itemLayoutView.findViewById(R.id.expired_warning);//Added ImageView when Objective Phase is expired, Author: Alin
		}

		@Override
		public void onClick(View v) {
			clickInterface.onListObjectiveClick(v, objective.getId(), objective.getCvaCode());
		}

		public interface ObjectiveClickInterface {
			void onListObjectiveClick(View caller, int objectiveId, String cvaCode);
		}

	}

	@Override
	public int getItemCount() {
		return objectives.size();
	}

	public void addObjectives(ArrayList<ObjectiveLite> newObjectives) {

		this.objectives.addAll(newObjectives);
		notifyDataSetChanged();

	}
}
