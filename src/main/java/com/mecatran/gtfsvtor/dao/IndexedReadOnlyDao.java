package com.mecatran.gtfsvtor.dao;

import java.util.List;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;

/**
 * An indexed read-only DAO for GTFS data.
 * 
 * Only objects with valid primary IDs (non-null, and not duplicated), are
 * stored and thus returned by the DAO. For duplicated IDs, the first one is
 * stored only.
 */
public interface IndexedReadOnlyDao extends ReadOnlyDao {

	public Stream<GtfsRoute> getRoutesOfAgency(GtfsAgency.Id agencyId);

	public Stream<GtfsStop> getStopsOfType(GtfsStopType stopType);

	public Stream<GtfsStop> getStopsOfStation(GtfsStop.Id station);

	public Stream<GtfsStop> getEntrancesOfStation(GtfsStop.Id station);

	public Stream<GtfsStop> getNodesOfStation(GtfsStop.Id station);

	public Stream<GtfsStop> getBoardingAreasOfStop(GtfsStop.Id stop);

	public List<GtfsStopTime> getStopTimesOfTrip(GtfsTrip.Id tripId);

	public List<GtfsShapePoint> getPointsOfShape(GtfsShape.Id shapeId);

	public Stream<GtfsTrip> getTripsOfRoute(GtfsRoute.Id routeId);

	public Stream<GtfsTrip> getTripsOfCalendar(GtfsCalendar.Id calendarId);

	public Stream<GtfsFrequency> getFrequenciesOfTrip(GtfsTrip.Id tripId);

	public CalendarIndex getCalendarIndex();

	public DaoSpatialIndex getSpatialIndex();

	public LinearGeometryIndex getLinearGeometryIndex();
}
