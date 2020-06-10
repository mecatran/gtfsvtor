package com.mecatran.gtfsvtor.model;

import java.util.Collections;
import java.util.List;

/**
 * A simple holder for a trip and it's associated list of stop times.
 */
public class GtfsTripAndTimes {

	private GtfsTrip trip;
	private List<GtfsStopTime> stopTimes;

	public GtfsTripAndTimes(GtfsTrip trip, List<GtfsStopTime> stopTimes) {
		this.trip = trip;
		this.stopTimes = Collections.unmodifiableList(stopTimes);
	}

	public GtfsTrip getTrip() {
		return trip;
	}

	public List<GtfsStopTime> getStopTimes() {
		return stopTimes;
	}
}
