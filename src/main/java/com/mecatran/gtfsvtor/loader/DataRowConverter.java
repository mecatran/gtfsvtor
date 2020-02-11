package com.mecatran.gtfsvtor.loader;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.model.GtfsBikeAccess;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTripDirectionId;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.model.GtfsWheelchairAccess;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;

public class DataRowConverter {

	private DataRow row;
	private ReportSink reportSink;

	public DataRowConverter(DataRow row, ReportSink reportSink) {
		this.row = row;
		this.reportSink = reportSink;
	}

	public String getString(String field) {
		return getString(field, null);
	}

	public String getString(String field, String defaultValue) {
		String ret = row.getString(field);
		if (ret != null && ret.contains("\uFFFD")) {
			reportSink.report(
					new InvalidEncodingError(row.getSourceInfo(), field, ret));
		}
		if (ret == null || ret.isEmpty()) {
			return defaultValue;
		}
		return ret;
	}

	public Integer getInteger(String field) {
		return this.getInteger(field, null);
	}

	public Integer getInteger(String field, Integer defaultValue) {
		String value = getString(field);
		if (value == null || value.isEmpty())
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			reportSink.report(fieldFormatError(field, value, "integer"));
			return defaultValue;
		}
	}

	public Double getDouble(String field) {
		return this.getDouble(field, null);
	}

	public Double getDouble(String field, Double defaultValue) {
		String value = getString(field);
		if (value == null || value.isEmpty())
			return defaultValue;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			reportSink.report(
					fieldFormatError(field, value, "floating-point (double)"));
			return defaultValue;
		}
	}

	public Boolean getBoolean(String field) {
		return this.getBoolean(field, null);
	}

	public Boolean getBoolean(String field, Boolean defaultValue) {
		String value = getString(field);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		} else if (value.equals("0")) {
			return false;
		} else if (value.equals("1")) {
			return true;
		} else {
			reportSink
					.report(fieldFormatError(field, value, "boolean (0 or 1)"));
			return defaultValue;
		}
	}

	public TimeZone getTimeZone(String field) {
		String tz = getString(field);
		if (tz == null || tz.isEmpty())
			return null;
		try {
			ZoneId zoneId = ZoneId.of(tz);
			return TimeZone.getTimeZone(zoneId);
		} catch (DateTimeException e) {
			reportSink.report(fieldFormatError(field, tz, "IANA timezone"));
			return null;
		}
	}

	public Locale getLocale(String field) {
		String lang = getString(field);
		if (lang == null || lang.isEmpty())
			return null;
		try {
			return Locale.forLanguageTag(lang);
		} catch (Exception e) {
			// TODO Validate correct lang
			reportSink.report(fieldFormatError(field, lang, "lang code"));
			return null;
		}
	}

	public GtfsLogicalDate getLogicalDate(String field) {
		return this.getLogicalDate(field, null);
	}

	public GtfsLogicalDate getLogicalDate(String field,
			GtfsLogicalDate defaultValue) {
		String value = getString(field);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		} else {
			try {
				GtfsLogicalDate ret = GtfsLogicalDate.parseFromYYYYMMDD(value);
				return ret;
			} catch (ParseException e) {
				reportSink.report(
						fieldFormatError(field, value, "date (YYYYMMDD)"));
				return defaultValue;
			}
		}
	}

	public GtfsLogicalTime getLogicalTime(String field) {
		return this.getLogicalTime(field, null);
	}

	public GtfsLogicalTime getLogicalTime(String field,
			GtfsLogicalTime defaultValue) {
		String value = getString(field);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		} else {
			try {
				GtfsLogicalTime ret = GtfsLogicalTime.parseFromHH_MM_SS(value);
				return ret;
			} catch (ParseException e) {
				reportSink.report(fieldFormatError(field, value,
						"time (HH:MM:SS) " + e.getLocalizedMessage()));
				return defaultValue;
			}
		}
	}

	public GtfsCalendarDateExceptionType getCalendarDateExceptionType(
			String field) {
		String str = getString(field);
		try {
			if (str == null || str.isEmpty())
				throw new IllegalArgumentException(
						"Exception type cannot be null");
			return GtfsCalendarDateExceptionType
					.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(
					fieldFormatError(field, str, "exception type (1 or 2)"));
			return null;
		}
	}

	public GtfsStopType getStopType(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsStopType.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(fieldFormatError(field, str,
					"stop type (0, 1, 2, 3 or 4)"));
			return null;
		}
	}

	public GtfsBlockId getBlockId(String field) {
		return GtfsBlockId.fromValue(getString(field));
	}

	public GtfsTripDirectionId getDirectionId(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsTripDirectionId.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(
					fieldFormatError(field, str, "direction ID (0 or 1)"));
			return null;
		}
	}

	public GtfsTripStopSequence getTripStopSequence(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsTripStopSequence.fromSequence(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(
					fieldFormatError(field, str, "stop sequence (integer)"));
			return null;
		}
	}

	public GtfsShapePointSequence getShapePointSequence(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsShapePointSequence.fromSequence(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(
					fieldFormatError(field, str, "point sequence (integer)"));
			return null;
		}
	}

	public GtfsPickupType getPickupType(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsPickupType.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(
					fieldFormatError(field, str, "pickup type (0, 1, 2 or 3)"));
			return null;
		}
	}

	public GtfsDropoffType getDropoffType(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsDropoffType.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(fieldFormatError(field, str,
					"drop-off type (0, 1, 2 or 3)"));
			return null;
		}
	}

	public GtfsTimepoint getTimepoint(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsTimepoint.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink
					.report(fieldFormatError(field, str, "timepoint (0 or 1)"));
			return null;
		}
	}

	public GtfsColor getColor(String field) {
		String str = getString(field);
		try {
			return GtfsColor.parseHexTriplet(str);
		} catch (ParseException e) {
			reportSink.report(fieldFormatError(field, str,
					"hexadecimal RGB color triplet (6 characters)"));
			return null;
		}
	}

	public GtfsWheelchairAccess getWheelchairAccess(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsWheelchairAccess.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(fieldFormatError(field, str,
					"wheelchair access (0, 1, 2)"));
			return null;
		}
	}

	public GtfsBikeAccess getBikeAccess(String field) {
		String str = getString(field);
		if (str == null || str.isEmpty())
			return null;
		try {
			return GtfsBikeAccess.fromValue(Integer.parseInt(str));
		} catch (IllegalArgumentException e) {
			reportSink.report(
					fieldFormatError(field, str, "bike access (0, 1, 2)"));
			return null;
		}
	}

	private InvalidFieldFormatError fieldFormatError(String field, String value,
			String expectedFormat) {
		return new InvalidFieldFormatError(row.getSourceInfo(), field, value,
				expectedFormat);
	}
}
