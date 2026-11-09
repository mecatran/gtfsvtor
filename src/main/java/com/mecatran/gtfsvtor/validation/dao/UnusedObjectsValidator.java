package com.mecatran.gtfsvtor.validation.dao;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class UnusedObjectsValidator implements DaoValidator {

	@ConfigurableOption(description = "Report unused agencies")
	private boolean checkAgencies = true;

	@ConfigurableOption(description = "Report unused routes")
	private boolean checkRoutes = true;

	@ConfigurableOption(description = "Report unused calendars")
	private boolean checkCalendars = true;

	@ConfigurableOption(description = "Report unused calendar dates")
	private boolean checkCalendarDates = true;

	@ConfigurableOption(description = "Report unused shapes")
	private boolean checkShapes = true;

	@ConfigurableOption(description = "Report unused stations")
	private boolean checkStations = true;

	@ConfigurableOption(description = "Report unused levels")
	private boolean checkLevels = true;

	@ConfigurableOption(description = "Report unused areas")
	private boolean checkAreas = true;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		/*
		 * Look for unused agencies. Note that we consider an agency to be used
		 * only if it own some routes. An agency can be referenced in
		 * translations, attributions, fares... and still be considered as
		 * "unused" (no routes).
		 */
		if (checkAgencies && dao.getAgencies().count() > 1)
			dao.getAgencies().forEach(agency -> {
				if (dao.getRoutesOfAgency(agency.getId()).count() == 0) {
					reportSink.report(
							new UnusedObjectWarning("agency", agency.getId(),
									agency.getSourceRef(), "agency_id"));
				}
			});

		/*
		 * Look for unused (empty) routes. Same comment apply to routes: a used
		 * route must contain trips. A route referenced by attributions or other
		 * tables but with no trips is considered unused.
		 */
		if (checkRoutes) {
			dao.getRoutes().forEach(route -> {
				if (dao.getTripsOfRoute(route.getId()).count() == 0) {
					reportSink.report(new UnusedObjectWarning("route",
							route.getId(), route.getSourceRef(), "route_id"));
				}
			});
		}

		/* Look for unused calendars and shapes */
		if (checkCalendars || checkCalendarDates || checkShapes) {
			checkCalendarsAndShapes(dao, reportSink);
		}

		/* Look for unused stations */
		if (checkStations) {
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
		}

		/* Look for unused levels */
		if (checkLevels) {
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
		}

		/* Look for unused areas */
		if (checkAreas) {
			dao.getAreas().forEach(area -> {
				if (dao.getStopsOfArea(area.getId()).count() == 0) {
					reportSink.report(new UnusedObjectWarning("area", area.getId(),
							area.getSourceRef(), "area_id"));
				}
			});
		}

		/* Empty (or single stop) trips do have a special validator */
	}

	private void checkCalendarsAndShapes(IndexedReadOnlyDao dao, ReportSink reportSink) {
		Set<GtfsShape.Id> unusedShapeIds = checkShapes
				? dao.getShapeIds().collect(Collectors.toSet())
				: Collections.emptySet();
		Set<GtfsCalendar.Id> unusedCalendarIds = (checkCalendars || checkCalendarDates)
				? dao.getCalendarIndex().getAllCalendarIds().collect(Collectors.toSet())
				: Collections.emptySet();
		dao.getTrips().forEach(trip -> {
			if (!unusedCalendarIds.isEmpty())
				unusedCalendarIds.remove(trip.getServiceId());
			if (!unusedShapeIds.isEmpty() && trip.getShapeId() != null) {
				unusedShapeIds.remove(trip.getShapeId());
			}
		});
		for (GtfsCalendar.Id unusedCalendarId : unusedCalendarIds) {
			if (checkCalendars) {
				GtfsCalendar calendar = dao.getCalendar(unusedCalendarId);
				if (calendar != null) {
					reportSink.report(
							new UnusedObjectWarning("calendar", unusedCalendarId,
									calendar.getSourceRef(), "service_id"));
				}
			}
			if (checkCalendarDates) {
				dao.getCalendarDates(unusedCalendarId).forEach(calendarDate -> {
					reportSink.report(new UnusedObjectWarning("calendar date",
							unusedCalendarId, calendarDate.getSourceRef(),
							"service_id"));
				});
			}
		}
		for (GtfsShape.Id unusedShapeId : unusedShapeIds) {
			reportSink.report(new UnusedObjectWarning("shape", unusedShapeId,
					null, "shape_id"));
		}
	}
}
