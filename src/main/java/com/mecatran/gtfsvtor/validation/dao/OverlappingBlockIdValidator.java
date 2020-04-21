package com.mecatran.gtfsvtor.validation.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.CalendarIndex.OverlappingCalendarInfo;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsBlockId;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.OverlappingBlockIdIssue;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class OverlappingBlockIdValidator implements DaoValidator {

	// TODO Handle frequencies when loaded

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();

		// Index trips per block ID
		ListMultimap<GtfsBlockId, GtfsTrip> tripsPerBlockId = ArrayListMultimap
				.create();
		dao.getTrips().filter(trip -> trip.getBlockId() != null)
				.forEach(trip -> tripsPerBlockId.put(trip.getBlockId(), trip));

		// Process all trips for each block ID
		for (GtfsBlockId blockId : tripsPerBlockId.asMap().keySet()) {
			List<GtfsTrip> trips = Multimaps.asMap(tripsPerBlockId)
					.get(blockId);
			// First index trips per depart/arrive time
			Multimap<GtfsLogicalTime, GtfsTrip> tripStartAt = ArrayListMultimap
					.create();
			Multimap<GtfsLogicalTime, GtfsTrip> tripEndAt = ArrayListMultimap
					.create();
			for (GtfsTrip trip : trips) {
				List<GtfsStopTime> stopTimes = dao
						.getStopTimesOfTrip(trip.getId());
				if (stopTimes.size() < 2)
					continue; // Bogus trip
				GtfsStopTime first = stopTimes.get(0);
				GtfsStopTime last = stopTimes.get(stopTimes.size() - 1);
				GtfsLogicalTime depart = first.getDepartureOrArrivalTime();
				GtfsLogicalTime arrive = last.getArrivalOrDepartureTime();
				if (depart == null || arrive == null)
					continue; // Bogus trip
				if (depart.equals(arrive))
					continue; // Bogus trip, and will fail anyway
				if (arrive.compareTo(depart) < 0)
					continue; // Bogus trip
				tripStartAt.put(depart, trip);
				tripEndAt.put(arrive, trip);
			}
			// Get and sort ALL distinct times (depart+arrive)
			List<GtfsLogicalTime> sortedTimes = Stream
					.concat(tripStartAt.keySet().stream(),
							tripEndAt.keySet().stream())
					.distinct().sorted().collect(Collectors.toList());
			Set<GtfsTrip> activeTrips = new HashSet<>();
			// Process each time in order
			for (GtfsLogicalTime time : sortedTimes) {
				/*
				 * First remove the trips that are currently active, if any.
				 * That way a trip ending and starting at the exact same time
				 * will not considered to be overlapping.
				 */
				activeTrips.removeAll(tripEndAt.asMap().getOrDefault(time,
						Collections.emptyList()));
				/*
				 * For each new trip in the active set, check if it overlaps
				 * with any trip currently active (calendar overlap).
				 */
				for (GtfsTrip newTrip : tripStartAt.asMap().getOrDefault(time,
						Collections.emptyList())) {
					activeLoop: for (GtfsTrip activeTrip : activeTrips) {
						OverlappingCalendarInfo overlap = calIndex
								.calendarOverlap(newTrip.getServiceId(),
										activeTrip.getServiceId());
						if (overlap == null) {
							// Calendar do not overlap, no problems.
							continue activeLoop;
						}
						// Overlapping block found
						List<GtfsStopTime> stopTimes1 = dao
								.getStopTimesOfTrip(activeTrip.getId());
						List<GtfsStopTime> stopTimes2 = dao
								.getStopTimesOfTrip(newTrip.getId());
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

}
