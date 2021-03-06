package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.FirstOrLastStopTimeMissingError;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class FirstAndLastStopTimeSetValidator implements TripTimesValidator {

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		GtfsTrip trip = tripAndTimes.getTrip();
		List<GtfsStopTime> stopTimes = tripAndTimes.getStopTimes();

		if (stopTimes.isEmpty())
			return;

		GtfsStopTime firstStopTime = stopTimes.get(0);
		GtfsStopTime lastStopTime = stopTimes.get(stopTimes.size() - 1);
		if (firstStopTime.getDepartureTime() == null) {
			GtfsRoute route = dao.getRoute(trip.getRouteId());
			reportSink.report(new FirstOrLastStopTimeMissingError(true, route,
					trip, firstStopTime));
		}
		if (lastStopTime.getDepartureTime() == null) {
			GtfsRoute route = dao.getRoute(trip.getRouteId());
			reportSink.report(new FirstOrLastStopTimeMissingError(false, route,
					trip, lastStopTime));
		}
	}

}
