package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsFrequency.class)
public class FrequencyStreamingValidator
		implements StreamingValidator<GtfsFrequency> {

	@Override
	public void validate(Class<? extends GtfsFrequency> clazz,
			GtfsFrequency frequency, Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();
		// Check frequency->trip reference
		if (frequency.getTripId() != null
				&& dao.getTrip(frequency.getTripId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"trip_id", frequency.getTripId().getInternalId(),
					GtfsTrip.TABLE_NAME, "trip_id"));
		}
		// Check start_time < end_time
		if (frequency.getStartTime() != null && frequency.getEndTime() != null
				&& frequency.getStartTime()
						.compareTo(frequency.getEndTime()) > 0) {
			reportSink.report(new InvalidFieldValueIssue(
					context.getSourceInfo(), frequency.getEndTime().toString(),
					"end time should be greater or equal than start time",
					"end_time"));
		}
	}
}
