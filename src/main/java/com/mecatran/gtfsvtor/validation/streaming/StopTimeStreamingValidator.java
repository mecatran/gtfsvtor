package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.reporting.issues.TimeTravelAtStopError;
import com.mecatran.gtfsvtor.reporting.issues.UselessTimepointWarning;
import com.mecatran.gtfsvtor.reporting.issues.WrongStopTimeStopTypeError;
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
		// stoptime->trip reference
		if (stopTime.getTripId() != null) {
			GtfsTrip trip = dao.getTrip(stopTime.getTripId());
			if (trip == null) {
				reportSink.report(
						new InvalidReferenceError(context.getSourceInfo(),
								"trip_id", stopTime.getTripId().getInternalId(),
								GtfsTrip.TABLE_NAME, "trip_id"));
			}
		}
		if (stopTime.getStopId() != null) {
			GtfsStop stop = dao.getStop(stopTime.getStopId());
			// stoptime->stop reference
			if (stop == null) {
				reportSink.report(
						new InvalidReferenceError(context.getSourceInfo(),
								"stop_id", stopTime.getStopId().getInternalId(),
								GtfsStop.TABLE_NAME, "stop_id"));
			} else {
				// stop type
				if (stop.getType() != GtfsStopType.STOP) {
					reportSink.report(new WrongStopTimeStopTypeError(
							context.getSourceInfo(), stopTime, stop));
				}
			}
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
		// No pickup/dropoff and no timepoint
		if (stopTime.getNonNullDropoffType() == GtfsDropoffType.NO_DROPOFF
				&& stopTime.getNonNullPickupType() == GtfsPickupType.NO_PICKUP
				&& stopTime
						.getNonNullTimepoint() == GtfsTimepoint.APPROXIMATE) {
			reportSink.report(new UselessTimepointWarning(stopTime,
					context.getSourceInfo()));
		}
	}
}
