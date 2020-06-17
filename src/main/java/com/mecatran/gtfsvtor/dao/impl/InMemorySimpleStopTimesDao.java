package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collections;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.StopTimesDao;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

public class InMemorySimpleStopTimesDao implements StopTimesDao {

	private ListMultimap<GtfsTrip.Id, GtfsStopTime> stopTimes = ArrayListMultimap
			.create();
	private boolean closed = false;

	@Override
	public void addStopTime(GtfsStopTime stopTime) {
		if (closed)
			throw new RuntimeException(
					"Cannot re-open a closed InMemorySimpleStopTimesDao. Implement this if needed.");
		stopTimes.put(stopTime.getTripId(), stopTime);
	}

	@Override
	public void close() {
		closeIfNeeded();
	}

	@Override
	public int getStopTimesCount() {
		closeIfNeeded();
		return stopTimes.size();
	}

	@Override
	public GtfsTripAndTimes getStopTimesOfTrip(GtfsTrip.Id tripId,
			GtfsTrip trip) {
		closeIfNeeded();
		return new GtfsTripAndTimes(trip,
				Collections.unmodifiableList(stopTimes.get(tripId)));
	}

	private void closeIfNeeded() {
		if (closed)
			return;
		// Sort stop times by stop sequence
		Multimaps.asMap(stopTimes).values().forEach(times -> Collections
				.sort(times, GtfsStopTime.STOP_SEQ_COMPARATOR));
		closed = true;
	}
}
