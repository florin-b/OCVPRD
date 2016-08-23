package com.arabesque.obiectivecva.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumStadiuObiectiv {

	ACTIV("Activ", 0), INACTIV("Inactiv", 1), PROSPECT("Prospect", 2);

	private String numeStadiu;
	private int codStadiu;

	EnumStadiuObiectiv(String numeStadiu, int codStadiu) {
		this.numeStadiu = numeStadiu;
		this.codStadiu = codStadiu;
	}

	public String getNumeStadiu() {
		return numeStadiu;
	}

	public int getCodStadiu() {
		return codStadiu;
	}

	public static String getNumeStadiu(int codStadiu) {

		for (EnumStadiuObiectiv enumSt : EnumStadiuObiectiv.values())
			if (enumSt.codStadiu == codStadiu)
				return enumSt.numeStadiu;

		return "";
	}

	public static int getCodStadiu(String numeStadiu) {

		for (EnumStadiuObiectiv enumSt : EnumStadiuObiectiv.values())
			if (enumSt.numeStadiu == numeStadiu)
				return enumSt.codStadiu;

		return -1;
	}

	public static List<String> getStatusNames() {
		List<String> listValues = new ArrayList<String>();

		for (EnumStadiuObiectiv enumS : EnumStadiuObiectiv.values())
			listValues.add(enumS.numeStadiu);

		return listValues;
	}

}
