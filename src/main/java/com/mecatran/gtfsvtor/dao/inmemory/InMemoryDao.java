package com.mecatran.gtfsvtor.dao.inmemory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import com.mecatran.gtfsvtor.dao.packing.GtfsIdIndexer;
import com.mecatran.gtfsvtor.dao.shapepoints.AutoSwitchShapePointDao;
import com.mecatran.gtfsvtor.dao.shapepoints.PackingShapePointsDao;
import com.mecatran.gtfsvtor.dao.shapepoints.PackingUnsortedShapePointsDao;
import com.mecatran.gtfsvtor.dao.shapepoints.ShapePointsDao;
import com.mecatran.gtfsvtor.dao.stoptimes.AutoSwitchStopTimesDao;
import com.mecatran.gtfsvtor.dao.stoptimes.PackingStopTimesDao;
import com.mecatran.gtfsvtor.dao.stoptimes.PackingUnsortedStopTimesDao;
import com.mecatran.gtfsvtor.dao.stoptimes.StopTimesDao;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.ShapePointsDaoMode;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions.StopTimesDaoMode;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsAttribution;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendar.Id;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareLegRule;
import com.mecatran.gtfsvtor.model.GtfsFareProduct;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFareTransferRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsLegGroup;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsNetwork;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsObjectWithSourceRef;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopArea;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTranslation;
import com.mecatran.gtfsvtor.model.GtfsTranslationTable;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.model.GtfsZone;
import com.mecatran.gtfsvtor.model.impl.InternedGtfsTranslation;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.MissingObjectIdError;
import com.mecatran.gtfsvtor.reporting.issues.MultipleFeedInfoError;

public class InMemoryDao implements IndexedReadOnlyDao, AppendableDao {

	private GtfsFeedInfo feedInfo;
	private Map<GtfsAgency.Id, GtfsAgency> agencies = new HashMap<>();
	private Map<GtfsRoute.Id, GtfsRoute> routes = new HashMap<>();
	private Set<GtfsNetwork.Id> networkIds = new HashSet<>();
	private Map<GtfsStop.Id, GtfsStop> stops = new HashMap<>();
	private Set<GtfsZone.Id> zoneIds = new HashSet<>();
	private Map<GtfsCalendar.Id, GtfsCalendar> calendars = new HashMap<>();
	private Map<GtfsTrip.Id, GtfsTrip> trips = new HashMap<>();
	private GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer = new GtfsIdIndexer.GtfsStopIdIndexer();
	private StopTimesDao stopTimesDao;
	private ShapePointsDao shapePointsDao;
	private ListMultimap<GtfsTrip.Id, GtfsFrequency> frequencies = ArrayListMultimap
			.create();
	private Multimap<GtfsCalendar.Id, GtfsCalendarDate> calendarDates = ArrayListMultimap
			.create();
	private Map<GtfsTransfer.Id, GtfsTransfer> transfers = new HashMap<>();
	private Map<GtfsPathway.Id, GtfsPathway> pathways = new HashMap<>();
	private Map<GtfsFareAttribute.Id, GtfsFareAttribute> fareAttributes = new HashMap<>();
	private ListMultimap<GtfsFareAttribute.Id, GtfsFareRule> fareRules = ArrayListMultimap
			.create();
	private Map<GtfsFareProduct.Id, GtfsFareProduct> fareProducts = new HashMap<>();
	private Map<GtfsFareLegRule.Id, GtfsFareLegRule> fareLegRules = new HashMap<>();
	private Set<GtfsLegGroup.Id> legGroupIds = new HashSet<>();
	private Map<GtfsFareTransferRule.Id, GtfsFareTransferRule> fareTransferRules = new HashMap<>();
	private Map<GtfsLevel.Id, GtfsLevel> levels = new HashMap<>();
	private Map<GtfsTranslation.Id, GtfsTranslation> translations = new HashMap<>();
	private List<GtfsAttribution> attributions = new ArrayList<>();
	private Map<GtfsAttribution.Id, GtfsAttribution> attributionsPerId = new HashMap<>();
	private Map<GtfsArea.Id, GtfsArea> areas = new HashMap<>();
	private Map<GtfsStopArea.Id, GtfsStopArea> stopAreas = new HashMap<>();
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
	private Multimap<GtfsStop.Id, GtfsArea.Id> areasPerStop = ArrayListMultimap
			.create();
	private Multimap<GtfsArea.Id, GtfsStop.Id> stopsPerArea = ArrayListMultimap
			.create();

	private CalendarIndex calendarIndex = null;
	private DaoSpatialIndex spatialIndex = null;
	private LinearGeometryIndex linearGeometryIndex = null;
	private boolean verbose = false;

	public InMemoryDao(StopTimesDaoMode stopTimesDaoMode,
			int maxStopTimesInterleaving, ShapePointsDaoMode shapePointsDaoMode,
			int maxShapePointsInterleaving) {
		switch (stopTimesDaoMode) {
		case AUTO:
			stopTimesDao = new AutoSwitchStopTimesDao(
					maxShapePointsInterleaving, stopIdIndexer);
			break;
		case PACKED:
			stopTimesDao = new PackingStopTimesDao(maxShapePointsInterleaving,
					stopIdIndexer);
			break;
		case UNSORTED:
			stopTimesDao = new PackingUnsortedStopTimesDao(stopIdIndexer);
			break;
		}
		switch (shapePointsDaoMode) {
		case AUTO:
			shapePointsDao = new AutoSwitchShapePointDao(
					maxShapePointsInterleaving);
			break;
		case PACKED:
			shapePointsDao = new PackingShapePointsDao(
					maxShapePointsInterleaving);
			break;
		case UNSORTED:
			shapePointsDao = new PackingUnsortedShapePointsDao();
			break;
		}
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
	public boolean hasNetworkId(GtfsNetwork.Id networkId) {
		return networkIds.contains(networkId);
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
	public GtfsTransfer getTransfer(GtfsTransfer.Id transferId) {
		return transfers.get(transferId);
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
	public Stream<GtfsFareProduct> getFareProducts() {
		return fareProducts.values().stream();
	}

	@Override
	public GtfsFareProduct getFareProduct(GtfsFareProduct.Id fareProductId) {
		return fareProducts.get(fareProductId);
	}

	@Override
	public Stream<GtfsFareLegRule> getFareLegRules() {
		return fareLegRules.values().stream();
	}

	@Override
	public GtfsFareLegRule getFareLegRule(GtfsFareLegRule.Id fareLegRuleId) {
		return fareLegRules.get(fareLegRuleId);
	}

	@Override
	public boolean hasLegGroupId(GtfsLegGroup.Id legGroupId) {
		return legGroupIds.contains(legGroupId);
	}

	@Override
	public Stream<GtfsFareTransferRule> getFareTransferRules() {
		return fareTransferRules.values().stream();
	}

	@Override
	public GtfsFareTransferRule getFareTransferRule(
			GtfsFareTransferRule.Id fareTransferRuleId) {
		return fareTransferRules.get(fareTransferRuleId);
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
	public Stream<GtfsTranslation> getTranslations() {
		return translations.values().stream();
	}

	@Override
	public GtfsTranslation getTranslation(GtfsTranslationTable tableName,
			String fieldName, Locale language, String fieldValue) {
		GtfsTranslation.Id id = InternedGtfsTranslation.id(tableName, fieldName,
				language, fieldValue);
		return translations.get(id);
	}

	@Override
	public GtfsTranslation getTranslation(GtfsTranslationTable tableName,
			String fieldName, Locale language, String recordId,
			String recordSubId) {
		GtfsTranslation.Id id = InternedGtfsTranslation.id(tableName, fieldName,
				language, recordId, recordSubId);
		return translations.get(id);
	}

	@Override
	public Stream<GtfsAttribution> getAttributions() {
		return attributions.stream();
	}

	@Override
	public GtfsAttribution getAttribution(GtfsAttribution.Id attributionId) {
		return attributionsPerId.get(attributionId);
	}

	@Override
	public Stream<GtfsArea> getAreas() {
		return areas.values().stream();
	}

	@Override
	public GtfsArea getArea(GtfsArea.Id areaId) {
		return areas.get(areaId);
	}

	@Override
	public Stream<GtfsStopArea> getStopAreas() {
		return stopAreas.values().stream();
	}

	@Override
	public GtfsObject<?> getObject(GtfsTranslationTable table, String recordId,
			Optional<String> recordSubId) {
		switch (table) {
		case FEED_INFO:
			return getFeedInfo();
		case AGENCY:
			return getAgency(GtfsAgency.id(recordId));
		case STOPS:
			return getStop(GtfsStop.id(recordId));
		case ROUTES:
			return getRoute(GtfsRoute.id(recordId));
		case TRIPS:
			return getTrip(GtfsTrip.id(recordId));
		case PATHWAYS:
			return getPathway(GtfsPathway.id(recordId));
		case STOP_TIMES:
			if (!recordSubId.isPresent())
				return null;
			int seq;
			try {
				seq = Integer.parseInt(recordSubId.get());
			} catch (NumberFormatException e) {
				// Hide this, rely on the caller to check
				return null;
			}
			GtfsTripAndTimes tt = getTripAndTimes(GtfsTrip.id(recordId));
			if (tt == null)
				return null;
			return tt.getStopTimes().stream()
					.filter(st -> st.getStopSequence().getSequence() == seq)
					.findFirst().orElse(null);
		case LEVELS:
			return getLevel(GtfsLevel.id(recordId));
		case ATTRIBUTIONS:
			return getAttribution(GtfsAttribution.id(recordId));
		default:
			throw new RuntimeException("Unhandled table: " + table);
		}
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
	public Stream<GtfsStop> getStopsOfArea(GtfsArea.Id areaId) {
		return stopsPerArea.get(areaId).stream().map(this::getStop)
				.filter(s -> s != null);
	}

	@Override
	public Stream<GtfsArea> getAreasOfStop(GtfsStop.Id stopId) {
		return areasPerStop.get(stopId).stream().map(this::getArea)
				.filter(a -> a != null);
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
			InMemoryLinearGeometryIndex imlgi = new InMemoryLinearGeometryIndex(
					this, verbose);
			linearGeometryIndex = imlgi;
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
		// Do not add agency conflicting
		// Careful: agency ID is optional
		if (!checkExisting(agency.getId(), getAgency(agency.getId()),
				sourceContext, "agency_id")) {
			return;
		}
		agencies.put(agency.getId(), agency);
	}

	@Override
	public void addRoute(GtfsRoute route, SourceContext sourceContext) {
		// Do not add route w/o ID or conflicting
		if (!checkId(route.getId(), getRoute(route.getId()), sourceContext,
				"route_id")) {
			return;
		}
		routes.put(route.getId(), route);
		// Note: this will also work for null agency IDs
		routesPerAgency.put(route.getAgencyId(), route);
		if (route.getNetworkId().isPresent()) {
			networkIds.add(route.getNetworkId().get());
		}
	}

	@Override
	public void addStop(GtfsStop stop, SourceContext sourceContext) {
		if (stop.getZoneId() != null) {
			// Should we really condider an ID-less stop zone valid?
			zoneIds.add(stop.getZoneId());
		}
		// Do not add stop w/o ID or conflicting
		if (!checkId(stop.getId(), getStop(stop.getId()), sourceContext,
				"stop_id")) {
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
		// Do not add calendar w/o ID or conflicting
		if (!checkId(calendar.getId(), getCalendar(calendar.getId()),
				sourceContext, "service_id")) {
			return;
		}
		calendars.put(calendar.getId(), calendar);
	}

	@Override
	public void addCalendarDate(GtfsCalendarDate calendarDate,
			SourceContext sourceContext) {
		if (!checkNotNullId(calendarDate.getCalendarId(), sourceContext,
				"service_id")) {
			return;
		}
		// TODO Check for duplicated key (service, date)?
		calendarDates.put(calendarDate.getCalendarId(), calendarDate);
	}

	@Override
	public void addShapePoint(GtfsShapePoint shapePoint,
			SourceContext sourceContext) {
		// Do not add to DAO shape points w/o shape ID
		if (!checkNotNullId(shapePoint.getShapeId(), sourceContext,
				"shape_id")) {
			return;
		}
		// Do not add shape points w/o point sequence
		if (!checkNotNullId(shapePoint.getPointSequence(), sourceContext,
				"shape_pt_sequence")) {
			return;
		}
		shapePointsDao.addShapePoint(shapePoint);
	}

	@Override
	public void addTrip(GtfsTrip trip, SourceContext sourceContext) {
		// Do not add trip w/o ID or conflicting
		if (!checkId(trip.getId(), getTrip(trip.getId()), sourceContext,
				"trip_id")) {
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
		if (!checkNotNullId(stopTime.getTripId(), sourceContext, "trip_id")) {
			return;
		}
		// Do not add times w/o stop sequence
		if (!checkNotNullId(stopTime.getStopSequence(), sourceContext,
				"stop_sequence")) {
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
		if (!checkNotNullId(frequency.getTripId(), sourceContext, "trip_id")) {
			return;
		}
		frequencies.put(frequency.getTripId(), frequency);
	}

	@Override
	public void addTransfer(GtfsTransfer transfer,
			SourceContext sourceContext) {
		// Do not add transfer missing ID or conflicting
		if (!checkId(transfer.getId(), getTransfer(transfer.getId()),
				sourceContext, "from_stop_id", "to_stop_id", "from_route_id",
				"to_route_id", "from_trip_id", "to_trip_id")) {
			return;
		}
		transfers.put(transfer.getId(), transfer);
		// TODO Should we index on from/to stop IDs?
	}

	@Override
	public void addPathway(GtfsPathway pathway, SourceContext sourceContext) {
		// Do not add pathway w/o ID or conflicting
		if (!checkId(pathway.getId(), getPathway(pathway.getId()),
				sourceContext, "pathway_id")) {
			return;
		}
		pathways.put(pathway.getId(), pathway);
		// TODO Should we index on from/to stop IDs?
	}

	@Override
	public void addFareAttribute(GtfsFareAttribute fareAttribute,
			SourceContext sourceContext) {
		// Do not add fare attribute w/o ID or conflicting
		if (!checkId(fareAttribute.getId(),
				getFareAttribute(fareAttribute.getId()), sourceContext,
				"fare_id")) {
			return;
		}
		fareAttributes.put(fareAttribute.getId(), fareAttribute);
	}

	@Override
	public void addFareRule(GtfsFareRule fareRule,
			SourceContext sourceContext) {
		if (!checkNotNullId(fareRule.getFareId(), sourceContext, "fare_id")) {
			return;
		}
		fareRules.put(fareRule.getFareId(), fareRule);
	}

	@Override
	public void addFareProduct(GtfsFareProduct fareProduct,
			SourceContext sourceContext) {
		if (!checkId(fareProduct.getId(), getFareProduct(fareProduct.getId()),
				sourceContext, "fare_product_id")) {
			return;
		}
		fareProducts.put(fareProduct.getId(), fareProduct);
	}

	@Override
	public void addFareLegRule(GtfsFareLegRule fareLegRule,
			SourceContext sourceContext) {
		if (!checkId(fareLegRule.getId(), getFareLegRule(fareLegRule.getId()),
				sourceContext, "network_id", "from_area_id", "to_area_id",
				"fare_product_id")) {
			return;
		}
		fareLegRules.put(fareLegRule.getId(), fareLegRule);
		if (fareLegRule.getLegGroupId().isPresent()) {
			legGroupIds.add(fareLegRule.getLegGroupId().get());
		}
		// TODO Index leg rule on various fields for checks
	}

	@Override
	public void addFareTransferRule(GtfsFareTransferRule fareTransferRule,
			SourceContext sourceContext) {
		if (!checkId(fareTransferRule.getId(),
				getFareTransferRule(fareTransferRule.getId()), sourceContext,
				"from_leg_group_id", "to_leg_group_id", "fare_product_id",
				"transfer_count", "duration_limit")) {
			return;
		}
		fareTransferRules.put(fareTransferRule.getId(), fareTransferRule);
		// TODO Index transfer rule on various fields for checks
	}

	@Override
	public void addLevel(GtfsLevel level, SourceContext sourceContext) {
		// Do not add levels w/o ID or conflicting
		if (!checkId(level.getId(), getLevel(level.getId()), sourceContext,
				"level_id")) {
			return;
		}
		levels.put(level.getId(), level);
	}

	@Override
	public void addTranslation(GtfsTranslation translation,
			SourceContext sourceContext) {
		// Do not add translation w/o ID or conflicting
		if (!checkId(translation.getId(), translations.get(translation.getId()),
				sourceContext, "table_name", "field_name", "language",
				"record_id", "record_sub_id", "field_value")) {
			return;
		}
		translations.put(translation.getId(), translation);
	}

	@Override
	public void addAttribution(GtfsAttribution attribution,
			SourceContext sourceContext) {
		// Always add to list even if ID collide (ID is optional)
		attributions.add(attribution);
		// Check for duplicated ID
		Optional<GtfsAttribution.Id> oid = attribution.getId();
		if (oid.isPresent()) {
			if (!checkExisting(oid.get(), getAttribution(oid.get()),
					sourceContext, "attribution_id")) {
				return;
			}
			// Add to map
			attributionsPerId.put(oid.get(), attribution);
		}
	}

	@Override
	public void addArea(GtfsArea area, SourceContext sourceContext) {
		// Do not add areas w/o ID or conflicting
		if (!checkId(area.getId(), getArea(area.getId()), sourceContext,
				"area_id")) {
			return;
		}
		areas.put(area.getId(), area);
	}

	@Override
	public void addStopArea(GtfsStopArea stopArea,
			SourceContext sourceContext) {
		// Do not add stop_area w/o IDs (area_id or stop_id) or conflicting
		if (!checkId(stopArea.getId(), stopAreas.get(stopArea.getId()),
				sourceContext, "area_id", "stop_id")) {
			return;
		}
		stopAreas.put(stopArea.getId(), stopArea);
		stopsPerArea.put(stopArea.getAreaId(), stopArea.getStopId());
		areasPerStop.put(stopArea.getStopId(), stopArea.getAreaId());
	}

	@Override
	public void close() {
		stopTimesDao.close();
	}

	/* Check if ID is not null and existing object is null */
	private boolean checkId(GtfsId<?, ?> id,
			GtfsObjectWithSourceRef existingObject, SourceContext sourceContext,
			String... idFieldNames) {
		if (!checkNotNullId(id, sourceContext, idFieldNames)) {
			return false;
		}
		if (!checkExisting(id, existingObject, sourceContext, idFieldNames)) {
			return false;
		}
		return true;
	}

	/* Check if ID is not null, report error if not */
	private boolean checkNotNullId(Object id, SourceContext sourceContext,
			String... idFieldNames) {
		if (id == null) {
			sourceContext.getReportSink()
					.report(new MissingObjectIdError(
							sourceContext.getSourceRef(), idFieldNames),
							sourceContext.getSourceInfo());
			return false;
		} else {
			return true;
		}
	}

	/* Check if existing object is null, report error if not */
	private boolean checkExisting(GtfsId<?, ?> id,
			GtfsObjectWithSourceRef existingObject, SourceContext sourceContext,
			String... idFieldNames) {
		if (existingObject != null) {
			sourceContext.getReportSink().report(
					new DuplicatedObjectIdError(existingObject.getSourceRef(),
							sourceContext.getSourceRef(), id, idFieldNames),
					null, sourceContext.getSourceInfo());
			return false;
		} else {
			return true;
		}
	}
}
