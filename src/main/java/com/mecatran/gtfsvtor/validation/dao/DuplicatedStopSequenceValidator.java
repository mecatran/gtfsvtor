package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedStopSequenceError;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class DuplicatedStopSequenceValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		dao.getTrips().forEach(trip -> {
			List<GtfsStopTime> stopTimes = dao.getStopTimesOfTrip(trip.getId());
			GtfsTripStopSequence lastSeq = null;
			GtfsStopTime lastStopTime = null;
			for (GtfsStopTime stopTime : stopTimes) {
				GtfsTripStopSequence seq = stopTime.getStopSequence();
				if (seq == null)
					continue;
				if (seq.equals(lastSeq)) {
					GtfsRoute route = dao.getRoute(trip.getRouteId());
					reportSink.report(new DuplicatedStopSequenceError(route,
							trip, lastStopTime, stopTime, seq));
				}
				lastSeq = seq;
				lastStopTime = stopTime;
			}
		});
	}
}
