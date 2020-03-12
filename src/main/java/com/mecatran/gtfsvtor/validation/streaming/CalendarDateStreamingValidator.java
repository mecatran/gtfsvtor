package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkFieldValue;

import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsCalendarDate.class)
public class CalendarDateStreamingValidator
		implements StreamingValidator<GtfsCalendarDate> {

	@ConfigurableOption
	private int minYearInThePast = 1980;

	@ConfigurableOption
	private int maxYearInTheFuture = 2100;

	@Override
	public void validate(Class<? extends GtfsCalendarDate> clazz,
			GtfsCalendarDate calendarDate, StreamingValidator.Context context) {

		// Check date range
		checkFieldValue(
				date -> date != null && date.getYear() < minYearInThePast,
				calendarDate.getDate(), "date", context,
				"date too far in the past");
		checkFieldValue(
				date -> date != null && date.getYear() > maxYearInTheFuture,
				calendarDate.getDate(), "date", context,
				"date too far in the future");

	}
}
