package com.example.mercado;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

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
		}catch(final Exception error){
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

		@SuppressWarnings("deprecation")
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
		}catch(final ParseException error){
			return false;
		}
	}
	@NonNull
	public static String formatDate(@NonNull final String date) throws NullPointerException {
		try{
			@SuppressLint("DefaultLocale")
			final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

			dateFormat.setLenient(false);

			@SuppressWarnings("deprecation")
			final Locale locale = new Locale("pt", "BR");

			return DateFormat.getDateInstance(DateFormat.SHORT, locale)
				.format(Objects.requireNonNull(dateFormat.parse(date)));
		}catch(final ParseException error){
			Log.e(TAG, Objects.requireNonNull(error.getMessage()));
			return "";
		}
	}
}
