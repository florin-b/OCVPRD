package com.arabesque.obiectivecva.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumTipExecutant {

	REGIE_PROPRIE("Regie proprie", 0), TERT("Tert", 1);

	private String nume;
	private int cod;

	EnumTipExecutant(String nume, int cod) {
		this.nume = nume;
		this.cod = cod;
	}

	public String toString() {
		return nume;
	}

	public String getNume() {
		return nume;
	}

	public int getCod() {
		return cod;
	}

	public static String getNumeTip(int codTip) {

		for (EnumTipExecutant enumTip : EnumTipExecutant.values())
			if (enumTip.cod == codTip)
				return enumTip.nume;

		return "";
	}

	public static int getCodTip(String numeTip) {

		for (EnumTipExecutant enumTip : EnumTipExecutant.values())
			if (enumTip.nume == numeTip)
				return enumTip.cod;

		return -1;
	}

	public static List<String> getTipExecNames() {
		List<String> listValues = new ArrayList<String>();

		for (EnumTipExecutant enumS : EnumTipExecutant.values())
			listValues.add(enumS.nume);

		return listValues;
	}

}
