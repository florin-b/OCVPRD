package com.arabesque.obiectivecva.beans;

import java.util.ArrayList;
import java.util.List;

import com.arabesque.obiectivecva.ObjectivePhase;
import com.stimasoft.obiectivecva.models.db_classes.Beneficiary;
import com.stimasoft.obiectivecva.models.db_classes.Objective;

public class BeanDateTabele {

	private ArrayList<Objective> listObiective;
	private ArrayList<Beneficiary> listBeneficiari;
	private ArrayList<ObjectivePhase> listStadii;

	public ArrayList<Objective> getListObiective() {
		return listObiective;
	}

	public void setListObiective(ArrayList<Objective> listObiective) {
		this.listObiective = listObiective;
	}

	public ArrayList<Beneficiary> getListBeneficiari() {
		return listBeneficiari;
	}

	public void setListBeneficiari(ArrayList<Beneficiary> listBeneficiari) {
		this.listBeneficiari = listBeneficiari;
	}

	public ArrayList<ObjectivePhase> getListStadii() {
		return listStadii;
	}

	public void setListStadii(ArrayList<ObjectivePhase> listStadii) {
		this.listStadii = listStadii;
	}

}
