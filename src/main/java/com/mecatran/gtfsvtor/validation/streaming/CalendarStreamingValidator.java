package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsCalendar.class)
public class CalendarStreamingValidator
		implements StreamingValidator<GtfsCalendar> {

	@Override
	public void validate(Class<? extends GtfsCalendar> clazz,
			GtfsCalendar calendar, StreamingValidator.Context context) {
		checkNonNull(calendar::isMonday, "monday", context);
		checkNonNull(calendar::isTuesday, "tuesday", context);
		checkNonNull(calendar::isWednesday, "wednesday", context);
		checkNonNull(calendar::isThursday, "thursday", context);
		checkNonNull(calendar::isFriday, "friday", context);
		checkNonNull(calendar::isSaturday, "saturday", context);
		checkNonNull(calendar::isSunday, "sunday", context);
		checkNonNull(calendar::getStartDate, "start_date", context);
		checkNonNull(calendar::getEndDate, "end_date", context);
	}
}
