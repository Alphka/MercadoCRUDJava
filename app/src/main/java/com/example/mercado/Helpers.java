package com.example.mercado;

import android.util.Log;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class Helpers {
	private static final String TAG = "helpers";
	public static final String DATE_FORMAT = "dd/MM/yyyy";

	public static String getInputString(final String value){
		return Objects.isNull(value) ? "" : value;
	}
	public static String formatPhoneNumber(String phoneNumber){
		phoneNumber = getInputString(phoneNumber);

		if(phoneNumber.isEmpty()) return phoneNumber;

		try{
			int textLength = phoneNumber.length();
			boolean hasCountryCode = textLength > 10;

			int index = hasCountryCode ? 2 : 0;

			StringBuilder formattedPhoneNumber = new StringBuilder();

			if(hasCountryCode){
				formattedPhoneNumber.append(String.format("+%s ", phoneNumber.substring(0, index)));
			}

			formattedPhoneNumber.append(phoneNumber.substring(index, index += textLength == 12 ? 1 : 2));
			formattedPhoneNumber.append(String.format(" %s ", phoneNumber.substring(index++, index)));
			formattedPhoneNumber.append(phoneNumber.substring(index, index += 4));
			formattedPhoneNumber.append("-");
			formattedPhoneNumber.append(phoneNumber.substring(index));

			return formattedPhoneNumber.toString();
		}catch(Exception error){
			Log.e(TAG, Objects.requireNonNull(error.getMessage()));
			return phoneNumber;
		}
	}
	public static String formatCep(String cep){
		cep = getInputString(cep);

		if(cep.isEmpty()) return cep;
		return String.format("%s-%s", cep.substring(0, 5), cep.substring(5));
	}
	public static String formatPrice(final float price){
		if(Objects.isNull(price)) return "";

		final Locale locale = new Locale("pt", "BR");
		final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

		return currencyFormatter.format(price);
	}
	public static boolean isValidDate(final String date){
		try{
			final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
			dateFormat.setLenient(false);
			Objects.requireNonNull(dateFormat.parse(date));
			return true;
		}catch(ParseException error){
			return false;
		}
	}
}
