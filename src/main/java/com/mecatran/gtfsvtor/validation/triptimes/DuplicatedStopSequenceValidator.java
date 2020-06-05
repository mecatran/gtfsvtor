package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedStopSequenceError;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class DuplicatedStopSequenceValidator implements TripTimesValidator {

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		GtfsTrip trip = tripAndTimes.getTrip();
		List<GtfsStopTime> stopTimes = tripAndTimes.getStopTimes();
		GtfsTripStopSequence lastSeq = null;
		GtfsStopTime lastStopTime = null;
		for (GtfsStopTime stopTime : stopTimes) {
			GtfsTripStopSequence seq = stopTime.getStopSequence();
			if (seq == null)
				continue;
			if (seq.equals(lastSeq)) {
				GtfsRoute route = dao.getRoute(trip.getRouteId());
				reportSink.report(new DuplicatedStopSequenceError(route, trip,
						lastStopTime, stopTime, seq));
			}
			lastSeq = seq;
			lastStopTime = stopTime;
		}
	}
}
