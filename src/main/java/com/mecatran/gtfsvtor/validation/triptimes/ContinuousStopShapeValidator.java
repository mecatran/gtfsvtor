package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsContinuousStop;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class ContinuousStopShapeValidator implements TripTimesValidator {

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		GtfsTrip trip = tripAndTimes.getTrip();
		if (trip.getShapeId() != null)
			return;
		GtfsRoute route = trip.getRouteId() == null ? null
				: context.getDao().getRoute(trip.getRouteId());
		List<GtfsStopTime> stops = tripAndTimes.getStopTimes();
		// A stop-time override applies from that stop to the next one.
		for (int i = 0; i + 1 < stops.size(); i++) {
			GtfsStopTime stop = stops.get(i);
			if (stop.getContinuousPickup(route) != GtfsContinuousStop.NONE
					|| stop.getContinuousDropoff(route) != GtfsContinuousStop.NONE) {
				context.getReportSink().report(new MissingMandatoryValueError(
						trip.getSourceRef(), "shape_id"));
				return;
			}
		}
	}
}
