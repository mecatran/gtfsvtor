package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.DaoSpatialIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.loader.DataLoader;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendar.Id;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.MissingObjectIdError;

public class InMemoryDao implements IndexedReadOnlyDao, AppendableDao {

	private Map<GtfsAgency.Id, GtfsAgency> agencies = new HashMap<>();
	private Map<GtfsRoute.Id, GtfsRoute> routes = new HashMap<>();
	private Map<GtfsStop.Id, GtfsStop> stops = new HashMap<>();
	private Map<GtfsCalendar.Id, GtfsCalendar> calendars = new HashMap<>();
	private Map<GtfsTrip.Id, GtfsTrip> trips = new HashMap<>();
	private ListMultimap<GtfsTrip.Id, GtfsStopTime> stopTimes = ArrayListMultimap
			.create();
	private ListMultimap<GtfsShape.Id, GtfsShapePoint> shapePoints = ArrayListMultimap
			.create();
	private Multimap<GtfsCalendar.Id, GtfsCalendarDate> calendarDates = ArrayListMultimap
			.create();
	private Multimap<GtfsAgency.Id, GtfsRoute> routesPerAgency = ArrayListMultimap
			.create();
	private Multimap<GtfsRoute.Id, GtfsTrip> tripsPerRoute = ArrayListMultimap
			.create();
	private Multimap<GtfsCalendar.Id, GtfsTrip> tripsPerCalendar = ArrayListMultimap
			.create();
	private Multimap<GtfsStopType, GtfsStop> stopsPerType = ArrayListMultimap
			.create();
	private Multimap<GtfsStop.Id, GtfsStop> stopsPerStation = ArrayListMultimap
			.create();
	private Multimap<GtfsStop.Id, GtfsStop> entrancesPerStation = ArrayListMultimap
			.create();
	private Multimap<GtfsStop.Id, GtfsStop> nodesPerStation = ArrayListMultimap
			.create();
	private Multimap<GtfsStop.Id, GtfsStop> boardingAreasPerStop = ArrayListMultimap
			.create();

	private CalendarIndex calendarIndex = null;
	private DaoSpatialIndex spatialIndex = null;
	private LinearGeometryIndex linearGeometryIndex = null;

	public InMemoryDao() {
	}

	@Override
	public Collection<GtfsAgency> getAgencies() {
		return Collections.unmodifiableCollection(agencies.values());
	}

	@Override
	public GtfsAgency getAgency(GtfsAgency.Id agencyId) {
		return agencies.get(agencyId);
	}

	@Override
	public Collection<GtfsRoute> getRoutes() {
		return Collections.unmodifiableCollection(routes.values());
	}

	@Override
	public GtfsRoute getRoute(GtfsRoute.Id routeId) {
		return routes.get(routeId);
	}

	@Override
	public Collection<GtfsRoute> getRoutesOfAgency(GtfsAgency.Id agencyId) {
		return Collections
				.unmodifiableCollection(routesPerAgency.get(agencyId));
	}

	@Override
	public Collection<GtfsStop> getStops() {
		return Collections.unmodifiableCollection(stops.values());
	}

	@Override
	public GtfsStop getStop(GtfsStop.Id stopId) {
		return stops.get(stopId);
	}

	@Override
	public Collection<GtfsCalendar> getCalendars() {
		return Collections.unmodifiableCollection(calendars.values());
	}

	@Override
	public GtfsCalendar getCalendar(GtfsCalendar.Id calendarId) {
		return calendars.get(calendarId);
	}

	@Override
	public Collection<GtfsCalendarDate> getCalendarDates() {
		return Collections.unmodifiableCollection(calendarDates.values());
	}

	@Override
	public Collection<GtfsCalendarDate> getCalendarDates(Id calendarId) {
		return Collections
				.unmodifiableCollection(calendarDates.get(calendarId));
	}

	@Override
	public boolean hasShape(GtfsShape.Id shapeId) {
		return !shapePoints.get(shapeId).isEmpty();
	}

	@Override
	public Collection<GtfsTrip> getTrips() {
		return Collections.unmodifiableCollection(trips.values());
	}

	@Override
	public GtfsTrip getTrip(GtfsTrip.Id tripId) {
		return trips.get(tripId);
	}

	@Override
	public int getStopTimesCount() {
		return stopTimes.size();
	}

	@Override
	public int getShapePointsCount() {
		return shapePoints.size();
	}

	@Override
	public Collection<GtfsStop> getStopsOfType(GtfsStopType stopType) {
		return Collections.unmodifiableCollection(stopsPerType.get(stopType));
	}

	@Override
	public Collection<GtfsStop> getStopsOfStation(GtfsStop.Id station) {
		return Collections.unmodifiableCollection(stopsPerStation.get(station));
	}

	@Override
	public Collection<GtfsStop> getEntrancesOfStation(
			com.mecatran.gtfsvtor.model.GtfsStop.Id station) {
		return Collections
				.unmodifiableCollection(entrancesPerStation.get(station));
	}

	@Override
	public Collection<GtfsStop> getNodesOfStation(
			com.mecatran.gtfsvtor.model.GtfsStop.Id station) {
		return Collections.unmodifiableCollection(nodesPerStation.get(station));
	}

	@Override
	public Collection<GtfsStop> getBoardingAreasOfStop(
			com.mecatran.gtfsvtor.model.GtfsStop.Id stop) {
		return Collections
				.unmodifiableCollection(boardingAreasPerStop.get(stop));
	}

	@Override
	public Collection<GtfsTrip> getTripsOfRoute(GtfsRoute.Id routeId) {
		return Collections.unmodifiableCollection(tripsPerRoute.get(routeId));
	}

	@Override
	public Collection<GtfsTrip> getTripsOfCalendar(GtfsCalendar.Id calendarId) {
		return Collections
				.unmodifiableCollection(tripsPerCalendar.get(calendarId));
	}

	@Override
	public List<GtfsStopTime> getStopTimesOfTrip(GtfsTrip.Id tripId) {
		return Collections.unmodifiableList(stopTimes.get(tripId));
	}

	@Override
	public List<GtfsShapePoint> getPointsOfShape(GtfsShape.Id shapeId) {
		return Collections.unmodifiableList(shapePoints.get(shapeId));
	}

	@Override
	public CalendarIndex getCalendarIndex() {
		// Lazy create the calendar index
		if (calendarIndex == null) {
			calendarIndex = new InMemoryCalendarIndex(this);
		}
		return calendarIndex;
	}

	@Override
	public DaoSpatialIndex getSpatialIndex() {
		// Lazy create the spatial index
		if (spatialIndex == null) {
			spatialIndex = new InMemoryDaoSpatialIndex(this);
		}
		return spatialIndex;
	}

	@Override
	public LinearGeometryIndex getLinearGeometryIndex() {
		// Lazy create the index
		if (linearGeometryIndex == null) {
			linearGeometryIndex = new InMemoryLinearGeometryIndex(this);
		}
		return linearGeometryIndex;
	}

	@Override
	public void addAgency(GtfsAgency agency,
			DataLoader.SourceContext sourceContext) {
		GtfsAgency existingAgency = getAgency(agency.getId());
		if (existingAgency != null) {
			sourceContext.getReportSink().report(new DuplicatedObjectIdError(
					existingAgency, agency, agency.getId(), "agency_id"));
			return;
		}
		agencies.put(agency.getId(), agency);
	}

	@Override
	public void addRoute(GtfsRoute route,
			DataLoader.SourceContext sourceContext) {
		if (route.getId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "route_id"));
			return;
		}
		GtfsRoute existingRoute = getRoute(route.getId());
		if (existingRoute != null) {
			sourceContext.getReportSink().report(new DuplicatedObjectIdError(
					existingRoute, route, route.getId(), "route_id"));
			return;
		}
		routes.put(route.getId(), route);
		// Note: this will also work for null agency IDs
		routesPerAgency.put(route.getAgencyId(), route);
	}

	@Override
	public void addStop(GtfsStop stop, DataLoader.SourceContext sourceContext) {
		if (stop.getId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "stop_id"));
			return;
		}
		GtfsStop existingStop = getStop(stop.getId());
		if (existingStop != null) {
			sourceContext.getReportSink().report(new DuplicatedObjectIdError(
					existingStop, stop, stop.getId(), "stop_id"));
			return;
		}
		stops.put(stop.getId(), stop);
		stopsPerType.put(stop.getType(), stop);
		GtfsStop.Id parentId = stop.getParentId();
		if (parentId != null) {
			// Add to parent->child index
			switch (stop.getType()) {
			case STOP:
				stopsPerStation.put(parentId, stop);
				break;
			case ENTRANCE:
				entrancesPerStation.put(parentId, stop);
				break;
			case NODE:
				nodesPerStation.put(parentId, stop);
				break;
			case BOARDING_AREA:
				boardingAreasPerStop.put(parentId, stop);
				break;
			case STATION:
				// Nothing to index
			}
		}
	}

	@Override
	public void addCalendar(GtfsCalendar calendar,
			DataLoader.SourceContext sourceContext) {
		if (calendar.getId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "service_id"));
			return;
		}
		GtfsCalendar existingCalendar = getCalendar(calendar.getId());
		if (existingCalendar != null) {
			sourceContext.getReportSink()
					.report(new DuplicatedObjectIdError(existingCalendar,
							calendar, calendar.getId(), "service_id"));
			return;
		}
		calendars.put(calendar.getId(), calendar);
	}

	@Override
	public void addCalendarDate(GtfsCalendarDate calendarDate,
			DataLoader.SourceContext sourceContext) {
		if (calendarDate.getCalendarId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "service_id"));
			return;
		}
		// TODO Check for duplicated key (service, date)?
		calendarDates.put(calendarDate.getCalendarId(), calendarDate);
	}

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint,
			DataLoader.SourceContext sourceContext) {
		// Do not add to DAO shape points w/o shape ID
		if (shapePoint.getShapeId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "shape_id"));
			return;
		}
		// Do not add shape points w/o point sequence
		if (shapePoint.getPointSequence() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "shape_pt_sequence"));
			return;
		}
		shapePoints.put(shapePoint.getShapeId(), shapePoint);
	}

	@Override
	public void addTrip(GtfsTrip trip, DataLoader.SourceContext sourceContext) {
		if (trip.getId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "trip_id"));
			return;
		}
		GtfsTrip existingTrip = getTrip(trip.getId());
		if (existingTrip != null) {
			sourceContext.getReportSink().report(new DuplicatedObjectIdError(
					existingTrip, trip, trip.getId(), "trip_id"));
			return;
		}
		trips.put(trip.getId(), trip);
		// Note: we do not report if route and service ID are null here
		if (trip.getRouteId() != null)
			tripsPerRoute.put(trip.getRouteId(), trip);
		if (trip.getServiceId() != null)
			tripsPerCalendar.put(trip.getServiceId(), trip);
	}

	@Override
	public void addStopTime(GtfsStopTime stopTime,
			DataLoader.SourceContext sourceContext) {
		// Do not add times w/o trip ID
		if (stopTime.getTripId() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "trip_id"));
			return;
		}
		// Do not add times w/o stop sequence
		if (stopTime.getStopSequence() == null) {
			sourceContext.getReportSink().report(new MissingObjectIdError(
					sourceContext.getSourceInfo(), "stop_sequence"));
			return;
		}
		// But we add times w/o stops
		stopTimes.put(stopTime.getTripId(), stopTime);
		// TODO: Add index stop->trip, stop->route?
	}

	@Override
	public void close() {
		// Sort stop times by stop sequence
		Multimaps.asMap(stopTimes).values().forEach(times -> Collections
				.sort(times, GtfsStopTime.STOP_SEQ_COMPARATOR));
		// Sort shape points by point sequence
		Multimaps.asMap(shapePoints).values().forEach(points -> Collections
				.sort(points, GtfsShapePoint.POINT_SEQ_COMPARATOR));
	}
}
