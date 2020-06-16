package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.mecatran.gtfsvtor.dao.StopTimesDao;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTrip.Id;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.model.impl.PackedStopPattern;
import com.mecatran.gtfsvtor.model.impl.PackedStopTimes;
import com.mecatran.gtfsvtor.model.impl.PackedTimePattern;
import com.mecatran.gtfsvtor.utils.GenericInterner;

public class PackingStopTimesDao implements StopTimesDao,
		ListPacker.PackerUnpacker<GtfsTrip.Id, GtfsStopTime, PackedStopTimes> {

	@FunctionalInterface
	public static interface AssertListener {
		public void check(List<GtfsStopTime> before, List<GtfsStopTime> after);
	}

	public static class DefaultContext implements PackedStopTimes.Context {
		private GtfsIdIndexer<String, GtfsStop, GtfsStop.Id> stopIdIndexer = new GtfsIdIndexer<>();
		private GenericInterner<PackedTimePattern> tDataInterner = new GenericInterner<>(
				true);
		private GenericInterner<PackedStopPattern> sDataInterner = new GenericInterner<>(
				true);

		@Override
		public int indexStopId(GtfsStop.Id stopId) {
			return stopIdIndexer.index(stopId);
		}

		@Override
		public GtfsStop.Id getStopIdIndex(int stopIdIndex) {
			return stopIdIndexer.unindex(stopIdIndex);
		}

		@Override
		public PackedTimePattern intern(PackedTimePattern tData) {
			return tDataInterner.intern(tData);
		}

		@Override
		public PackedStopPattern intern(PackedStopPattern sdata) {
			return sDataInterner.intern(sdata);
		}
	}

	private ListPacker<GtfsTrip.Id, GtfsStopTime, PackedStopTimes> listPacker;
	private DefaultContext context = new DefaultContext();
	private static AssertListener assertListener = null;
	private boolean verbose = false;

	public PackingStopTimesDao(int maxInterleaving) {
		this.listPacker = new ListPacker<>(this, maxInterleaving);
	}

	public PackingStopTimesDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	@Override
	public void addStopTime(GtfsStopTime stopTime) {
		GtfsTrip.Id tripId = stopTime.getTripId();
		listPacker.push(tripId, stopTime);
	}

	@Override
	public void close() {
		listPacker.close();
		if (verbose) {
			long nStopTimes = listPacker.itemsCount();
			long nTrips = listPacker.groupCount();
			long tripBytes = nTrips * (3 * 8); // 2 pointers, one int
			long tDataBytes = context.tDataInterner.all()
					.mapToInt(ptp -> ptp.getTData().length).sum();
			long sDataBytes = context.sDataInterner.all()
					.mapToInt(ptp -> ptp.getSData().length
							+ (ptp.getHeadsigns() == null ? 0
									: ptp.getHeadsigns().stream()
											.mapToInt(s -> s.length()).sum()))
					.sum();
			long totalBytes = tripBytes + tDataBytes + sDataBytes;
			System.out.println(String.format(Locale.US,
					"Packed %d stop times, %d trips (%dk) in %d time patterns (%dk), %d stop patterns (%dk)",
					nStopTimes, nTrips, tripBytes / 1024,
					context.tDataInterner.size(), tDataBytes / 1024,
					context.sDataInterner.size(), sDataBytes / 1024));
			System.out.println(String.format(Locale.US,
					"Total %dk. Avg bytes: %.2f per stop time, %.2f per trip, %.2f per time pattern, %.2f per stop pattern",
					totalBytes / 1024, totalBytes * 1.0 / nStopTimes,
					totalBytes * 1.0 / nTrips,
					tDataBytes * 1.0 / context.tDataInterner.size(),
					sDataBytes * 1.0 / context.sDataInterner.size()));
		}
	}

	@Override
	public int getStopTimesCount() {
		return listPacker.itemsCount();
	}

	@Override
	public GtfsTripAndTimes getStopTimesOfTrip(Id tripId, GtfsTrip trip) {
		PackedStopTimes pst = listPacker.get(tripId);
		List<GtfsStopTime> stopTimes = pst == null ? Collections.emptyList()
				: pst.getStopTimes(tripId, context);
		return new GtfsTripAndTimes(trip, stopTimes);
	}

	@Override
	public PackedStopTimes pack(GtfsTrip.Id tripId,
			List<GtfsStopTime> stopTimes) {
		Collections.sort(stopTimes, GtfsStopTime.STOP_SEQ_COMPARATOR);
		PackedStopTimes packed = new PackedStopTimes(context, stopTimes);
		if (assertListener != null) {
			assertListener.check(stopTimes,
					packed.getStopTimes(tripId, context));
		}
		return packed;
	}

	@Override
	public List<GtfsStopTime> unpack(GtfsTrip.Id tripId, PackedStopTimes w) {
		return w.getStopTimes(tripId, context);
	}

	/**
	 * @param enable True to enable assert mode: that is check if the
	 *        packed/unpacked stop times are the same as the original ones
	 *        before packing. Enable this only for testing, as this have a large
	 *        impact on performance.
	 */
	public static void setAssertListener(AssertListener assertListener) {
		PackingStopTimesDao.assertListener = assertListener;
	}

}
