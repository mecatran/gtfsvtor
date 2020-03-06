package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsCalendar.class)
public class CalendarStreamingValidator
		implements StreamingValidator<GtfsCalendar> {

	@Override
	public void validate(Class<? extends GtfsCalendar> clazz,
			GtfsCalendar calendar, StreamingValidator.Context context) {
		// Nothing to validate for now
	}
}
