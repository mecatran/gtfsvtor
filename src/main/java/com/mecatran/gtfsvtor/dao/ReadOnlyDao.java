package com.mecatran.gtfsvtor.dao;

import java.util.stream.Stream;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsZone;

/**
 * A read-only DAO for GTFS data.
 * 
 * Only objects with valid primary IDs (non-null, and not duplicated), are
 * stored and thus returned by the DAO. For duplicated IDs, the first one is
 * stored only.
 *
 * TODO - Use iterable for the return signature of collection of elements.
 */
public interface ReadOnlyDao {

	public GtfsFeedInfo getFeedInfo();

	public Stream<GtfsAgency> getAgencies();

	public GtfsAgency getAgency(GtfsAgency.Id agencyId);

	public Stream<GtfsRoute> getRoutes();

	public GtfsRoute getRoute(GtfsRoute.Id routeId);

	public Stream<GtfsStop> getStops();

	public GtfsStop getStop(GtfsStop.Id stopId);

	public boolean hasZoneId(GtfsZone.Id zoneId);

	public Stream<GtfsCalendar> getCalendars();

	public GtfsCalendar getCalendar(GtfsCalendar.Id calendarId);

	public Stream<GtfsCalendarDate> getCalendarDates();

	public Stream<GtfsCalendarDate> getCalendarDates(
			GtfsCalendar.Id calendarId);

	public boolean hasShape(GtfsShape.Id shapeId);

	public Stream<GtfsShape.Id> getShapeIds();

	public Stream<GtfsTrip> getTrips();

	public GtfsTrip getTrip(GtfsTrip.Id tripId);

	public Stream<GtfsFrequency> getFrequencies();

	public Stream<GtfsTransfer> getTransfers();

	public GtfsTransfer getTransfer(GtfsStop.Id fromStopId,
			GtfsStop.Id toStopId);

	public GtfsTransfer getTransfer(GtfsStop.Id fromStopId, GtfsStop.Id toStopId,
			GtfsRoute.Id fromRouteId, GtfsRoute.Id toRouteId, GtfsTrip.Id fromTripId,
			GtfsTrip.Id toTripId);

	public Stream<GtfsPathway> getPathways();

	public GtfsPathway getPathway(GtfsPathway.Id pathwayId);

	public Stream<GtfsFareAttribute> getFareAttributes();

	public GtfsFareAttribute getFareAttribute(GtfsFareAttribute.Id fareId);

	public Stream<GtfsFareRule> getRulesOfFare(GtfsFareAttribute.Id fareId);

	public Stream<GtfsLevel> getLevels();

	public GtfsLevel getLevel(GtfsLevel.Id levelId);

	public int getStopTimesCount();

	public int getShapePointsCount();

	public int getFareRulesCount();

}
