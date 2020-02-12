package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsAgency.class)
public class AgencyStreamingValidator
		implements StreamingValidator<GtfsAgency> {

	@Override
	public void validate(Class<? extends GtfsAgency> clazz, GtfsAgency agency,
			Context context) {
		checkNonNull(agency::getName, "agency_name", context);
		checkNonNull(agency::getUrl, "agency_url", context);
		checkNonNull(agency::getTimezone, "agency_timezone", context);
	}
}
