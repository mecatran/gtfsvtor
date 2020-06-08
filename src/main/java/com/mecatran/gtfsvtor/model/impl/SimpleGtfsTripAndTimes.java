package com.mecatran.gtfsvtor.model.impl;

import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

/**
 * A simple holder for a trip and it's associated list of stop times.
 */
public class SimpleGtfsTripAndTimes implements GtfsTripAndTimes {

	private GtfsTrip trip;

	private List<GtfsStopTime> stopTimes;

	public SimpleGtfsTripAndTimes(GtfsTrip trip, List<GtfsStopTime> stopTimes) {
		this.trip = trip;
		this.stopTimes = Collections.unmodifiableList(stopTimes);
	}

	@Override
	public GtfsTrip getTrip() {
		return trip;
	}

	@Override
	public List<GtfsStopTime> getStopTimes() {
		return stopTimes;
	}
}
