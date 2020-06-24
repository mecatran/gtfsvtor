package com.mecatran.gtfsvtor.dao.stoptimes;

import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

public interface StopTimesDao {

	public void addStopTime(GtfsStopTime stopTime);

	public void close();

	public int getStopTimesCount();

	public GtfsTripAndTimes getStopTimesOfTrip(GtfsTrip.Id tripId,
			GtfsTrip trip);

	public default StopTimesDao withVerbose(boolean verbose) {
		return this;
	}
}
