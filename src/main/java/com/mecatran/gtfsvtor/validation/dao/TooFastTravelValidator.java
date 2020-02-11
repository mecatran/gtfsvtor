package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex.ProjectedPoint;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsRouteType;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.TooFastTravelIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class TooFastTravelValidator implements DaoValidator {

	@ConfigurableOption
	private double errorSpeedMultiplier = 3.;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();
		ReportSink reportSink = context.getReportSink();
		for (GtfsTrip trip : dao.getTrips()) {
			GtfsRoute route = dao.getRoute(trip.getRouteId());
			List<GtfsStopTime> stopTimes = dao.getStopTimesOfTrip(trip.getId());
			double maxSpeedMps = getMaxSpeedMps(route);
			boolean hasExactSeconds = false;
			for (GtfsStopTime stopTime : stopTimes) {
				if (stopTime.getArrivalTime() != null
						&& stopTime.getArrivalTime().getSecond() != 0) {
					hasExactSeconds = true;
					break;
				}
				if (stopTime.getDepartureTime() != null
						&& stopTime.getDepartureTime().getSecond() != 0) {
					hasExactSeconds = true;
					break;
				}
			}
			int slackSec = hasExactSeconds ? 0 : 60;
			GtfsStopTime lastValidStopTime = null;
			ProjectedPoint lastValidProjectedPoint = null;
			for (GtfsStopTime stopTime : stopTimes) {
				ProjectedPoint projectedPoint = lgi.getProjectedPoint(stopTime);
				GtfsLogicalTime arrivalTime = stopTime
						.getArrivalOrDepartureTime();
				if (projectedPoint != null && arrivalTime != null) {
					if (lastValidStopTime != null) {
						double d = projectedPoint.getLinearDistanceMeters()
								- lastValidProjectedPoint
										.getLinearDistanceMeters();
						int t = arrivalTime.getSecondSinceMidnight()
								- lastValidStopTime.getDepartureOrArrivalTime()
										.getSecondSinceMidnight();
						double speedMps = d / (t + slackSec);
						if (speedMps > maxSpeedMps) {
							GtfsStop stop1 = dao
									.getStop(lastValidStopTime.getStopId());
							GtfsStop stop2 = dao.getStop(stopTime.getStopId());
							ReportIssueSeverity severity = getSeverity(speedMps,
									maxSpeedMps);
							reportSink.report(new TooFastTravelIssue(route,
									trip, lastValidStopTime, stop1, stopTime,
									stop2, d, speedMps, maxSpeedMps, severity));
						}
					}
					lastValidStopTime = stopTime;
					lastValidProjectedPoint = projectedPoint;
				}
			}
		}
	}

	private ReportIssueSeverity getSeverity(double speedMps,
			double maxSpeedMps) {
		double factor = speedMps / maxSpeedMps;
		return factor >= errorSpeedMultiplier ? ReportIssueSeverity.ERROR
				: ReportIssueSeverity.WARNING;
	}

	// TODO Read this values from the config
	private double getMaxSpeedMps(GtfsRoute route) {
		double maxSpeedKph;
		// For bogus route, test anyway, fallback on BUS
		int routeTypeCode = GtfsRouteType.BUS_CODE;
		if (route != null && route.getType() != null)
			routeTypeCode = route.getType().getValue();
		switch (routeTypeCode) {
		default:
		case GtfsRouteType.CABLE_CAR_CODE:
		case GtfsRouteType.GONDOLA_CODE:
		case GtfsRouteType.FUNICULAR_CODE:
			maxSpeedKph = 50;
			break;
		case GtfsRouteType.FERRY_CODE:
			maxSpeedKph = 80;
			break;
		case GtfsRouteType.TRAM_CODE:
		case GtfsRouteType.BUS_CODE:
			maxSpeedKph = 100;
			break;
		case GtfsRouteType.METRO_CODE:
			maxSpeedKph = 150;
			break;
		case GtfsRouteType.RAIL_CODE:
			maxSpeedKph = 300;
			break;
		}
		return maxSpeedKph / 3.6; // in m/s
	}
}
