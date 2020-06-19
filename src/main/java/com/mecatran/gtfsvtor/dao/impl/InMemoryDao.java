package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.DaoSpatialIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.dao.ShapePointsDao;
import com.mecatran.gtfsvtor.dao.StopTimesDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendar.Id;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.model.GtfsZone;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.MissingObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.MultipleFeedInfoError;
import com.mecatran.gtfsvtor.utils.Sextet;

public class InMemoryDao implements IndexedReadOnlyDao, AppendableDao {

	private GtfsFeedInfo feedInfo;
	private Map<GtfsAgency.Id, GtfsAgency> agencies = new HashMap<>();
	private Map<GtfsRoute.Id, GtfsRoute> routes = new HashMap<>();
	private Map<GtfsStop.Id, GtfsStop> stops = new HashMap<>();
	private Set<GtfsZone.Id> zoneIds = new HashSet<>();
	private Map<GtfsCalendar.Id, GtfsCalendar> calendars = new HashMap<>();
	private Map<GtfsTrip.Id, GtfsTrip> trips = new HashMap<>();
	private StopTimesDao stopTimesDao;
	private ShapePointsDao shapePointsDao;
	private ListMultimap<GtfsTrip.Id, GtfsFrequency> frequencies = ArrayListMultimap
			.create();
	private Multimap<GtfsCalendar.Id, GtfsCalendarDate> calendarDates = ArrayListMultimap
			.create();
	private Map<Sextet<GtfsStop.Id, GtfsStop.Id, GtfsRoute.Id, GtfsRoute.Id, GtfsTrip.Id, GtfsTrip.Id>, GtfsTransfer> transfers = new HashMap<>();
	private Map<GtfsPathway.Id, GtfsPathway> pathways = new HashMap<>();
	private Map<GtfsFareAttribute.Id, GtfsFareAttribute> fareAttributes = new HashMap<>();
	private ListMultimap<GtfsFareAttribute.Id, GtfsFareRule> fareRules = ArrayListMultimap
			.create();
	private Map<GtfsLevel.Id, GtfsLevel> levels = new HashMap<>();
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
	private boolean verbose = false;

	public InMemoryDao(boolean disableStopTimesPackingDao,
			int maxStopTimesInterleaving, boolean disableShapePointsPackingDao,
			int maxShapePointsInterleaving) {
		stopTimesDao = disableStopTimesPackingDao
				? new InMemorySimpleStopTimesDao()
				: new PackingStopTimesDao(maxStopTimesInterleaving);
		shapePointsDao = disableShapePointsPackingDao
				? new InMemorySimpleShapePointsDao()
				: new PackingShapePointsDao(maxShapePointsInterleaving);
	}

	public InMemoryDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		stopTimesDao.withVerbose(verbose);
		shapePointsDao.withVerbose(verbose);
		return this;
	}

	@Override
	public GtfsFeedInfo getFeedInfo() {
		return feedInfo;
	}

	@Override
	public Stream<GtfsAgency> getAgencies() {
		return agencies.values().stream();
	}

	@Override
	public GtfsAgency getAgency(GtfsAgency.Id agencyId) {
		return agencies.get(agencyId);
	}

	@Override
	public Stream<GtfsRoute> getRoutes() {
		return routes.values().stream();
	}

	@Override
	public GtfsRoute getRoute(GtfsRoute.Id routeId) {
		return routes.get(routeId);
	}

	@Override
	public Stream<GtfsStop> getStops() {
		return stops.values().stream();
	}

	@Override
	public GtfsStop getStop(GtfsStop.Id stopId) {
		return stops.get(stopId);
	}

	@Override
	public boolean hasZoneId(GtfsZone.Id zoneId) {
		return zoneIds.contains(zoneId);
	}

	@Override
	public Stream<GtfsCalendar> getCalendars() {
		return calendars.values().stream();
	}

	@Override
	public GtfsCalendar getCalendar(GtfsCalendar.Id calendarId) {
		return calendars.get(calendarId);
	}

	@Override
	public Stream<GtfsCalendarDate> getCalendarDates() {
		return calendarDates.values().stream();
	}

	@Override
	public Stream<GtfsCalendarDate> getCalendarDates(Id calendarId) {
		return calendarDates.get(calendarId).stream();
	}

	@Override
	public boolean hasShape(GtfsShape.Id shapeId) {
		return shapePointsDao.hasShape(shapeId);
	}

	@Override
	public Stream<GtfsShape.Id> getShapeIds() {
		return shapePointsDao.getShapeIds();
	}

	@Override
	public Stream<GtfsTrip> getTrips() {
		return trips.values().stream();
	}

	@Override
	public GtfsTrip getTrip(GtfsTrip.Id tripId) {
		return trips.get(tripId);
	}

	@Override
	public Stream<GtfsFrequency> getFrequencies() {
		return frequencies.values().stream();
	}

	@Override
	public int getStopTimesCount() {
		return stopTimesDao.getStopTimesCount();
	}

	@Override
	public int getShapePointsCount() {
		return shapePointsDao.getShapePointsCount();
	}

	@Override
	public int getFareRulesCount() {
		return fareRules.size();
	}

	@Override
	public Stream<GtfsTransfer> getTransfers() {
		return transfers.values().stream();
	}

	@Override
	public GtfsTransfer getTransfer(GtfsStop.Id fromStopId,
			GtfsStop.Id toStopId) {
		return getTransfer(fromStopId, toStopId, null, null, null, null);
	}

	@Override
	public GtfsTransfer getTransfer(GtfsStop.Id fromStopId,
			GtfsStop.Id toStopId, GtfsRoute.Id fromRouteId,
			GtfsRoute.Id toRouteId, GtfsTrip.Id fromTripId,
			GtfsTrip.Id toTripId) {
		return transfers.get(new Sextet<>(fromStopId, toStopId, fromRouteId,
				toRouteId, fromTripId, toTripId));
	}

	@Override
	public Stream<GtfsPathway> getPathways() {
		return pathways.values().stream();
	}

	@Override
	public GtfsPathway getPathway(GtfsPathway.Id pathwayId) {
		return pathways.get(pathwayId);
	}

	@Override
	public Stream<GtfsFareAttribute> getFareAttributes() {
		return fareAttributes.values().stream();
	}

	@Override
	public GtfsFareAttribute getFareAttribute(GtfsFareAttribute.Id fareId) {
		return fareAttributes.get(fareId);
	}

	@Override
	public Stream<GtfsFareRule> getRulesOfFare(GtfsFareAttribute.Id fareId) {
		return fareRules.get(fareId).stream();
	}

	@Override
	public Stream<GtfsLevel> getLevels() {
		return levels.values().stream();
	}

	@Override
	public GtfsLevel getLevel(GtfsLevel.Id levelId) {
		return levels.get(levelId);
	}

	@Override
	public Stream<GtfsRoute> getRoutesOfAgency(GtfsAgency.Id agencyId) {
		return routesPerAgency.get(agencyId).stream();
	}

	@Override
	public Stream<GtfsStop> getStopsOfType(GtfsStopType stopType) {
		return stopsPerType.get(stopType).stream();
	}

	@Override
	public Stream<GtfsStop> getStopsOfStation(GtfsStop.Id station) {
		return stopsPerStation.get(station).stream();
	}

	@Override
	public Stream<GtfsStop> getEntrancesOfStation(GtfsStop.Id station) {
		return entrancesPerStation.get(station).stream();
	}

	@Override
	public Stream<GtfsStop> getNodesOfStation(GtfsStop.Id station) {
		return nodesPerStation.get(station).stream();
	}

	@Override
	public Stream<GtfsStop> getBoardingAreasOfStop(GtfsStop.Id stop) {
		return boardingAreasPerStop.get(stop).stream();
	}

	@Override
	public Stream<GtfsTrip> getTripsOfRoute(GtfsRoute.Id routeId) {
		return tripsPerRoute.get(routeId).stream();
	}

	@Override
	public Stream<GtfsTrip> getTripsOfCalendar(GtfsCalendar.Id calendarId) {
		return tripsPerCalendar.get(calendarId).stream();
	}

	@Override
	public Stream<GtfsFrequency> getFrequenciesOfTrip(GtfsTrip.Id tripId) {
		return frequencies.get(tripId).stream();
	}

	@Override
	public GtfsTripAndTimes getTripAndTimes(GtfsTrip.Id tripId) {
		return stopTimesDao.getStopTimesOfTrip(tripId, getTrip(tripId));
	}

	@Override
	public Stream<GtfsTripAndTimes> getTripsAndTimes() {
		return getRoutes().flatMap(route -> getTripsOfRoute(route.getId())).map(
				trip -> stopTimesDao.getStopTimesOfTrip(trip.getId(), trip));
	}

	@Override
	public List<GtfsShapePoint> getPointsOfShape(GtfsShape.Id shapeId) {
		return shapePointsDao.getPointsOfShape(shapeId)
				.orElse(Collections.emptyList());
	}

	@Override
	public synchronized CalendarIndex getCalendarIndex() {
		// Lazy create the calendar index
		if (calendarIndex == null) {
			long start = System.currentTimeMillis();
			calendarIndex = new InMemoryCalendarIndex(this);
			long end = System.currentTimeMillis();
			if (verbose) {
				System.out.println(
						"Indexed " + calendarIndex.getAllCalendarIds().count()
								+ " calendars in " + (end - start) + "ms");
			}
		}
		return calendarIndex;
	}

	@Override
	public synchronized DaoSpatialIndex getSpatialIndex() {
		// Lazy create the spatial index
		if (spatialIndex == null) {
			long start = System.currentTimeMillis();
			spatialIndex = new InMemoryDaoSpatialIndex(this);
			long end = System.currentTimeMillis();
			if (verbose) {
				System.out.println("Spatial-indexed " + stops.size()
						+ " stops in " + (end - start) + "ms");
			}
		}
		return spatialIndex;
	}

	@Override
	public synchronized LinearGeometryIndex getLinearGeometryIndex() {
		// Lazy create the index
		if (linearGeometryIndex == null) {
			long start = System.currentTimeMillis();
			InMemoryLinearGeometryIndex imlgi = new InMemoryLinearGeometryIndex(
					this);
			linearGeometryIndex = imlgi;
			long end = System.currentTimeMillis();
			if (verbose) {
				System.out.println("Linear-indexed " + imlgi.getPatternCount()
						+ " shape.patterns in " + (end - start) + "ms");
			}
		}
		return linearGeometryIndex;
	}

	@Override
	public void setFeedInfo(GtfsFeedInfo feedInfo,
			SourceContext sourceContext) {
		if (this.feedInfo != null) {
			sourceContext.getReportSink().report(
					new MultipleFeedInfoError(this.feedInfo, feedInfo), null,
					sourceContext.getSourceInfo());
			return;
		}
		this.feedInfo = feedInfo;
	}

	@Override
	public void addAgency(GtfsAgency agency, SourceContext sourceContext) {
		GtfsAgency existingAgency = getAgency(agency.getId());
		if (existingAgency != null) {
			sourceContext.getReportSink()
					.report(new DuplicatedObjectIdError(
							existingAgency.getSourceRef(),
							sourceContext.getSourceRef(), agency.getId(),
							"agency_id"), null, sourceContext.getSourceInfo());
			return;
		}
		agencies.put(agency.getId(), agency);
	}

	@Override
	public void addRoute(GtfsRoute route, SourceContext sourceContext) {
		if (route.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "route_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsRoute existingRoute = getRoute(route.getId());
		if (existingRoute != null) {
			sourceContext.getReportSink()
					.report(new DuplicatedObjectIdError(
							existingRoute.getSourceRef(),
							sourceContext.getSourceRef(), route.getId(),
							"route_id"), null, sourceContext.getSourceInfo());
			return;
		}
		routes.put(route.getId(), route);
		// Note: this will also work for null agency IDs
		routesPerAgency.put(route.getAgencyId(), route);
	}

	@Override
	public void addStop(GtfsStop stop, SourceContext sourceContext) {
		if (stop.getZoneId() != null) {
			// Should we really condider an ID-less stop zone valid?
			zoneIds.add(stop.getZoneId());
		}
		if (stop.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "stop_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsStop existingStop = getStop(stop.getId());
		if (existingStop != null) {
			sourceContext.getReportSink()
					.report(new DuplicatedObjectIdError(
							existingStop.getSourceRef(),
							sourceContext.getSourceRef(), stop.getId(),
							"stop_id"), null, sourceContext.getSourceInfo());
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
			SourceContext sourceContext) {
		if (calendar.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "service_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsCalendar existingCalendar = getCalendar(calendar.getId());
		if (existingCalendar != null) {
			sourceContext.getReportSink().report(new DuplicatedObjectIdError(
					existingCalendar.getSourceRef(), calendar.getSourceRef(),
					calendar.getId(), "service_id"));
			return;
		}
		calendars.put(calendar.getId(), calendar);
	}

	@Override
	public void addCalendarDate(GtfsCalendarDate calendarDate,
			SourceContext sourceContext) {
		if (calendarDate.getCalendarId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "service_id"),
							sourceContext.getSourceInfo());
			return;
		}
		// TODO Check for duplicated key (service, date)?
		calendarDates.put(calendarDate.getCalendarId(), calendarDate);
	}

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint,
			SourceContext sourceContext) {
		// Do not add to DAO shape points w/o shape ID
		if (shapePoint.getShapeId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "shape_id"),
							sourceContext.getSourceInfo());
			return;
		}
		// Do not add shape points w/o point sequence
		if (shapePoint.getPointSequence() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "shape_pt_sequence"),
							sourceContext.getSourceInfo());
			return;
		}
		shapePointsDao.addShapePoint(shapePoint);
	}

	@Override
	public void addTrip(GtfsTrip trip, SourceContext sourceContext) {
		if (trip.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "trip_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsTrip existingTrip = getTrip(trip.getId());
		if (existingTrip != null) {
			sourceContext.getReportSink()
					.report(new DuplicatedObjectIdError(
							existingTrip.getSourceRef(),
							sourceContext.getSourceRef(), trip.getId(),
							"trip_id"), null, sourceContext.getSourceInfo());
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
			SourceContext sourceContext) {
		// Do not add times w/o trip ID
		if (stopTime.getTripId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "trip_id"),
							sourceContext.getSourceInfo());
			return;
		}
		// Do not add times w/o stop sequence
		if (stopTime.getStopSequence() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "stop_sequence"),
							sourceContext.getSourceInfo());
			return;
		}
		// But we add times w/o stops
		stopTimesDao.addStopTime(stopTime);
		// TODO: Add index stop->trip, stop->route?
	}

	@Override
	public void addFrequency(GtfsFrequency frequency,
			SourceContext sourceContext) {
		// Do not add frequency w/o trip ID
		if (frequency.getTripId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "trip_id"),
							sourceContext.getSourceInfo());
			return;
		}
		frequencies.put(frequency.getTripId(), frequency);
	}

	@Override
	public void addTransfer(GtfsTransfer transfer,
			SourceContext sourceContext) {
		// Do not add transfer w/o from/to stop ID
		if (transfer.getFromStopId() == null
				|| transfer.getToStopId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "from_stop_id",
							"to_stop_id"));
			return;
		}
		GtfsTransfer existingTransfer = getTransfer(transfer.getFromStopId(),
				transfer.getToStopId(), transfer.getFromRouteId(),
				transfer.getToRouteId(), transfer.getFromTripId(),
				transfer.getToTripId());
		if (existingTransfer != null) {
			sourceContext.getReportSink().report(new DuplicatedObjectIdError(
					sourceContext.getSourceRef(), existingTransfer.getId(),
					"from_stop_id", "to_stop_id"));
			return;
		}
		Sextet<GtfsStop.Id, GtfsStop.Id, GtfsRoute.Id, GtfsRoute.Id, GtfsTrip.Id, GtfsTrip.Id> id = new Sextet<>(
				transfer.getFromStopId(), transfer.getToStopId(),
				transfer.getFromRouteId(), transfer.getToRouteId(),
				transfer.getFromTripId(), transfer.getToTripId());
		transfers.put(id, transfer);
		// TODO Should we index on from/to stop IDs?
	}

	@Override
	public void addPathway(GtfsPathway pathway, SourceContext sourceContext) {
		// Do not add pathway w/o ID
		if (pathway.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "pathway_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsPathway existingPathway = getPathway(pathway.getId());
		if (existingPathway != null) {
			sourceContext.getReportSink()
					.report(new DuplicatedObjectIdError(
							sourceContext.getSourceRef(),
							existingPathway.getId(), "pathway_id"));
			return;
		}
		pathways.put(pathway.getId(), pathway);
		// TODO Should we index on from/to stop IDs?
	}

	@Override
	public void addFareAttribute(GtfsFareAttribute fareAttribute,
			SourceContext sourceContext) {
		if (fareAttribute.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "fare_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsFareAttribute existingFare = getFareAttribute(
				fareAttribute.getId());
		if (existingFare != null) {
			sourceContext.getReportSink().report(
					new DuplicatedObjectIdError(sourceContext.getSourceRef(),
							existingFare.getId(), "fare_id"),
					sourceContext.getSourceInfo());
			return;
		}
		fareAttributes.put(fareAttribute.getId(), fareAttribute);
	}

	@Override
	public void addFareRule(GtfsFareRule fareRule,
			SourceContext sourceContext) {
		if (fareRule.getFareId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "fare_id"),
							sourceContext.getSourceInfo());
			return;
		}
		fareRules.put(fareRule.getFareId(), fareRule);
	}

	@Override
	public void addLevel(GtfsLevel level, SourceContext sourceContext) {
		// Do not add levels w/o ID
		if (level.getId() == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), "level_id"),
							sourceContext.getSourceInfo());
			return;
		}
		GtfsLevel existingLevel = getLevel(level.getId());
		if (existingLevel != null) {
			sourceContext.getReportSink().report(
					new DuplicatedObjectIdError(sourceContext.getSourceRef(),
							existingLevel.getId(), "level_id"),
					sourceContext.getSourceInfo());
			return;
		}
		levels.put(level.getId(), level);
	}

	@Override
	public void close() {
		stopTimesDao.close();
	}
}
