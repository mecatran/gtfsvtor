package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.WrongStopTimeStopTypeError;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class StopTimeStopTypeValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		for (GtfsTrip trip : dao.getTrips()) {
			List<GtfsStopTime> stopTimes = dao.getStopTimesOfTrip(trip.getId());
			for (GtfsStopTime stopTime : stopTimes) {
				GtfsStop stop = dao.getStop(stopTime.getStopId());
				if (stop != null && stop.getType() != GtfsStopType.STOP) {
					GtfsRoute route = dao.getRoute(trip.getRouteId());
					reportSink.report(new WrongStopTimeStopTypeError(route,
							trip, stopTime, stop));
				}
			}
		}
	}
}
