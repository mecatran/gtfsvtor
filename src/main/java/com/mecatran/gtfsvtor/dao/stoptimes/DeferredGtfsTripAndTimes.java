package com.mecatran.gtfsvtor.dao.stoptimes;

import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

public abstract class DeferredGtfsTripAndTimes implements GtfsTripAndTimes {

	private GtfsTrip trip;

	public DeferredGtfsTripAndTimes(GtfsTrip trip) {
		this.trip = trip;
	}

	@Override
	public GtfsTrip getTrip() {
		return trip;
	}

}
