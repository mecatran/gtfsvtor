package com.mecatran.gtfsvtor.model;

import java.util.List;
import java.util.Optional;

/**
 * A simple holder for a trip, its associated list of stop times, and a pattern
 * key.
 */
public interface GtfsTripAndTimes {

	public GtfsTrip getTrip();

	public List<GtfsStopTime> getStopTimes();

	/**
	 * @return The first stop time in the list, if present.
	 */
	public default Optional<GtfsStopTime> getFirstStopTime() {
		List<GtfsStopTime> stopTimes = getStopTimes();
		if (stopTimes == null || stopTimes.isEmpty())
			return Optional.empty();
		else
			return Optional.of(stopTimes.get(0));
	}

	/**
	 * @return The first stop departure time, if present and defined. Fall back
	 *         to arrival time if defined but departure is not.
	 */
	public default Optional<GtfsLogicalTime> getFirstStopTimeDeparture() {
		Optional<GtfsStopTime> stopTime = getFirstStopTime();
		if (stopTime.isPresent()) {
			return Optional
					.ofNullable(stopTime.get().getDepartureOrArrivalTime());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * @return The last stop time in the list, if present.
	 */
	public default Optional<GtfsStopTime> getLastStopTime() {
		List<GtfsStopTime> stopTimes = getStopTimes();
		if (stopTimes == null || stopTimes.isEmpty())
			return Optional.empty();
		else
			return Optional.of(stopTimes.get(stopTimes.size() - 1));
	}

	/**
	 * @return The last stop arrival time, if presend and defined. Fall back to
	 *         departure time if defined but arrival is not.
	 */
	public default Optional<GtfsLogicalTime> getLastStopTimeArrival() {
		Optional<GtfsStopTime> stopTime = getLastStopTime();
		if (stopTime.isPresent()) {
			return Optional
					.ofNullable(stopTime.get().getArrivalOrDepartureTime());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * @return An opaque object that is guaranteed to be distinct (in regard to
	 *         hashCode/equals) for each distinct stop + shape dist pattern; but
	 *         is not guaranteed to be the same for same pattern. This key can
	 *         be used as key in maps for example. Indeed in some case we could
	 *         include the stop sequence and return distinct keys for the same
	 *         stop pattern + shape dist, if the stop sequence or headsigns. are
	 *         different
	 */
	public Object getStopPatternKey();
}
