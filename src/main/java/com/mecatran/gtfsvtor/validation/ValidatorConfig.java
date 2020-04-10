package com.mecatran.gtfsvtor.validation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.model.GtfsLogicalDate;

public interface ValidatorConfig {

	public String getString(String key);

	public default String getString(String key, String defaultValue) {
		String str = this.getString(key);
		if (str == null)
			return defaultValue;
		return str;
	}

	public default Boolean getBoolean(String key, Boolean defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		/*
		 * Being lenient on accepted values is a long-term pain, but in this
		 * case accept common values.
		 */
		if (str.equals("1") || str.equals("true"))
			return true;
		if (str.equals("0") || str.equals("false"))
			return false;
		return defaultValue;
	}

	public default Long getLong(String key, Long defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public default Double getDouble(String key, Double defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public default GtfsLogicalDate getLogicalDate(String key,
			GtfsLogicalDate defaultValue) {
		String str = this.getString(key);
		if (str == null || str.isEmpty())
			return defaultValue;
		DateFormat df = new SimpleDateFormat("yyyy/mm/dd");
		// We do not care about the timezone
		TimeZone tz = TimeZone.getDefault();
		df.setTimeZone(tz);
		try {
			Calendar cal = GregorianCalendar.getInstance(tz);
			cal.setTime(df.parse(str));
			return GtfsLogicalDate.getDate(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
		} catch (ParseException e) {
			return defaultValue;
		}
	}

	public default String getKey(Object validator) {
		return getKey(validator, null);
	}

	public default String getKey(Object validator, String keySuffix) {
		// TODO Make sure this is sane
		// TODO Does this method belongs here?
		return "validator." + validator.getClass().getSimpleName()
				+ (keySuffix == null ? "" : ("." + keySuffix));
	}
}
