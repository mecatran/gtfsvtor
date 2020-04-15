package com.mecatran.gtfsvtor.loader;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.model.GtfsBikeAccess;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsColor;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsExactTime;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.model.GtfsTripDirectionId;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.model.GtfsWheelchairAccess;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;

public class DataRowConverter {

	private DataRow row;
	private ReportSink reportSink;

	public DataRowConverter(DataRow row, ReportSink reportSink) {
		this.row = row;
		this.reportSink = reportSink;
	}

	public String getString(String field) {
		return getString(field, null, false);
	}

	public String getString(String field, boolean mandatory) {
		return getString(field, null, mandatory);
	}

	public String getString(String field, String defaultValue,
			boolean mandatory) {
		String ret = row.getString(field);
		if (ret != null && ret.contains("\uFFFD")) {
			reportSink.report(
					new InvalidEncodingError(row.getSourceInfo(), field, ret));
			// Return the string anyway
		}
		if (ret == null || ret.isEmpty()) {
			if (mandatory) {
				reportSink.report(new MissingMandatoryValueError(
						row.getSourceInfo(), field));
			}
			return defaultValue;
		}
		return ret;
	}

	public Integer getInteger(String field) {
		return this.getInteger(field, false);
	}

	public Integer getInteger(String field, boolean mandatory) {
		return getTypeFromString(Integer.class, field, mandatory, "integer",
				Integer::parseInt);
	}

	public Double getDouble(String field, boolean mandatory) {
		return this.getDouble(field, null, null, mandatory);
	}

	public Double getDouble(String field, Double defaultValue,
			Double defaultValueIfInvalid, boolean mandatory) {
		String value = getString(field);
		if (value == null || value.isEmpty()) {
			if (mandatory) {
				reportSink.report(new MissingMandatoryValueError(
						row.getSourceInfo(), field));
			}
			return defaultValue;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			reportSink.report(
					fieldFormatError(field, value, "floating-point (double)"));
			return defaultValueIfInvalid;
		}
	}

	public Boolean getBoolean(String field) {
		return this.getBoolean(field, false);
	}

	public Boolean getBoolean(String field, boolean mandatory) {
		return getTypeFromString(Boolean.class, field, mandatory,
				"boolean (0 or 1)", str -> {
					if (str.equals("0"))
						return false;
					else if (str.equals("1"))
						return true;
					else
						throw new ParseException(str, 0);
				});
	}

	public TimeZone getTimeZone(String field) {
		return getTimeZone(field, false);
	}

	public TimeZone getTimeZone(String field, boolean mandatory) {
		return getTypeFromString(TimeZone.class, field, mandatory,
				"IANA timezone", tz -> TimeZone.getTimeZone(ZoneId.of(tz)));
	}

	public Locale getLocale(String field) {
		return getTypeFromString(Locale.class, field, false,
				"ISO 639-1 language code", Locale::forLanguageTag);
	}

	public GtfsLogicalDate getLogicalDate(String field, boolean mandatory) {
		return getTypeFromString(GtfsLogicalDate.class, field, mandatory,
				"time (HH:MM:SS)", GtfsLogicalDate::parseFromYYYYMMDD);
	}

	public GtfsLogicalTime getLogicalTime(String field) {
		return getTypeFromString(GtfsLogicalTime.class, field, false,
				"time (HH:MM:SS)", GtfsLogicalTime::parseFromHH_MM_SS);
	}

	public GtfsCalendarDateExceptionType getCalendarDateExceptionType(
			String field) {
		return getTypeFromInteger(GtfsCalendarDateExceptionType.class, field,
				true, "exception type (1 or 2)",
				GtfsCalendarDateExceptionType::fromValue);
	}

	public GtfsStopType getStopType(String field) {
		return getTypeFromInteger(GtfsStopType.class, field, false,
				"stop type (0, 1, 2, 3 or 4)", GtfsStopType::fromValue);
	}

	public GtfsBlockId getBlockId(String field) {
		return GtfsBlockId.fromValue(getString(field));
	}

	public GtfsTripDirectionId getDirectionId(String field) {
		return getTypeFromInteger(GtfsTripDirectionId.class, field, false,
				"direction ID (0 or 1)", GtfsTripDirectionId::fromValue);
	}

	public GtfsTripStopSequence getTripStopSequence(String field) {
		return getTypeFromInteger(GtfsTripStopSequence.class, field, false,
				"stop sequence (integer)", GtfsTripStopSequence::fromSequence);
	}

	public GtfsShapePointSequence getShapePointSequence(String field) {
		return getTypeFromInteger(GtfsShapePointSequence.class, field, false,
				"point sequence (integer)",
				GtfsShapePointSequence::fromSequence);
	}

	public GtfsPickupType getPickupType(String field) {
		return getTypeFromInteger(GtfsPickupType.class, field, false,
				"pickup type (0, 1, 2 or 3)", GtfsPickupType::fromValue);
	}

	public GtfsDropoffType getDropoffType(String field) {
		return getTypeFromInteger(GtfsDropoffType.class, field, false,
				"drop-off type (0, 1, 2 or 3)", GtfsDropoffType::fromValue);
	}

	public GtfsTimepoint getTimepoint(String field) {
		return getTypeFromInteger(GtfsTimepoint.class, field, false,
				"timepoint (0 or 1)", GtfsTimepoint::fromValue);
	}

	public GtfsColor getColor(String field) {
		return getTypeFromString(GtfsColor.class, field, false,
				"hexadecimal RGB color triplet (6 characters)",
				GtfsColor::parseHexTriplet);
	}

	public GtfsWheelchairAccess getWheelchairAccess(String field) {
		return getTypeFromInteger(GtfsWheelchairAccess.class, field, false,
				"wheelchair access (0, 1, 2)", GtfsWheelchairAccess::fromValue);
	}

	public GtfsBikeAccess getBikeAccess(String field) {
		return getTypeFromInteger(GtfsBikeAccess.class, field, false,
				"bike access (0, 1, 2)", GtfsBikeAccess::fromValue);
	}

	public GtfsExactTime getExactTimes(String field) {
		return getTypeFromInteger(GtfsExactTime.class, field, false,
				"exact times (0, 1)", GtfsExactTime::fromValue);
	}

	public GtfsTransferType getTransferType(String field) {
		return getTypeFromInteger(GtfsTransferType.class, field, false,
				"transfer type (0, 1, 2, 3)", GtfsTransferType::fromValue);
	}

	@FunctionalInterface
	public interface DataConverter<T, R> {
		R convert(T t) throws Exception;
	}

	private <T> T getTypeFromInteger(Class<T> clazz, String field,
			boolean mandatory, String expectedFormat,
			DataConverter<Integer, T> func) {
		return getTypeFromString(clazz, field, mandatory, expectedFormat,
				str -> func.convert(Integer.parseInt(str)));
	}

	private <T> T getTypeFromString(Class<T> clazz, String field,
			boolean mandatory, String expectedFormat,
			DataConverter<String, T> func) {
		String str = getString(field);
		if (str == null || str.isEmpty()) {
			if (mandatory) {
				reportSink.report(new MissingMandatoryValueError(
						row.getSourceInfo(), field));
			}
			return null;
		}
		try {
			return func.convert(str);
		} catch (Exception e) {
			reportSink.report(fieldFormatError(field, str, expectedFormat));
			return null;
		}
	}

	private InvalidFieldFormatError fieldFormatError(String field, String value,
			String expectedFormat) {
		return fieldFormatError(field, value, expectedFormat, null);
	}

	private InvalidFieldFormatError fieldFormatError(String field, String value,
			String expectedFormat, String additionalInfo) {
		return new InvalidFieldFormatError(row.getSourceInfo(), field, value,
				expectedFormat, additionalInfo);
	}
}
