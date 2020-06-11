package com.mecatran.gtfsvtor.validation.dao;

import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStopType;
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
		if (dao.getAgencies().count() > 1)
			dao.getAgencies().forEach(agency -> {
				if (dao.getRoutesOfAgency(agency.getId()).count() == 0) {
					reportSink.report(new UnusedObjectWarning("agency",
							agency.getId(), agency.getSourceRef(), "agency_id"));
				}
			});

		/* Look for unused (empty) routes */
		dao.getRoutes().forEach(route -> {
			if (dao.getTripsOfRoute(route.getId()).count() == 0) {
				reportSink.report(new UnusedObjectWarning("route",
						route.getId(), route.getSourceRef(), "route_id"));
			}
		});

		/* Look for unused calendars and shapes */
		Set<GtfsShape.Id> unusedShapeIds = dao.getShapeIds()
				.collect(Collectors.toSet());
		Set<GtfsCalendar.Id> unusedCalendarIds = dao.getCalendarIndex()
				.getAllCalendarIds().collect(Collectors.toSet());
		dao.getTrips().forEach(trip -> {
			unusedCalendarIds.remove(trip.getServiceId());
			if (trip.getShapeId() != null) {
				unusedShapeIds.remove(trip.getShapeId());
			}
		});
		for (GtfsCalendar.Id unusedCalendarId : unusedCalendarIds) {
			GtfsCalendar calendar = dao.getCalendar(unusedCalendarId);
			if (calendar != null) {
				reportSink.report(
						new UnusedObjectWarning("calendar", unusedCalendarId,
								calendar.getSourceRef(), "service_id"));
			}
			dao.getCalendarDates(unusedCalendarId).forEach(calendarDate -> {
				reportSink.report(new UnusedObjectWarning("calendar date",
						unusedCalendarId, calendarDate.getSourceRef(),
						"service_id"));
			});
		}
		for (GtfsShape.Id unusedShapeId : unusedShapeIds) {
			reportSink.report(new UnusedObjectWarning("shape", unusedShapeId,
					null, "shape_id"));
		}

		/* Look for unused stations */
		dao.getStopsOfType(GtfsStopType.STATION).forEach(station -> {
			// TODO - shouldn't we warn of station w/o stops?
			// A station with only entrances and nodes would be suspicious
			boolean noStops = dao.getStopsOfStation(station.getId())
					.count() == 0;
			boolean noEntrances = dao.getEntrancesOfStation(station.getId())
					.count() == 0;
			boolean noNodes = dao.getNodesOfStation(station.getId())
					.count() == 0;
			if (noStops && noEntrances && noNodes) {
				reportSink.report(new UnusedObjectWarning("station",
						station.getId(), station.getSourceRef(), "stop_id"));
			}
		});

		/* Look for unused levels */
		Set<GtfsLevel.Id> unusedLevelIds = dao.getLevels().map(GtfsLevel::getId)
				.collect(Collectors.toSet());
		dao.getStops().forEach(stop -> {
			if (stop.getLevelId() != null)
				unusedLevelIds.remove(stop.getLevelId());
		});
		for (GtfsLevel.Id unusedLevelId : unusedLevelIds) {
			GtfsLevel level = dao.getLevel(unusedLevelId);
			reportSink.report(new UnusedObjectWarning("level", unusedLevelId,
					level.getSourceRef(), "level_id"));
		}

		/* Empty (or single stop) trips do have a special validator */
	}
}
