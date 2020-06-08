package com.mecatran.gtfsvtor.model;

import java.util.List;

/**
 * A simple holder for a trip and it's associated list of stop times.
 */
public interface GtfsTripAndTimes {

	public GtfsTrip getTrip();

	public List<GtfsStopTime> getStopTimes();

	/*
	 * TODO Add indexed getter to stop times fields that do not need to
	 * instantiate a full list
	 */
}
