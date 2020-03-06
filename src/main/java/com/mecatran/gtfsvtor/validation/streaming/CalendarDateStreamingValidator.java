package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsCalendarDate.class)
public class CalendarDateStreamingValidator
		implements StreamingValidator<GtfsCalendarDate> {

	@Override
	public void validate(Class<? extends GtfsCalendarDate> clazz,
			GtfsCalendarDate calendarDate, StreamingValidator.Context context) {
		// Nothing to validate for the moment
	}
}
