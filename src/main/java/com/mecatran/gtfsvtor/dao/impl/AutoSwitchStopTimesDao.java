package com.mecatran.gtfsvtor.dao.impl;

import com.mecatran.gtfsvtor.dao.StopTimesDao;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTrip.Id;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;

/**
 * A rather simple StopTimesDao implementing a kind of multiplexer (decorator)
 * pattern, switching from one implementation to another if needed. Start with
 * the most efficient packing DAO, which rely on (almost) sorted stop times. If
 * an interleaving overflow is detected (stop times are really in random order),
 * we automatically switch to the other implementation, more memory intensive
 * but faster for unsorted data.
 */
public class AutoSwitchStopTimesDao implements StopTimesDao {

	private GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer;
	private PackingStopTimesDao pstDao;
	private PackingUnsortedStopTimesDao pustDao;
	private StopTimesDao currentDao;
	private boolean verbose = false;

	public AutoSwitchStopTimesDao(int maxInterleaving,
			GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer) {
		this.stopIdIndexer = stopIdIndexer;
		pstDao = new PackingStopTimesDao(maxInterleaving, stopIdIndexer)
				.withInterleavingOverflowCallback(this::handleOverflow);
		pustDao = null;
		currentDao = pstDao;
	}

	@Override
	public void addStopTime(GtfsStopTime stopTime) {
		currentDao.addStopTime(stopTime);
	}

	@Override
	public void close() {
		currentDao.close();
		/*
		 * TODO At this stage, we could revert back to the most efficient packed
		 * DAO implementation. This would take a bit of CPU time, but would save
		 * memory (as long as we do not end-up having two full DAO in memory at
		 * the same time, thus a probable need of an "evicting" stream).
		 * Implement this if needed.
		 */
	}

	@Override
	public int getStopTimesCount() {
		return currentDao.getStopTimesCount();
	}

	@Override
	public GtfsTripAndTimes getStopTimesOfTrip(Id tripId, GtfsTrip trip) {
		return currentDao.getStopTimesOfTrip(tripId, trip);
	}

	@Override
	public StopTimesDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		currentDao.withVerbose(verbose);
		return this;
	}

	private boolean handleOverflow(int n) {
		System.out.println(
				"Interleaving stop times overflow detected. Switching to relevant DAO implementation to better handle this.\nThis will increase memory consumption, though.");
		pstDao.withVerbose(false);
		pstDao.close();
		pustDao = new PackingUnsortedStopTimesDao(stopIdIndexer)
				.withVerbose(verbose);
		// Copy over
		pstDao.getStopTimes().forEach(pustDao::addStopTime);
		System.out.println(
				"Copied " + pustDao.getStopTimesCount() + " stop times.");
		// Free memory
		pstDao = null;
		currentDao = pustDao;
		return true;
	}
}
