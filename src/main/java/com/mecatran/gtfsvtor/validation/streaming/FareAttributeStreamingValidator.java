package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsFareAttribute.class)
public class FareAttributeStreamingValidator
		implements StreamingValidator<GtfsFareAttribute> {

	@Override
	public void validate(Class<? extends GtfsFareAttribute> clazz,
			GtfsFareAttribute fareAttribute,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();

		// Check transfer duration is not negative
		if (fareAttribute.getTransferDuration() != null
				&& fareAttribute.getTransferDuration() < 0) {
			reportSink.report(new InvalidFieldFormatError(
					context.getSourceInfo(), "transfer_duration",
					Integer.toString(fareAttribute.getTransferDuration()),
					"positive integer"));
		}

		// Check price is not negative
		if (fareAttribute.getPrice() != null && fareAttribute.getPrice() < 0.) {
			reportSink
					.report(new InvalidFieldFormatError(context.getSourceInfo(),
							"price", Double.toString(fareAttribute.getPrice()),
							"non-negative float"));
		}

		ReadOnlyDao dao = context.getPartialDao();

		// Check agency reference
		if (fareAttribute.getAgencyId() != null
				&& dao.getAgency(fareAttribute.getAgencyId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"agency_id", fareAttribute.getAgencyId().getInternalId(),
					GtfsAgency.TABLE_NAME, "agency_id"));
		}
	}
}
