package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.reporting.issues.TimeTravelAtStopError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsStopTime.class)
public class StopTimeStreamingValidator
		implements StreamingValidator<GtfsStopTime> {

	@Override
	public void validate(Class<? extends GtfsStopTime> clazz,
			GtfsStopTime stopTime, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		// Trip id / stop sequence is primary key and tested by DAO
		checkNonNull(stopTime::getStopId, "stop_id", context);
		if (stopTime.getStopSequence().getSequence() < 0)
			reportSink.report(new InvalidFieldFormatError(
					context.getSourceInfo(), "stop_sequence",
					Integer.toString(stopTime.getStopSequence().getSequence()),
					"positive integer"));

		ReadOnlyDao dao = context.getPartialDao();
		// stoptime->stop reference
		if (stopTime.getStopId() != null
				&& dao.getStop(stopTime.getStopId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"stop_id",
					stopTime.getStopId() == null ? null
							: stopTime.getStopId().getInternalId(),
					GtfsStop.TABLE_NAME, "stop_id"));
		}
		// Departure/arrival should be either set or not
		if (stopTime.getDepartureTime() == null
				&& stopTime.getArrivalTime() != null) {
			reportSink.report(new MissingMandatoryValueError(
					context.getSourceInfo(), "departure_time"));
		}
		if (stopTime.getDepartureTime() != null
				&& stopTime.getArrivalTime() == null) {
			reportSink.report(new MissingMandatoryValueError(
					context.getSourceInfo(), "arrival_time"));
		}
		// Departure should be after arrival
		if (stopTime.getDepartureTime() != null
				&& stopTime.getArrivalTime() != null
				&& stopTime.getDepartureTime()
						.compareTo(stopTime.getArrivalTime()) < 0) {
			reportSink.report(new TimeTravelAtStopError(stopTime,
					context.getSourceInfo()));
		}
	}
}
