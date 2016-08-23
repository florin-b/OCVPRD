package com.arabesque.obiectivecva.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumCategorieObiectiv {

	P("P", 0), P_M("P + M", 1), P_1_M("P + 1 + M", 2), P_2_M("P + 2 + M", 3), HALA("Hala", 4);

	private String nume;
	int cod;

	EnumCategorieObiectiv(String nume, int cod) {
		this.nume = nume;
		this.cod = cod;
	}

	public String getNume() {
		return nume;
	}

	public int getCod() {
		return cod;
	}

	public static List<String> getCategoriesNames() {
		List<String> listValues = new ArrayList<String>();

		for (EnumCategorieObiectiv enumC : EnumCategorieObiectiv.values())
			listValues.add(enumC.nume);

		return listValues;
	}

	public static int getCodeCategory(String categoryName) {

		for (EnumCategorieObiectiv enumCat : EnumCategorieObiectiv.values())
			if (enumCat.nume == categoryName)
				return enumCat.cod;

		return -1;
	}

}
