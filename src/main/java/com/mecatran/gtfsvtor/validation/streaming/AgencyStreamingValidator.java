package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkEmail;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkLang;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkUrl;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsAgency.class)
public class AgencyStreamingValidator
		implements StreamingValidator<GtfsAgency> {

	@Override
	public void validate(Class<? extends GtfsAgency> clazz, GtfsAgency agency,
			Context context) {
		checkUrl(agency::getUrl, "agency_url", context);
		checkEmail(agency::getEmail, "agency_email", context);
		if (agency.getLang() != null) {
			checkLang(() -> agency.getLang().getLanguage(), "agency_lang",
					context);
		}
	}
}
