package com.mecatran.gtfsvtor.validation.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsRouteType;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DifferentHeadsignsIssue;
import com.mecatran.gtfsvtor.reporting.issues.WrongDropOffPickUpTypeForSplitTripsIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class DropOffPickUpTypeForSplitOrJoinedTripsValidator
		implements DaoValidator {

	@ConfigurableOption
	GtfsRouteType[] routeTypes = 	new GtfsRouteType[]{GtfsRouteType.RAIL};

	/**
	 * Validates that boarding restrictions for split and joined trains are
	 * correctly set.
	 * Boarding restrictions (pickup / drop off type) are set correctly, if
	 * for the stops in common only one of these trips allows pickup (in case
	 * of joined trains) or drop-off (in case of split trains).
	 * <p>
	 * Note that this validation assumes, that split/joined trips have the same service id.
	 *
	 * @param context validation context
	 */
	@Override
	public void validate(Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		List<GtfsRouteType> routeTypesList = Arrays.asList(routeTypes);

		// Process trips route by route to reduce memory usage
		// split trains have at least their two first stops in common, joined
		// at least their last stops. We assume that both trips have the same
		// service id. That means stop id, departure and arrival time
		// match so we collect all split/joined trips by a key representing
		// their first/last to stops and service id.
		dao.getRoutes().forEach(route -> {
			if (!routeTypesList.contains(route.getType())) {
				return;
			}
			ListMultimap<Object, GtfsTrip.Id> tripsPerStartKey = ArrayListMultimap.create();
			ListMultimap<Object, GtfsTrip.Id> tripsPerEndKey = ArrayListMultimap.create();
			dao.getTripsOfRoute(route.getId()).forEach(trip -> {
				List<GtfsStopTime> stopTimesOfTrip = dao.getStopTimesOfTrip(
						trip.getId());
				Object tripStartKey = computeTripKey(trip, stopTimesOfTrip, true);
				tripsPerStartKey.put(tripStartKey, trip.getId());

				Object tripEndKey = computeTripKey(trip, stopTimesOfTrip, false);
				tripsPerEndKey.put(tripEndKey, trip.getId());

			});

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
						if (haveBothDropOffInSameStop(dao.getStopTimesOfTrip(tripId1),
								dao.getStopTimesOfTrip(tripId2))) {
							reportSink.report(
									new WrongDropOffPickUpTypeForSplitTripsIssue(tripId1,
											tripId2, true));
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
						if (haveBothPickupInSameStop(dao.getStopTimesOfTrip(tripId1),
								dao.getStopTimesOfTrip(tripId2))) {
							reportSink.report(
									new WrongDropOffPickUpTypeForSplitTripsIssue(tripId1,
											tripId2, false));
						}

						GtfsTrip trip1 = dao.getTrip(tripId1);
						GtfsTrip trip2 = dao.getTrip(tripId2);
						if (!Objects.equals(trip1.getHeadsign(), trip2.getHeadsign())) {
							reportSink.report(new DifferentHeadsignsIssue(trip1, trip2));
						}
					}
				}
			}
		});
	}

	private boolean haveBothDropOffInSameStop(List<GtfsStopTime> timesTrip1,
			List<GtfsStopTime> timesTrip2) {
		for (int n = 0; n < Math.min(timesTrip1.size(), timesTrip2.size()); n++) {
			GtfsStopTime stopTime1 = timesTrip1.get(n);
			GtfsStopTime stopTime2 = timesTrip2.get(n);
			if (isSameStop(stopTime1, stopTime2)) {
				// At least one of both must have NoDropOff set
				if (!GtfsDropoffType.NO_DROPOFF.equals(stopTime1.getNonNullDropoffType())
						&& !GtfsDropoffType.NO_DROPOFF.equals(stopTime2.getNonNullDropoffType())) {
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
						&& !GtfsPickupType.NO_PICKUP.equals(stopTime2.getNonNullPickupType())) {
					return true;
				}
			} else {
				// No need to compare before join point
				return false;
			}
		}
		return false;
	}

	private boolean isSameStop(GtfsStopTime stopTime1, GtfsStopTime stopTime2) {
		return
				Objects.equals(stopTime1.getArrivalTime(), stopTime2.getArrivalTime())
						&& Objects.equals(stopTime1.getDepartureTime(),
						stopTime2.getDepartureTime()) && Objects.equals(
						stopTime1.getStopId(), stopTime2.getStopId());
	}

	private Object computeTripKey(GtfsTrip trip, List<GtfsStopTime> stopTimes,
			boolean fromStart) {
		// Store references only to save space
		// No need to store route, as we process trips route by route
		List<Object> retval = new ArrayList<>();
		List<GtfsStopTime> subList;
		if (fromStart) {
			subList = stopTimes.subList(0, 2);
		} else {
			subList = stopTimes.subList(stopTimes.size() - 2, stopTimes.size());
		}
		for (GtfsStopTime stopTime : subList) {
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
