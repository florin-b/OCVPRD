package com.arabesque.obiectivecva.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.arabesque.obiectivecva.ObjectivePhase;
import com.arabesque.obiectivecva.beans.BeanDateTabele;
import com.arabesque.obiectivecva.listeners.AsyncTaskListener;
import com.arabesque.obiectivecva.listeners.AsyncTaskWSCall;
import com.stimasoft.obiectivecva.models.db_classes.Beneficiary;
import com.stimasoft.obiectivecva.models.db_classes.Objective;
import com.stimasoft.obiectivecva.utils.SQLiteHelper;

import android.content.Context;
import android.widget.Toast;

public class OperatiiTabele implements AsyncTaskListener {

	private Context context;

	public void salveazaTabelaObiective(Context context) {

		this.context = context;
		SQLiteHelper sqlHelper = new SQLiteHelper(context);

		Iterator<Objective> iterator = sqlHelper.getAllObjectives().iterator();

		JSONObject jsonObj = null;
		JSONArray jsonArrayObj = new JSONArray();
		try {

			Objective objective = null;
			while (iterator.hasNext()) {
				objective = iterator.next();

				
				
				
				jsonObj = new JSONObject();
				jsonObj.put("id", objective.getId());
				jsonObj.put("typeId", objective.getTypeId());
				jsonObj.put("cvaCode", objective.getCvaCode());
				jsonObj.put("regionID", objective.getRegionID());
				jsonObj.put("name", objective.getName());
				jsonObj.put("creationDate", objective.getCreationDateString());
				jsonObj.put("beneficiaryId", objective.getBeneficiaryId());
				jsonObj.put("beneficiaryType", objective.getBeneficiaryType());
				jsonObj.put("authorizationStart", objective.getAuthStartDateString());
				jsonObj.put("authorizationEnd", objective.getAuthEndDateString());
				jsonObj.put("estimationValue", String.format("%.2f", objective.getEstimationValue()));
				jsonObj.put("address", objective.getAddress());
				jsonObj.put("zip", objective.getZip());
				jsonObj.put("gps", objective.getGps());
				jsonObj.put("stageId", objective.getStageId());
				jsonObj.put("phaseId", objective.getPhaseId());
				jsonObj.put("expirationPhase", objective.getExpPhaseDateString());
				jsonObj.put("status", objective.getStatus());

				jsonObj.put("statusId", objective.getStatusId());
				jsonObj.put("categoryId", objective.getCategoryId());

				jsonObj.put("numeExecutant", objective.getNumeExecutant());
				jsonObj.put("cuiExecutant", objective.getCuiExecutant());
				jsonObj.put("nrcExecutant", objective.getNrcExecutant());

				// Meserias field Alin
				jsonObj.put("numeMeserias", objective.getNumeMeserias());
				jsonObj.put("prenMeserias", objective.getPrenMeserias());
				jsonObj.put("telMeserias", objective.getTelMeserias());
				// End Meserias field Alin

				jsonObj.put("telBenef", objective.getTelBenef());

				jsonObj.put("filiala", objective.getFiliala());

				jsonArrayObj.put(jsonObj);
			}

			JSONArray jsonArrayBenef = new JSONArray();
			Iterator<Beneficiary> iteratorBen = sqlHelper.getAllBeneficiaries().iterator();
			Beneficiary beneficiary = null;

			while (iteratorBen.hasNext()) {
				beneficiary = iteratorBen.next();

				jsonObj = new JSONObject();
				jsonObj.put("id", beneficiary.getId());
				jsonObj.put("cui", beneficiary.getCui());
				jsonObj.put("region_id", beneficiary.getRegionID());
				jsonObj.put("name", beneficiary.getName());
				jsonObj.put("type", beneficiary.getType());
				jsonObj.put("nr_rc", beneficiary.getNr_rc());
				jsonObj.put("cnp", beneficiary.getCnp());
				jsonObj.put("status", beneficiary.getStatus());

				jsonArrayBenef.put(jsonObj);
			}

			Iterator<ObjectivePhase> iteratorPhases = sqlHelper.getAllObjectivesPhases().iterator();

			JSONArray jsonArrayPhases = new JSONArray();
			ObjectivePhase objPhase = null;

			while (iteratorPhases.hasNext()) {
				objPhase = iteratorPhases.next();
				jsonObj = new JSONObject();

				jsonObj.put("id", objPhase.getId());
				jsonObj.put("phase_id", objPhase.getPhase_id());
				jsonObj.put("objective_id", objPhase.getObjective_id());
				jsonObj.put("days_nr", objPhase.getDays());
				jsonObj.put("phase_start", objPhase.getPhase_start());
				jsonObj.put("phase_end", objPhase.getPhase_end());

				jsonArrayPhases.put(jsonObj);

			}

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("dateObiective", jsonArrayObj.toString());
			params.put("beneficiari", jsonArrayBenef.toString());
			params.put("stadii", jsonArrayPhases.toString());

			AsyncTaskWSCall call = new AsyncTaskWSCall("salveazaObiectiveCVA", params, (AsyncTaskListener) this, context);
			call.getCallResultsFromFragment();

		} catch (Exception ex) {
			Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
		}

	}

	public BeanDateTabele deserializeObiective(String objectiveData) {

		BeanDateTabele dateTabele = new BeanDateTabele();

		ArrayList<Objective> listObiective = new ArrayList<Objective>();
		Objective obiectiv = null;

		ArrayList<Beneficiary> listBeneficiari = new ArrayList<Beneficiary>();
		Beneficiary beneficiar = null;

		ArrayList<ObjectivePhase> listStadii = new ArrayList<ObjectivePhase>();
		ObjectivePhase stadiu = null;

		try {

			JSONObject object = new JSONObject((String) objectiveData);

			JSONArray jsonObiective = new JSONArray(object.get("obiective").toString());
			JSONArray jsonBeneficiari = new JSONArray(object.get("beneficiari").toString());
			JSONArray jsonObjStadii = new JSONArray(object.get("stadii").toString());

			if (jsonObiective instanceof JSONArray) {

				JSONArray jsonArray = new JSONArray(object.get("obiective").toString());

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obiectivObject = jsonArray.getJSONObject(i);

					obiectiv = new Objective();
					obiectiv.setId(Integer.valueOf(obiectivObject.getString("id")));
					obiectiv.setTypeId(Integer.valueOf(obiectivObject.getString("typeId")));
					obiectiv.setCvaCode(obiectivObject.getString("cvaCode"));
					obiectiv.setRegionID(Integer.valueOf(obiectivObject.getString("regionID")));
					obiectiv.setName(obiectivObject.getString("name"));
					obiectiv.setCreationDate(getShortDate(obiectivObject.getString("creationDate")));
					obiectiv.setBeneficiaryId(Integer.valueOf(obiectivObject.getString("beneficiaryId")));
					obiectiv.setBeneficiaryType(Integer.valueOf(obiectivObject.getString("beneficiaryType")));
					obiectiv.setAuthorizationStart(getShortDate(obiectivObject.getString("authorizationStart")));
					obiectiv.setAuthorizationEnd(getShortDate(obiectivObject.getString("authorizationEnd")));
					obiectiv.setEstimationValue(Float.valueOf(obiectivObject.getString("estimationValue")));
					obiectiv.setAddress(obiectivObject.getString("address"));
					obiectiv.setZip(Integer.valueOf(obiectivObject.getString("zip")));
					obiectiv.setGps(obiectivObject.getString("gps"));
					obiectiv.setStageId(Integer.valueOf(obiectivObject.getString("stageId")));
					obiectiv.setPhaseId(Integer.valueOf(obiectivObject.getString("phaseId")));
					obiectiv.setExpirationPhase(getShortDate(obiectivObject.getString("expirationPhase")));
					obiectiv.setStatus(Integer.valueOf(obiectivObject.getString("status")));
					obiectiv.setStatusId(Integer.valueOf(obiectivObject.getString("statusId")));
					obiectiv.setCategoryId(Integer.valueOf(obiectivObject.getString("categoryId")));

					obiectiv.setNumeExecutant(obiectivObject.getString("numeExecutant"));
					obiectiv.setCuiExecutant(obiectivObject.getString("cuiExecutant"));
					obiectiv.setNrcExecutant(obiectivObject.getString("nrcExecutant"));

					// Meserias fields Alin
					obiectiv.setNumeMeserias(obiectivObject.getString("numeMeserias"));
					obiectiv.setPrenMeserias(obiectivObject.getString("prenMeserias"));
					obiectiv.setTelMeserias(obiectivObject.getString("telMeserias"));
					// End Meserias fields Alin

					obiectiv.setTelBenef(obiectivObject.getString("telBenef"));

					obiectiv.setFiliala(obiectivObject.getString("filiala"));

					listObiective.add(obiectiv);

				}

			}

			if (jsonBeneficiari instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(object.get("beneficiari").toString());

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject benefObject = jsonArray.getJSONObject(i);

					beneficiar = new Beneficiary();
					beneficiar.setId(Integer.valueOf(benefObject.getString("id")));
					beneficiar.setCui(benefObject.getString("cui"));
					beneficiar.setRegionID(Integer.valueOf(benefObject.getString("region_id")));
					beneficiar.setName(benefObject.getString("name"));
					beneficiar.setType(Integer.valueOf(benefObject.getString("type")));
					beneficiar.setNr_rc(benefObject.getString("nr_rc"));
					beneficiar.setCnp(benefObject.getString("cnp"));
					beneficiar.setStatus(Integer.valueOf(benefObject.getString("status")));
					beneficiar.setCvaCode(benefObject.getString("cvaCode"));
					listBeneficiari.add(beneficiar);

				}

			}

			if (jsonObjStadii instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(object.get("stadii").toString());

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject phaseObject = jsonArray.getJSONObject(i);

					stadiu = new ObjectivePhase();
					stadiu.setId(Integer.parseInt(phaseObject.getString("id")));
					stadiu.setPhase_id(Integer.parseInt(phaseObject.getString("phase_id")));
					stadiu.setObjective_id(Integer.parseInt(phaseObject.getString("objective_id")));
					stadiu.setDays(Integer.parseInt(phaseObject.getString("days_nr")));
					stadiu.setPhase_start(phaseObject.getString("phase_start"));
					stadiu.setPhase_end(phaseObject.getString("phase_end"));
					stadiu.setCvaCode(phaseObject.getString("cvaCode"));
					listStadii.add(stadiu);

				}

			}

		} catch (JSONException e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
		}

		dateTabele.setListObiective(listObiective);
		dateTabele.setListBeneficiari(listBeneficiari);
		dateTabele.setListStadii(listStadii);

		return dateTabele;

	}

	private Calendar getShortDate(String date) {
		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

		try {
			calendar.setTime(sdf.parse(date));
		} catch (ParseException e) {

			e.printStackTrace();
		}

		return calendar;
	}

	public void onTaskComplete(String methodName, Object result) {

	}

}
