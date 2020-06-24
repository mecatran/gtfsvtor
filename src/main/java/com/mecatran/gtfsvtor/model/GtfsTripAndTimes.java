package com.mecatran.gtfsvtor.model;

import java.util.Collections;
import java.util.List;

/**
 * A simple holder for a trip, it's associated list of stop times, and a pattern
 * key.
 */
public class GtfsTripAndTimes {

	private GtfsTrip trip;
	private List<GtfsStopTime> stopTimes;
	private Object stopPatternKey;

	public GtfsTripAndTimes(GtfsTrip trip, List<GtfsStopTime> stopTimes,
			Object stopPatternKey) {
		this.trip = trip;
		this.stopTimes = Collections.unmodifiableList(stopTimes);
		this.stopPatternKey = stopPatternKey;
	}

	public GtfsTrip getTrip() {
		return trip;
	}

	public List<GtfsStopTime> getStopTimes() {
		return stopTimes;
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
	public Object getStopPatternKey() {
		return stopPatternKey;
	}
}
