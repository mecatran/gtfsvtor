package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsRouteType;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DifferentHeadsignsIssue;
import com.mecatran.gtfsvtor.reporting.issues.WrongDropOffPickUpTypeForSplitTripsIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class DropOffPickUpTypeForSplitOrJoinedTripsValidator
		implements TripTimesValidator {

	@ConfigurableOption
	GtfsRouteType[] routeTypes = new GtfsRouteType[] {GtfsRouteType.RAIL};

	private Map<GtfsTrip.Id, GtfsTripAndTimes> tripAndTripTimesPerTripId = new HashMap<>();
	private GtfsRoute.Id lastRouteId;
	private List<GtfsRouteType> routeTypesList;

	private ListMultimap<Object, GtfsTrip.Id> tripsPerStartKey = ArrayListMultimap.create();
	private ListMultimap<Object, GtfsTrip.Id> tripsPerEndKey = ArrayListMultimap.create();

	@Override
	public void start(Context context) {
		routeTypesList = Arrays.asList(routeTypes);
	}

	/**
	 * Validates that boarding restrictions for split and joined trains are
	 * correctly set.
	 * Boarding restrictions (pickup / drop off type) are set correctly, if
	 * for the stops in common only one of these trips allows pickup (in case
	 * of joined trains) or drop-off (in case of split trains).
	 * <p>
	 * Note that this validation assumes, that split/joined trips have the same service id.
	 *
	 * @param context      validation context
	 * @param tripAndTimes trip and it's stopTimes
	 */
	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		GtfsTrip trip = tripAndTimes.getTrip();
		if (!Objects.equals(lastRouteId, trip.getRouteId())) {
			// Process trips route by route to reduce memory usage
			processTripsOfRoute(context);
			tripsPerStartKey.clear();
			tripsPerEndKey.clear();
			tripAndTripTimesPerTripId.clear();
		}
		lastRouteId = trip.getRouteId();
		// TODO if tripAndTimes would hold route as well, no dao would be needed here
		IndexedReadOnlyDao dao = context.getDao();
		if (!routeTypesList.contains(dao.getRoute(lastRouteId).getType())) {
			return;
		}
		tripAndTripTimesPerTripId.put(trip.getId(), tripAndTimes);

		// split trains have at least their two first stops in common, joined
		// at least their last stops. We assume that both trips have the same
		// service id. That means stop id, departure and arrival time
		// match so we collect all split/joined trips by a key representing
		// their first/last to stops and service id.
		List<GtfsStopTime> stopTimes = tripAndTimes.getStopTimes();
		Object tripStartKey = computeTripKey(trip, stopTimes);
		tripsPerStartKey.put(tripStartKey, trip.getId());
		Object tripEndKey = computeTripKey(trip, Lists.reverse(stopTimes));
		tripsPerEndKey.put(tripEndKey, trip.getId());
	}

	@Override
	public void end(Context context) {
		processTripsOfRoute(context);
	}

	private void processTripsOfRoute(Context context) {
		ReportSink reportSink = context.getReportSink();

		// validate that of split trains, at least one has dop-off none set
		// for stops in common
		for (List<GtfsTrip.Id> splitTripsIds : Multimaps.asMap(
				tripsPerStartKey).values()) {
			/*
			 * This loop is O(n^2), but we expect to be usually max 2 trips with
			 * same start stops at the same time.
			 */
			for (int i = 0; i < splitTripsIds.size(); i++) {
				for (int j = i + 1; j < splitTripsIds.size(); j++) {
					GtfsTrip.Id tripId1 = splitTripsIds.get(i);
					GtfsTrip.Id tripId2 = splitTripsIds.get(j);
					if (haveBothDropOffInSameStop(
							tripAndTripTimesPerTripId.get(tripId1).getStopTimes(),
							tripAndTripTimesPerTripId.get(tripId2).getStopTimes())) {
						reportSink.report(
								new WrongDropOffPickUpTypeForSplitTripsIssue(tripId1, tripId2,
										true));
					}
				}
			}
		}
		// validate that of joined trains, at least one has dop-off none set
		// for stops in common
		for (List<GtfsTrip.Id> joinedTripsIds : Multimaps.asMap(
				tripsPerEndKey).values()) {
			/*
			 * This loop is O(n^2), but we expect to be usually max 2 trips with
			 * same start stops at the same time.
			 */
			for (int i = 0; i < joinedTripsIds.size(); i++) {
				for (int j = i + 1; j < joinedTripsIds.size(); j++) {
					GtfsTrip.Id tripId1 = joinedTripsIds.get(i);
					GtfsTrip.Id tripId2 = joinedTripsIds.get(j);
					if (haveBothPickupInSameStop(
							tripAndTripTimesPerTripId.get(tripId1).getStopTimes(),
							tripAndTripTimesPerTripId.get(tripId2).getStopTimes())) {
						reportSink.report(
								new WrongDropOffPickUpTypeForSplitTripsIssue(tripId1, tripId2,
										false));
					}

					GtfsTrip trip1 = tripAndTripTimesPerTripId.get(tripId1).getTrip();
					GtfsTrip trip2 = tripAndTripTimesPerTripId.get(tripId2).getTrip();
					if (!Objects.equals(trip1.getHeadsign(), trip2.getHeadsign())) {
						reportSink.report(new DifferentHeadsignsIssue(trip1, trip2));
					}
				}
			}
		}
	}

	private boolean haveBothDropOffInSameStop(List<GtfsStopTime> timesTrip1,
			List<GtfsStopTime> timesTrip2) {
		for (int n = 0; n < Math.min(timesTrip1.size(), timesTrip2.size()); n++) {
			GtfsStopTime stopTime1 = timesTrip1.get(n);
			GtfsStopTime stopTime2 = timesTrip2.get(n);
			if (isSameStop(stopTime1, stopTime2)) {
				// At least one of both must have NoDropOff set
				if (!GtfsDropoffType.NO_DROPOFF.equals(
						stopTime1.getNonNullDropoffType())
						&& !GtfsDropoffType.NO_DROPOFF.equals(
						stopTime2.getNonNullDropoffType())) {
					return true;
				}
			} else {
				// No need to compare beyond split point
				return false;
			}
		}
		return false;
	}

	private boolean haveBothPickupInSameStop(List<GtfsStopTime> timesTrip1,
			List<GtfsStopTime> timesTrip2) {
		List<GtfsStopTime> reversedTimesTrip1 = Lists.reverse(timesTrip1);
		List<GtfsStopTime> reversedTimesTrip2 = Lists.reverse(timesTrip2);

		for (int n = 0; n < Math.min(reversedTimesTrip1.size(),
				reversedTimesTrip2.size()); n++) {
			GtfsStopTime stopTime1 = reversedTimesTrip1.get(n);
			GtfsStopTime stopTime2 = reversedTimesTrip2.get(n);
			if (isSameStop(stopTime1, stopTime2)) {
				// At least one of both must have NoDropOff set
				if (!GtfsPickupType.NO_PICKUP.equals(stopTime1.getNonNullPickupType())
						&& !GtfsPickupType.NO_PICKUP.equals(
						stopTime2.getNonNullPickupType())) {
					return true;
				}
			} else {
				// No need to compare before join point
				return false;
			}
		}
		return false;
	}

	/**
	 * Stops are considered same, if their stop_id, arrival and departure time are equal
	 *
	 * @param stopTime1 stop of one trip
	 * @param stopTime2 stop of another trip
	 * @return true, if stops are equal
	 */
	private boolean isSameStop(GtfsStopTime stopTime1, GtfsStopTime stopTime2) {
		return
				Objects.equals(stopTime1.getArrivalTime(), stopTime2.getArrivalTime())
						&& Objects.equals(stopTime1.getDepartureTime(),
						stopTime2.getDepartureTime()) && Objects.equals(
						stopTime1.getStopId(), stopTime2.getStopId());
	}

	/**
	 * Computes trip key consisting of the first two stop times and service id.
	 * This will result same trip keys for split trips, that have same
	 * stops at least at the trip start. For joined trips, reverse the stopTimes
	 * list.
	 *
	 * @param trip      trip
	 * @param stopTimes stopTimes list to compute trip key from
	 * @return trip key
	 */
	private Object computeTripKey(GtfsTrip trip, List<GtfsStopTime> stopTimes) {
		// Store references only to save space
		// No need to store route, as we process trips route by route
		List<Object> retval = new ArrayList<>();
		for (GtfsStopTime stopTime : stopTimes.subList(0, 2)) {
			/*
			 * Adding entities that differs most often at the start of the list will
			 * optimize the List.equals() method, which will be often called in hash
			 * map.
			 */
			retval.add(stopTime.getDepartureTime());
			retval.add(stopTime.getArrivalTime());
			retval.add(stopTime.getStopId());
		}
		retval.add(trip.getServiceId());
		return retval;
	}
}
