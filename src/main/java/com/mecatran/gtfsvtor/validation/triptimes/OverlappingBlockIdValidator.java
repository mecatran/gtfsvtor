package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.CalendarIndex.OverlappingCalendarInfo;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.OverlappingBlockIdIssue;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class OverlappingBlockIdValidator implements TripTimesValidator {

	// TODO Handle frequencies when loaded

	private static class BlockInfo {
		List<GtfsTrip> trips = new ArrayList<>();
		Multimap<GtfsLogicalTime, GtfsTrip> tripStartAt = ArrayListMultimap
				.create();
		Multimap<GtfsLogicalTime, GtfsTrip> tripEndAt = ArrayListMultimap
				.create();
	}

	private Map<GtfsBlockId, BlockInfo> blocks;

	@Override
	public void start(Context context) {
		blocks = new HashMap<>();
	}

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		GtfsTrip trip = tripAndTimes.getTrip();
		GtfsBlockId blockId = trip.getBlockId();
		if (blockId == null)
			return; // Skip
		List<GtfsStopTime> stopTimes = tripAndTimes.getStopTimes();
		if (stopTimes.size() < 2)
			return; // Bogus trip
		GtfsStopTime first = stopTimes.get(0);
		GtfsStopTime last = stopTimes.get(stopTimes.size() - 1);
		GtfsLogicalTime depart = first.getDepartureOrArrivalTime();
		GtfsLogicalTime arrive = last.getArrivalOrDepartureTime();
		if (depart == null || arrive == null)
			return; // Bogus trip
		if (depart.equals(arrive))
			return; // Bogus trip, and will fail anyway
		if (arrive.compareTo(depart) < 0)
			return; // Bogus trip
		BlockInfo blockInfo = blocks.computeIfAbsent(blockId,
				b -> new BlockInfo());
		blockInfo.trips.add(trip);
		blockInfo.tripStartAt.put(depart, trip);
		blockInfo.tripEndAt.put(arrive, trip);
	}

	@Override
	public void end(Context context) {
		blocks.entrySet()
				.forEach(e -> check(context, e.getKey(), e.getValue()));

	}

	private void check(Context context, GtfsBlockId blockId,
			BlockInfo blockInfo) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();

		// Get and sort ALL distinct times (depart+arrive)
		List<GtfsLogicalTime> sortedTimes = Stream
				.concat(blockInfo.tripStartAt.keySet().stream(),
						blockInfo.tripEndAt.keySet().stream())
				.distinct().sorted().collect(Collectors.toList());
		Set<GtfsTrip> activeTrips = new HashSet<>();
		// Process each time in order
		for (GtfsLogicalTime time : sortedTimes) {
			/*
			 * First remove the trips that are currently active, if any. That
			 * way a trip ending and starting at the exact same time will not
			 * considered to be overlapping.
			 */
			activeTrips.removeAll(blockInfo.tripEndAt.asMap().getOrDefault(time,
					Collections.emptyList()));
			/*
			 * For each new trip in the active set, check if it overlaps with
			 * any trip currently active (calendar overlap).
			 */
			for (GtfsTrip newTrip : blockInfo.tripStartAt.asMap()
					.getOrDefault(time, Collections.emptyList())) {
				activeLoop: for (GtfsTrip activeTrip : activeTrips) {
					OverlappingCalendarInfo overlap = calIndex.calendarOverlap(
							newTrip.getServiceId(), activeTrip.getServiceId());
					if (overlap == null) {
						// Calendar do not overlap, no problems.
						continue activeLoop;
					}
					// Overlapping block found
					List<GtfsStopTime> stopTimes1 = dao
							.getTripAndTimes(activeTrip.getId()).getStopTimes();
					List<GtfsStopTime> stopTimes2 = dao
							.getTripAndTimes(newTrip.getId()).getStopTimes();
					reportSink.report(new OverlappingBlockIdIssue(blockId,
							activeTrip, newTrip,
							stopTimes1.get(0).getDepartureOrArrivalTime(),
							stopTimes1.get(stopTimes1.size() - 1)
									.getArrivalOrDepartureTime(),
							stopTimes2.get(0).getDepartureOrArrivalTime(),
							stopTimes2.get(stopTimes2.size() - 1)
									.getArrivalOrDepartureTime(),
							overlap));
				}
				activeTrips.add(newTrip);
			}
		}
	}
}
