package com.arabesque.obiectivecva;

import android.content.Context;
import android.widget.Toast;

public class Logon {

	public boolean validateLogin(String result, Context context) {

		if (!result.equals("-1") && result.length() > 0) {
			String[] token = result.split("#");

			if (token[0].equals("0")) {
				Toast.makeText(context, "Cont inexistent!", Toast.LENGTH_SHORT).show();
				return false;
			}
			if (token[0].equals("1")) {
				Toast.makeText(context, "Cont blocat 60 de minute.", Toast.LENGTH_SHORT).show();
				return false;
			}
			if (token[0].equals("2")) {
				Toast.makeText(context, "Parola incorecta!", Toast.LENGTH_SHORT).show();
				return false;
			}
			if (token[0].equals("3")) {

				if (token[5].equals("8") || token[5].equals("9") || token[5].equals("10") || token[5].equals("14") || token[5].equals("12")
						|| token[5].equals("27") || token[5].equals("35") || token[5].equals("17") || token[5].equals("18")) {

					UserInfo uInfo = UserInfo.getInstance();

					String tempAgCod = token[4].toString();

					if (tempAgCod.equalsIgnoreCase("-1")) {
						Toast.makeText(context, "Utilizator nedefinit!", Toast.LENGTH_SHORT).show();
						return false;
					}

					for (int i = 0; i < 8 - token[4].length(); i++) {
						tempAgCod = "0" + tempAgCod;
					}

					// coeficienti consilieri si sm
					if (token[5].equals("17") || token[5].equals("18") || token[10].equals("KA3")) {
						String[] coeficientiCV = token[6].split("!");
						uInfo.setComisionCV(Double.valueOf(coeficientiCV[0]));
						uInfo.setCoefCorectie(coeficientiCV[1]);

					}

					uInfo.setNume(token[3].toString());
					uInfo.setFiliala(token[2].toString());
					uInfo.setCod(tempAgCod);
					uInfo.setNumeDepart(token[1].toString());
					uInfo.setCodDepart(Utils.getDepart(token[1].toString()));
					uInfo.setUnitLog(Utils.getFiliala(token[2].toString()));
					uInfo.setInitUnitLog(Utils.getFiliala(token[2].toString()));
					uInfo.setTipAcces(token[5].toString());
					uInfo.setTipUser(Utils.getTipUser(token[5].toString()));
					uInfo.setParentScreen("logon");
					uInfo.setFilialeDV(token[9]);
					uInfo.setAltaFiliala(false);
					uInfo.setUserSite(token[8]);
					uInfo.setDepartExtra(token[7]);
					uInfo.setTipUserSap(token[10]);
					uInfo.setExtraFiliale(token[11]);

					if (uInfo.getTipAcces().equals("27") || uInfo.getTipAcces().equals("35") || uInfo.getTipAcces().equals("17")
							|| uInfo.getTipAcces().equals("18")) {

						uInfo.setCodDepart("01");
						uInfo.setNumeDepart("LEMN");
					}

					return true;

				} else {
					Toast.makeText(context, "Acces interzis!", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			if (token[0].equals("4")) {
				Toast.makeText(context, "Cont inactiv!", Toast.LENGTH_SHORT).show();
				return false;

			}
		} else {
			Toast.makeText(context, "Autentificare esuata!", Toast.LENGTH_SHORT).show();
			return false;
		}

		return false;

	}

}
