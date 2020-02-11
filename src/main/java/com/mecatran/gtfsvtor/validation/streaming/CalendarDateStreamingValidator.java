package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsCalendarDate.class)
public class CalendarDateStreamingValidator
		implements StreamingValidator<GtfsCalendarDate> {

	@Override
	public void validate(GtfsCalendarDate calendarDate,
			StreamingValidator.Context context) {
		checkNonNull(calendarDate::getDate, "date", context);
		checkNonNull(calendarDate::getExceptionType, "exception_type", context);
	}
}
