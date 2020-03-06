package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsAgency.class)
public class AgencyStreamingValidator
		implements StreamingValidator<GtfsAgency> {

	@Override
	public void validate(Class<? extends GtfsAgency> clazz, GtfsAgency agency,
			Context context) {
		// TODO: Validate URLs, emails format etc...
	}
}
