package com.arabesque.obiectivecva;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class Utils {

	public static String getDepart(String numeDepart) {
		String dpt = "00";

		if (numeDepart.equals("CHIM"))
			dpt = "07";

		if (numeDepart.equals("DIVE"))
			dpt = "10";

		if (numeDepart.equals("ELEC"))
			dpt = "05";

		if (numeDepart.equals("FERO"))
			dpt = "02";

		if (numeDepart.equals("GIPS"))
			dpt = "06";

		if (numeDepart.equals("INST"))
			dpt = "08";

		if (numeDepart.equals("LEMN"))
			dpt = "01";

		if (numeDepart.equals("MATE"))
			dpt = "04";

		if (numeDepart.equals("PARC"))
			dpt = "03";

		if (numeDepart.equals("CHIM"))
			dpt = "07";

		if (numeDepart.equals("HIDR"))
			dpt = "09";

		if (numeDepart.equals("LEFA"))
			dpt = "02";

		return dpt;

	}

	public static String getFiliala(String numeFiliala) {
		String fl = "NN10";

		if (numeFiliala.equals("BACAU"))
			fl = "BC10";

		if (numeFiliala.equals("GALATI"))
			fl = "GL10";

		if (numeFiliala.equals("PITESTI"))
			fl = "AG10";

		if (numeFiliala.equals("TIMISOARA"))
			fl = "TM10";

		if (numeFiliala.equals("ORADEA"))
			fl = "BH10";

		if (numeFiliala.equals("FOCSANI"))
			fl = "VN10";

		if (numeFiliala.equals("GLINA"))
			fl = "BU10";

		if (numeFiliala.equals("ANDRONACHE"))
			fl = "BU13";

		if (numeFiliala.equals("OTOPENI"))
			fl = "BU12";

		if (numeFiliala.equals("CLUJ"))
			fl = "CJ10";

		if (numeFiliala.equals("BAIA"))
			fl = "MM10";

		if (numeFiliala.equals("MILITARI"))
			fl = "BU11";

		if (numeFiliala.equals("CONSTANTA"))
			fl = "CT10";

		if (numeFiliala.equals("BRASOV"))
			fl = "BV10";

		if (numeFiliala.equals("PLOIESTI"))
			fl = "PH10";

		if (numeFiliala.equals("PIATRA"))
			fl = "NT10";

		if (numeFiliala.equals("MURES"))
			fl = "MS10";

		if (numeFiliala.equals("IASI"))
			fl = "IS10";

		if (numeFiliala.equals("CRAIOVA"))
			fl = "DJ10";

		return fl;

	}

	public static String getTipUser(String tipUser) {
		String tipAcces = "NN";

		// AGENTI
		if (UserInfo.getInstance().getTipAcces().equals("9")) {
			tipAcces = "AV";
		}

		// SEFI DE DEPARTAMENT
		if (UserInfo.getInstance().getTipAcces().equals("10")) {
			tipAcces = "SD";
		}

		// DIRECTORI DE VANZARI, DEPARTAMENT
		if (UserInfo.getInstance().getTipAcces().equals("12") || UserInfo.getInstance().getTipAcces().equals("14")) {
			tipAcces = "DV";
		}

		// KEY ACCOUNTI
		if (UserInfo.getInstance().getTipAcces().equals("27")) {
			tipAcces = "KA";
		}

		// DIRECTOR KA
		if (UserInfo.getInstance().getTipAcces().equals("35")) {
			tipAcces = "DK";
		}

		// CONSILIERI
		if (UserInfo.getInstance().getTipAcces().equals("17")) {
			tipAcces = "CV";
		}

		// SEFI DE MAGAZIN
		if (UserInfo.getInstance().getTipAcces().equals("18")) {
			tipAcces = "SM";
		}

		return tipAcces;
	}

	public static String getCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

		return dateFormat.format(calendar.getTime());

	}

	public static String flattenToAscii(String string) {
		StringBuilder sb = new StringBuilder(string.length());
		string = Normalizer.normalize(string, Normalizer.Form.NFD);
		for (char c : string.toCharArray()) {
			if (c <= '\u007F')
				sb.append(c);
		}
		return sb.toString();
	}

	public static String serializeUserInfo() {

		JSONObject jsonUser = new JSONObject();
		JSONArray jsonArrayFiliale = new JSONArray();

		try {
			jsonUser.put("nume", UserInfo.getInstance().getNume());
			jsonUser.put("filiala", UserInfo.getInstance().getFiliala());
			jsonUser.put("cod", UserInfo.getInstance().getCod());
			jsonUser.put("numeDepart", UserInfo.getInstance().getNumeDepart());
			jsonUser.put("codDepart", UserInfo.getInstance().getCodDepart());
			jsonUser.put("unitLog", UserInfo.getInstance().getUnitLog());
			jsonUser.put("initUnitLog", UserInfo.getInstance().getInitUnitLog());
			jsonUser.put("tipAcces", UserInfo.getInstance().getTipAcces());
			jsonUser.put("parentScreen", UserInfo.getInstance().getParentScreen());
			jsonUser.put("filialeDV", UserInfo.getInstance().getFilialeDV());
			jsonUser.put("altaFiliala", UserInfo.getInstance().isAltaFiliala());
			jsonUser.put("tipUser", UserInfo.getInstance().getTipUser());
			jsonUser.put("departExtra", UserInfo.getInstance().getDepartExtra());
			jsonUser.put("tipUserSap", UserInfo.getInstance().getTipUserSap());

			for (String filiala : UserInfo.getInstance().getExtraFiliale()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("filiala", filiala);
				jsonArrayFiliale.put(jsonObject);
			}

			jsonUser.put("extraFiliale", jsonArrayFiliale.toString());
			jsonUser.put("comisionCV", UserInfo.getInstance().getComisionCV());
			jsonUser.put("coefCorectie", UserInfo.getInstance().getCoefCorectie());
			jsonUser.put("userSite", UserInfo.getInstance().getUserSite());
			jsonUser.put("userWood", UserInfo.getInstance().isUserWood());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonUser.toString();

	}

	public static void deserializeUserInfo(String userInfoSer, Context context) {

		try {
			List<String> listExtraFiliale = new ArrayList<String>();
			JSONObject jsonObject = new JSONObject(userInfoSer);

			if (jsonObject instanceof JSONObject) {
				UserInfo.getInstance().setNume(jsonObject.getString("nume"));
				UserInfo.getInstance().setFiliala(jsonObject.getString("filiala"));
				UserInfo.getInstance().setCod(jsonObject.getString("cod"));
				UserInfo.getInstance().setNumeDepart(jsonObject.getString("numeDepart"));
				UserInfo.getInstance().setCodDepart(jsonObject.getString("codDepart"));
				UserInfo.getInstance().setUnitLog(jsonObject.getString("unitLog"));
				UserInfo.getInstance().setInitUnitLog(jsonObject.getString("initUnitLog"));
				UserInfo.getInstance().setTipAcces(jsonObject.getString("tipAcces"));
				UserInfo.getInstance().setParentScreen(jsonObject.getString("parentScreen"));
				UserInfo.getInstance().setFilialeDV(jsonObject.getString("filialeDV"));
				UserInfo.getInstance().setAltaFiliala(Boolean.valueOf(jsonObject.getString("altaFiliala")));
				UserInfo.getInstance().setTipUser(jsonObject.getString("tipUser"));
				UserInfo.getInstance().setDepartExtra(jsonObject.getString("departExtra"));
				UserInfo.getInstance().setTipUserSap(jsonObject.getString("tipUserSap"));
				UserInfo.getInstance().setComisionCV(Double.valueOf(jsonObject.getString("comisionCV")));
				UserInfo.getInstance().setCoefCorectie(jsonObject.getString("coefCorectie"));
				UserInfo.getInstance().setUserSite(jsonObject.getString("userSite"));
				UserInfo.getInstance().setUserWood(Boolean.valueOf(jsonObject.getString("userWood")));

				Object json = new JSONTokener(jsonObject.getString("extraFiliale")).nextValue();

				if (json instanceof JSONArray) {
					JSONArray jsonArray = new JSONArray(jsonObject.getString("extraFiliale"));

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject compObject = jsonArray.getJSONObject(i);
						listExtraFiliale.add(compObject.getString("filiala"));

					}
				}

				UserInfo.getInstance().setExtraFiliale(listExtraFiliale);

			}

		} catch (JSONException e) {
			Toast.makeText(context, "JSON: " + userInfoSer + " " + e.toString(), Toast.LENGTH_SHORT).show();
		}

	}

	public static boolean isPackageInstalled(String packagename, Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

}
