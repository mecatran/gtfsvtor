package com.mecatran.gtfsvtor.validation.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.TripTransferDifferentCalendarError;
import com.mecatran.gtfsvtor.reporting.issues.TripTransferDisjointCalendarError;
import com.mecatran.gtfsvtor.reporting.issues.TripTransferTooLargeDistanceError;
import com.mecatran.gtfsvtor.reporting.issues.TripTransferInvalidDurationError;
import com.mecatran.gtfsvtor.utils.Pair;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class TripTransferValidator implements DaoValidator {

	@ConfigurableOption(description = "Distance between trip transfer stops above which an error is generated")
	private double maxStopDistanceMeters = 100.0;

	@ConfigurableOption(description = "Transfer duration between trips above which an error is generated")
	private int maxTransferDurationSeconds = 600;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();

		// Map 1 from trip to N to trips
		SetMultimap<GtfsTrip, Pair<GtfsTransfer, GtfsTrip>> fromOneToNContinuations = HashMultimap
				.create();
		// Map 1 to trip to N from trips
		SetMultimap<GtfsTrip, Pair<GtfsTransfer, GtfsTrip>> toOneFromNContinuations = HashMultimap
				.create();

		dao.getTransfers()
				.filter(t -> t.getNonNullType() == GtfsTransferType.TRIP_IN_SEAT
						|| t.getNonNullType() == GtfsTransferType.TRIP_ALIGHT)
				.forEach(t -> {
					GtfsTrip fromTrip = dao.getTrip(t.getFromTripId());
					GtfsTrip toTrip = dao.getTrip(t.getToTripId());
					if (fromTrip == null || toTrip == null) {
						// Already checked in streaming validator
						return;
					}
					checkTransfer(context, t, fromTrip, toTrip);
					// Store 1-to-N and N-to-1 continuation for later on
					fromOneToNContinuations.put(fromTrip,
							new Pair<>(t, toTrip));
					toOneFromNContinuations.put(toTrip,
							new Pair<>(t, fromTrip));
				});
		for (Map.Entry<GtfsTrip, Collection<Pair<GtfsTransfer, GtfsTrip>>> kv : fromOneToNContinuations
				.asMap().entrySet()) {
			checkTripContinuation(context, kv.getValue(), "to_trip_id");
		}
		for (Map.Entry<GtfsTrip, Collection<Pair<GtfsTransfer, GtfsTrip>>> kv : toOneFromNContinuations
				.asMap().entrySet()) {
			checkTripContinuation(context, kv.getValue(), "frm_trip_id");
		}
	}

	private void checkTransfer(DaoValidator.Context context,
			GtfsTransfer transfer, GtfsTrip fromTrip, GtfsTrip toTrip) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();
		// Check if both trips calendars do overlap at least one day
		// TODO Handle over-the-clock transfers?
		if (calIndex.calendarOverlap(fromTrip.getServiceId(),
				toTrip.getServiceId()) == null) {
			reportSink.report(new TripTransferDisjointCalendarError(transfer,
					fromTrip, toTrip, dao.getCalendar(fromTrip.getServiceId()),
					dao.getCalendar(toTrip.getServiceId())));
		}
		// Check transfer between trips: duration and distance
		GtfsTripAndTimes fromTimes = dao.getTripAndTimes(fromTrip.getId());
		GtfsTripAndTimes toTimes = dao.getTripAndTimes(toTrip.getId());
		if (fromTimes != null && toTimes != null) {
			Optional<GtfsStopTime> lastFromStopTimeOpt = fromTimes
					.getLastStopTime();
			Optional<GtfsStopTime> firstToStopTimeOpt = toTimes
					.getFirstStopTime();
			if (lastFromStopTimeOpt.isPresent()
					&& firstToStopTimeOpt.isPresent()) {
				GtfsStopTime lastFromStopTime = lastFromStopTimeOpt.get();
				GtfsStopTime firstToStopTime = firstToStopTimeOpt.get();
				// Check duration between two trips
				GtfsLogicalTime lastFromStopTimeArr = lastFromStopTime
						.getArrivalOrDepartureTime();
				GtfsLogicalTime firstToStopTimeDep = firstToStopTime
						.getDepartureOrArrivalTime();
				if (lastFromStopTimeArr != null && firstToStopTimeDep != null) {
					int delta = firstToStopTimeDep.getSecondSinceMidnight()
							- lastFromStopTimeArr.getSecondSinceMidnight();
					if (delta < 0 || delta > maxTransferDurationSeconds) {
						reportSink.report(new TripTransferInvalidDurationError(
								transfer, fromTrip, toTrip, lastFromStopTimeArr,
								firstToStopTimeDep,
								maxTransferDurationSeconds));
					}
				}
				// Check distance between last/first stop of trips
				GtfsStop fromStop = dao.getStop(lastFromStopTime.getStopId());
				GtfsStop toStop = dao.getStop(firstToStopTime.getStopId());
				if (fromStop != null && toStop != null) {
					Optional<GeoCoordinates> p1 = fromStop
							.getValidCoordinates();
					Optional<GeoCoordinates> p2 = toStop.getValidCoordinates();
					if (p1.isPresent() && p2.isPresent()) {
						double d = Geodesics.fastDistanceMeters(p1.get(),
								p2.get());
						if (d > maxStopDistanceMeters) {
							reportSink.report(
									new TripTransferTooLargeDistanceError(
											transfer, fromTrip, toTrip,
											fromStop, toStop, d,
											maxStopDistanceMeters));
						}
					}
				}
			}
		}
	}

	private void checkTripContinuation(DaoValidator.Context context,
			Collection<Pair<GtfsTransfer, GtfsTrip>> transferAndTrips,
			String transferField) {
		Set<GtfsTransfer> transfers = transferAndTrips.stream()
				.map(p -> p.getFirst()).collect(Collectors.toSet());
		Set<GtfsTrip> trips = transferAndTrips.stream().map(p -> p.getSecond())
				.collect(Collectors.toSet());
		ReportSink reportSink = context.getReportSink();
		if (trips.size() < 2)
			return;
		Set<GtfsCalendar.Id> calendarIds = trips.stream()
				.map(t -> t.getServiceId()).filter(id -> id != null)
				.collect(Collectors.toSet());
		if (calendarIds.size() >= 2) {
			reportSink.report(new TripTransferDifferentCalendarError(transfers,
					trips, calendarIds, transferField));
		}
	}

	// TODO Check: Trips may be linked together as part of multiple distinct
	// continuations, provided that the trip.service_id MUST NOT overlap on any
	// day of service.
}
