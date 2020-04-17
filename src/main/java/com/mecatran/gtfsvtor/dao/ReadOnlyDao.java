package com.mecatran.gtfsvtor.dao;

import java.util.Collection;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
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

	public Collection<GtfsAgency> getAgencies();

	public GtfsAgency getAgency(GtfsAgency.Id agencyId);

	public Collection<GtfsRoute> getRoutes();

	public GtfsRoute getRoute(GtfsRoute.Id routeId);

	public Collection<GtfsStop> getStops();

	public GtfsStop getStop(GtfsStop.Id stopId);

	public boolean hasZoneId(GtfsZone.Id zoneId);

	public Collection<GtfsCalendar> getCalendars();

	public GtfsCalendar getCalendar(GtfsCalendar.Id calendarId);

	public Collection<GtfsCalendarDate> getCalendarDates();

	public Collection<GtfsCalendarDate> getCalendarDates(
			GtfsCalendar.Id calendarId);

	public boolean hasShape(GtfsShape.Id shapeId);

	public Collection<GtfsShape.Id> getShapeIds();

	public Collection<GtfsTrip> getTrips();

	public GtfsTrip getTrip(GtfsTrip.Id tripId);

	public Collection<GtfsFrequency> getFrequencies();

	public Collection<GtfsTransfer> getTransfers();

	public GtfsTransfer getTransfer(GtfsStop.Id fromStopId,
			GtfsStop.Id toStopId);

	public Collection<GtfsFareAttribute> getFareAttributes();

	public GtfsFareAttribute getFareAttribute(GtfsFareAttribute.Id fareId);

	public Collection<GtfsFareRule> getRulesOfFare(GtfsFareAttribute.Id fareId);

	public int getStopTimesCount();

	public int getShapePointsCount();

	public int getFareRulesCount();

}
