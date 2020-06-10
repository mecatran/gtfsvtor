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

	@Override
	public void addStopTime(GtfsStopTime stopTime) {
		stopTimes.put(stopTime.getTripId(), stopTime);
	}

	@Override
	public void close() {
		// Sort stop times by stop sequence
		Multimaps.asMap(stopTimes).values().forEach(times -> Collections
				.sort(times, GtfsStopTime.STOP_SEQ_COMPARATOR));
	}

	@Override
	public int getStopTimesCount() {
		return stopTimes.size();
	}

	@Override
	public GtfsTripAndTimes getStopTimesOfTrip(GtfsTrip.Id tripId,
			GtfsTrip trip) {
		return new GtfsTripAndTimes(trip,
				Collections.unmodifiableList(stopTimes.get(tripId)));
	}
}
