package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.CalendarIndex.OverlappingCalendarInfo;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedTripIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class DuplicatedTripsValidator implements TripTimesValidator {

	@ConfigurableOption(description = "Include direction ID in duplication check")
	private boolean includeDirection = false;

	@ConfigurableOption(description = "Include headsign in duplication check")
	private boolean includeHeadsign = false;

	@ConfigurableOption(description = "Include short name in duplication check")
	private boolean includeTripShortName = false;

	@ConfigurableOption(description = "Include block ID in duplication check")
	private boolean includeBlockId = false;

	private GtfsRoute.Id lastRouteId = null;
	private ListMultimap<Object, GtfsTrip.Id> tripsPerKey = ArrayListMultimap
			.create();

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {

		GtfsTrip trip = tripAndTimes.getTrip();
		List<GtfsStopTime> stopTimes = tripAndTimes.getStopTimes();
		if (!Objects.equals(lastRouteId, trip.getRouteId())) {
			// Process trips route by route to reduce memory usage
			processTripsOfRoute(context);
			tripsPerKey.clear();
		}
		lastRouteId = trip.getRouteId();

		Object tripKey = computeTripKey(trip, stopTimes);
		tripsPerKey.put(tripKey, trip.getId());
	}

	@Override
	public void end(Context context) {
		processTripsOfRoute(context);
	}

	private void processTripsOfRoute(Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();

		for (List<GtfsTrip.Id> identicalTripIds : Multimaps.asMap(tripsPerKey)
				.values()) {
			/*
			 * This loop is O(n^2), but hopefully the number of trips should be
			 * low in each group. And the calendar disjoint check is cached by
			 * the calendar index.
			 */
			for (int i = 0; i < identicalTripIds.size(); i++) {
				for (int j = i + 1; j < identicalTripIds.size(); j++) {
					GtfsTrip trip1 = dao.getTrip(identicalTripIds.get(i));
					GtfsTrip trip2 = dao.getTrip(identicalTripIds.get(j));
					OverlappingCalendarInfo overlap = calIndex.calendarOverlap(
							trip1.getServiceId(), trip2.getServiceId());
					if (overlap == null)
						continue; // No calendar overlap, OK
					// Found a duplicate
					reportSink.report(
							new DuplicatedTripIssue(trip1, trip2, overlap));
				}
			}
		}
	}

	private Object computeTripKey(GtfsTrip trip, List<GtfsStopTime> stopTimes) {
		// Store references only to save space
		// No need to store route, as we process trips route by route
		List<Object> retval = new ArrayList<>();
		/*
		 * Adding entities that differs most often at the start of the list will
		 * optimize the List.equals() method, which will be often called in hash
		 * map.
		 */
		retval.add(stopTimes.size());
		if (includeDirection)
			retval.add(trip.getDirectionId());
		if (includeHeadsign)
			retval.add(trip.getHeadsign());
		if (includeTripShortName)
			retval.add(trip.getShortName());
		if (includeBlockId)
			retval.add(trip.getBlockId());
		for (GtfsStopTime stopTime : stopTimes) {
			retval.add(stopTime.getStopId());
			retval.add(stopTime.getDepartureTime());
			retval.add(stopTime.getArrivalTime());
			// TODO Add option to include other fields
		}
		return retval;
	}

}
