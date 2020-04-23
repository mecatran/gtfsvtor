package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.FirstOrLastStopTimeMissingError;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class FirstAndLastStopTimeSetValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		dao.getTrips().forEach(trip -> {
			List<GtfsStopTime> stopTimes = dao.getStopTimesOfTrip(trip.getId());
			if (stopTimes.isEmpty())
				return;
			GtfsStopTime firstStopTime = stopTimes.get(0);
			GtfsStopTime lastStopTime = stopTimes.get(stopTimes.size() - 1);
			if (firstStopTime.getDepartureTime() == null) {
				GtfsRoute route = dao.getRoute(trip.getRouteId());
				reportSink.report(new FirstOrLastStopTimeMissingError(true,
						route, trip, firstStopTime));
			}
			if (lastStopTime.getDepartureTime() == null) {
				GtfsRoute route = dao.getRoute(trip.getRouteId());
				reportSink.report(new FirstOrLastStopTimeMissingError(false,
						route, trip, lastStopTime));
			}
		});
	}
}
