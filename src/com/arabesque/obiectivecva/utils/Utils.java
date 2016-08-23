package com.arabesque.obiectivecva.utils;

import java.text.Normalizer;
import java.util.Random;

public class Utils {
	public static String flattenToAscii(String string) {
		StringBuilder sb = new StringBuilder(string.length());
		string = Normalizer.normalize(string, Normalizer.Form.NFD);
		for (char c : string.toCharArray()) {
			if (c <= '\u007F')
				sb.append(c);
		}
		return sb.toString();
	}

	public static int getIntRandom() {
		final int min = 10;
		final int max = 10000;
		Random rnd = new Random();
		final int random = rnd.nextInt((max - min) + 1) + min;

		return random;
	}

}
