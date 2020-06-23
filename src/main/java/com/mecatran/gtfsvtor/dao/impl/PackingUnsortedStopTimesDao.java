package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mecatran.gtfsvtor.dao.StopTimesDao;
import com.mecatran.gtfsvtor.dao.impl.GtfsIdIndexer.GtfsStopIdIndexer;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTrip.Id;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.model.impl.PackedUnsortedStopTimes;

public class PackingUnsortedStopTimesDao implements StopTimesDao {

	private Map<GtfsTrip.Id, PackedUnsortedStopTimes> stopTimes = new HashMap<>();
	private PackedUnsortedStopTimes.Context context;
	private int nStopTimes = 0;
	private boolean verbose = false;
	private boolean closed = false;

	public static class DefaultContext
			implements PackedUnsortedStopTimes.Context {
		private GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer;

		public DefaultContext(GtfsStopIdIndexer stopIdIndexer) {
			this.stopIdIndexer = stopIdIndexer;
		}

		@Override
		public int indexStopId(GtfsStop.Id stopId) {
			return stopIdIndexer.index(stopId);
		}

		@Override
		public GtfsStop.Id getStopIdIndex(int stopIdIndex) {
			return stopIdIndexer.unindex(stopIdIndex);
		}
	}

	public PackingUnsortedStopTimesDao(
			GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer) {
		context = new DefaultContext(stopIdIndexer);
	}

	public PackingUnsortedStopTimesDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	@Override
	public void addStopTime(GtfsStopTime stopTime) {
		if (closed)
			throw new RuntimeException(
					"Cannot re-open a closed PackingUnsortedStopTimesDao. Implement this if needed.");
		nStopTimes++;
		PackedUnsortedStopTimes st = stopTimes.computeIfAbsent(
				stopTime.getTripId(), tid -> new PackedUnsortedStopTimes());
		st.addStopTime(context, stopTime);
	}

	@Override
	public void close() {
		closeIfNeeded();
	}

	@Override
	public int getStopTimesCount() {
		return nStopTimes;
	}

	@Override
	public GtfsTripAndTimes getStopTimesOfTrip(Id tripId, GtfsTrip trip) {
		closeIfNeeded();
		PackedUnsortedStopTimes pst = stopTimes.get(tripId);
		return new GtfsTripAndTimes(trip, pst == null ? Collections.emptyList()
				: pst.getStopTimes(tripId, context));
	}

	private void closeIfNeeded() {
		if (closed)
			return;
		stopTimes.values().forEach(st -> st.sort(context));
		closed = true;
	}
}
