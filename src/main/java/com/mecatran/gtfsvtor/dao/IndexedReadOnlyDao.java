package com.mecatran.gtfsvtor.dao;

import java.util.List;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

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

	/**
	 * Do not use this method in a DaoValidator, if you plan to scan the stop
	 * times of all trips. Better use the streaming capabilities of a
	 * TripTimesValidator implementation when possible. But you can safely use
	 * this method for cherry-picking the stop times of a few selected trips.
	 *
	 * @param tripId
	 * @return The list of stop times for this trip.
	 */
	public GtfsTripAndTimes getTripAndTimes(GtfsTrip.Id tripId);

	/**
	 * @return A stream of trip with their stop times, ordered by route.
	 */
	public Stream<GtfsTripAndTimes> getTripsAndTimes();

	public List<GtfsShapePoint> getPointsOfShape(GtfsShape.Id shapeId);

	public Stream<GtfsTrip> getTripsOfRoute(GtfsRoute.Id routeId);

	public Stream<GtfsTrip> getTripsOfCalendar(GtfsCalendar.Id calendarId);

	public Stream<GtfsFrequency> getFrequenciesOfTrip(GtfsTrip.Id tripId);

	public Stream<GtfsStop> getStopsOfArea(GtfsArea.Id areaId);

	public Stream<GtfsArea> getAreasOfStop(GtfsStop.Id stopId);

	public CalendarIndex getCalendarIndex();

	public DaoSpatialIndex getSpatialIndex();

	public LinearGeometryIndex getLinearGeometryIndex();
}
