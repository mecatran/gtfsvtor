package com.mecatran.gtfsvtor.model;

import java.util.List;

/**
 * A simple holder for a trip, it's associated list of stop times, and a pattern
 * key.
 */
public interface GtfsTripAndTimes {

	public GtfsTrip getTrip();

	public List<GtfsStopTime> getStopTimes();

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
