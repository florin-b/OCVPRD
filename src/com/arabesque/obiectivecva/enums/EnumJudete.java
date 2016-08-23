package com.arabesque.obiectivecva.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumJudete {

	ALBA("ALBA", "01"), ARAD("ARAD", "02"), ARGES("ARGES", "03"), BACAU("BACAU", "04"), BIHOR("BIHOR", "05"), BISTRITA("BISTRITA-NASAUD", "06"), BOTOSANI(
			"BOTOSANI", "07"), BRAILA("BRAILA", "09"), BRASOV("BRASOV", "08"), BUCURESTI("BUCURESTI", "40"), BUZAU("BUZAU", "10"), CALARASI("CALARASI",
					"51"), CARAS("CARAS-SEVERIN", "11"), CLUJ("CLUJ", "12"), CONSTANTA("CONSTANTA", "13"), COVASNA("COVASNA", "14"), DAMBOVITA("DAMBOVITA",
							"15"), DOLJ("DOLJ", "16"), GALATI("GALATI", "17"), GIURGIU("GIURGIU", "52"), GORJ("GORJ", "18"), HARGHITA("HARGHITA",
									"19"), HUNEDOARA("HUNEDOARA", "20"), IALOMITA("IALOMITA", "21"), IASI("IASI", "22"), ILFOV("ILFOV", "23"), MARAMURES(
											"MARAMURES", "24"), MEHEDINTI("MEHEDINTI", "25"), MURES("MURES", "26"), NEAMT("NEAMT", "27"), OLT("OLT",
													"28"), PRAHOVA("PRAHOVA", "29"), SALAJ("SALAJ", "31"), SATU_MARE("SATU-MARE", "30"), SIBIU("SIBIU",
															"32"), SUCEAVA("SUCEAVA", "33"), TELEORMAN("TELEORMAN", "34"), TIMIS("TIMIS", "35"), TULCEA(
																	"TULCEA", "36"), VALCEA("VALCEA", "38"), VASLUI("VASLUI", "37"), VRANCEA("VRANCEA", "39");

	private String nume;
	private String cod;

	EnumJudete(String nume, String cod) {
		this.nume = nume;
		this.cod = cod;
	}

	public String toString() {
		return nume;
	}

	public String getCod() {
		return cod;
	}

	public static List<String> getRegionNames() {
		List<String> listValues = new ArrayList<String>();

		for (EnumJudete enumJ : EnumJudete.values())
			listValues.add(enumJ.nume);

		return listValues;
	}

	public static String getCodJudet(String numeJudet) {
		for (EnumJudete enumJ : EnumJudete.values()) {
			if (enumJ.nume.equals(numeJudet))
				return enumJ.cod;

		}
		return "";
	}

	public static String getNumeJudet(int codJudet) {
		for (EnumJudete enumJ : EnumJudete.values()) {
			if (Integer.valueOf(enumJ.cod) == codJudet)
				return enumJ.nume;

		}
		return "";
	}

}
