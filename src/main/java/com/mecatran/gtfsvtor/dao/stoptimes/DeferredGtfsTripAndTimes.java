package com.mecatran.gtfsvtor.dao.stoptimes;

import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

/**
 * Defer the loading of stop times and stop pattern key in abstract method each
 * implementor should implement. Cache the loaded value to prevent having to
 * recompute it several times.
 */
public abstract class DeferredGtfsTripAndTimes implements GtfsTripAndTimes {

	private GtfsTrip trip;
	private List<GtfsStopTime> stopTimes = null;
	private Object stopPatternKey = null;

	public DeferredGtfsTripAndTimes(GtfsTrip trip) {
		this.trip = trip;
	}

	@Override
	public GtfsTrip getTrip() {
		return trip;
	}

	@Override
	public List<GtfsStopTime> getStopTimes() {
		if (stopTimes == null) {
			stopTimes = loadStopTimes();
		}
		return stopTimes;
	}

	@Override
	public Object getStopPatternKey() {
		if (stopPatternKey == null) {
			stopPatternKey = loadStopPatternKey();
		}
		return stopPatternKey;
	}

	abstract List<GtfsStopTime> loadStopTimes();

	abstract Object loadStopPatternKey();
}
