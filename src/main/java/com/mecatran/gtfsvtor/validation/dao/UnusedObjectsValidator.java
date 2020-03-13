package com.mecatran.gtfsvtor.validation.dao;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class UnusedObjectsValidator implements DaoValidator {

	// TODO Add options to enable/disable unused by type

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		/* Look for unused agencies */
		for (GtfsAgency agency : dao.getAgencies()) {
			if (dao.getRoutesOfAgency(agency.getId()).isEmpty()) {
				reportSink.report(new UnusedObjectWarning("agency",
						agency.getId(), agency.getSourceInfo(), "agency_id"));
			}
		}

		/* Look for unused (empty) routes */
		for (GtfsRoute route : dao.getRoutes()) {
			if (dao.getTripsOfRoute(route.getId()).isEmpty()) {
				reportSink.report(new UnusedObjectWarning("route",
						route.getId(), route.getSourceInfo(), "route_id"));
			}
		}

		/* Look for unused calendars and stops */
		Set<GtfsCalendar.Id> unusedCalendarIds = new HashSet<>(
				dao.getCalendarIndex().getAllCalendarIds());
		Set<GtfsStop.Id> unusedStopsIds = dao.getStops().stream()
				.filter(s -> s.getType() == GtfsStopType.STOP)
				.map(GtfsStop::getId).collect(Collectors.toSet());
		for (GtfsTrip trip : dao.getTrips()) {
			unusedCalendarIds.remove(trip.getServiceId());
			for (GtfsStopTime stopTime : dao.getStopTimesOfTrip(trip.getId())) {
				unusedStopsIds.remove(stopTime.getStopId());
			}
		}
		for (GtfsCalendar.Id unusedCalendarId : unusedCalendarIds) {
			GtfsCalendar calendar = dao.getCalendar(unusedCalendarId);
			if (calendar != null) {
				reportSink.report(
						new UnusedObjectWarning("calendar", unusedCalendarId,
								calendar.getSourceInfo(), "service_id"));
			}
			for (GtfsCalendarDate calendarDate : dao
					.getCalendarDates(unusedCalendarId)) {
				reportSink.report(new UnusedObjectWarning("calendar date",
						unusedCalendarId, calendarDate.getSourceInfo(),
						"service_id"));
			}
		}
		for (GtfsStop.Id unusedStopId : unusedStopsIds) {
			GtfsStop stop = dao.getStop(unusedStopId);
			reportSink.report(new UnusedObjectWarning("stop", unusedStopId,
					stop.getSourceInfo(), "stop_id"));
		}

		/* Look for unused stations */
		for (GtfsStop station : dao.getStopsOfType(GtfsStopType.STATION)) {
			// TODO - shouldn't we warn of station w/o stops?
			// A station with only entrances and nodes would be suspicious
			boolean noStops = dao.getStopsOfStation(station.getId()).isEmpty();
			boolean noEntrances = dao.getEntrancesOfStation(station.getId())
					.isEmpty();
			boolean noNodes = dao.getNodesOfStation(station.getId()).isEmpty();
			if (noStops && noEntrances && noNodes) {
				reportSink.report(new UnusedObjectWarning("station",
						station.getId(), station.getSourceInfo(), "stop_id"));
			}
		}

		/* Look for unused shapes */
		Set<GtfsShape.Id> unusedShapeIds = new HashSet<>(dao.getShapeIds());
		for (GtfsTrip trip : dao.getTrips()) {
			if (trip.getShapeId() != null) {
				unusedShapeIds.remove(trip.getShapeId());
			}
		}
		for (GtfsShape.Id unusedShapeId : unusedShapeIds) {
			reportSink.report(new UnusedObjectWarning("shape", unusedShapeId,
					null, "shape_id"));
		}

		/* Empty (or single stop) trips do have a special validator */
	}
}
