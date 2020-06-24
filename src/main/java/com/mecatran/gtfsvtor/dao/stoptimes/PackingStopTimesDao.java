package com.mecatran.gtfsvtor.dao.stoptimes;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.dao.packing.GtfsIdIndexer;
import com.mecatran.gtfsvtor.dao.packing.ListPacker;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTrip.Id;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.utils.GenericInterner;

public class PackingStopTimesDao implements StopTimesDao,
		ListPacker.PackerUnpacker<GtfsTrip.Id, GtfsStopTime, PackedStopTimes> {

	@FunctionalInterface
	public static interface AssertListener {
		public void check(List<GtfsStopTime> before, List<GtfsStopTime> after);
	}

	public static class DefaultContext implements PackedStopTimes.Context {
		private GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer;
		private GenericInterner<PackedTimePattern> tDataInterner = new GenericInterner<>(
				true);
		private GenericInterner<PackedStopPattern> sDataInterner = new GenericInterner<>(
				true);

		public DefaultContext(GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer) {
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
	private DefaultContext context;
	private static AssertListener assertListener = null;
	private boolean verbose = false;
	private boolean closed = false;

	public PackingStopTimesDao(int maxInterleaving,
			GtfsIdIndexer.GtfsStopIdIndexer stopIdIndexer) {
		this.listPacker = new ListPacker<>(this, maxInterleaving);
		this.context = new DefaultContext(stopIdIndexer);
	}

	public PackingStopTimesDao withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public PackingStopTimesDao withInterleavingOverflowCallback(
			Function<Integer, Boolean> callback) {
		this.listPacker.withInterleavingOverflowCallback(callback);
		return this;
	}

	@Override
	public void addStopTime(GtfsStopTime stopTime) {
		if (closed)
			throw new RuntimeException(
					"Cannot re-open a closed PackingStopTimesDao. Implement this if needed.");
		GtfsTrip.Id tripId = stopTime.getTripId();
		listPacker.push(tripId, stopTime);
	}

	@Override
	public void close() {
		closeIfNeeded();
	}

	@Override
	public int getStopTimesCount() {
		closeIfNeeded();
		return listPacker.itemsCount();
	}

	@Override
	public GtfsTripAndTimes getStopTimesOfTrip(Id tripId, GtfsTrip trip) {
		closeIfNeeded();
		PackedStopTimes pst = listPacker.get(tripId);
		List<GtfsStopTime> stopTimes = pst == null ? Collections.emptyList()
				: pst.getStopTimes(tripId, context);
		return new GtfsTripAndTimes(trip, stopTimes,
				pst == null ? null : pst.getStopPatternKey());
	}

	public Stream<GtfsStopTime> getStopTimes() {
		closeIfNeeded();
		return listPacker.entries().flatMap(
				e -> e.getValue().getStopTimes(e.getKey(), context).stream());
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

	private void closeIfNeeded() {
		if (closed)
			return;
		listPacker.close();
		if (verbose) {
			long nStopTimes = listPacker.itemsCount();
			long nTrips = listPacker.groupCount();
			long nTimePatterns = context.tDataInterner.size();
			long nStopPatterns = context.sDataInterner.size();
			long tripBytes = nTrips * (3 * 8); // 2 pointers, one int
			long tDataBytes = context.tDataInterner.all()
					.mapToInt(ptp -> ptp.getTDataSize()).sum();
			long sDataBytes = context.sDataInterner.all()
					.mapToInt(ptp -> ptp.getSDataSize()
							+ (ptp.getHeadsigns() == null ? 0
									: ptp.getHeadsigns().stream()
											.mapToInt(s -> s.length()).sum()))
					.sum();
			long totalBytes = tripBytes + tDataBytes + sDataBytes;

			System.out.println(
					"------[ Packing sorted stop times crude memory stats ]-----");
			System.out.println(
					"       What          |    Count   | Total (kB) | Per item  ");
			System.out.println(
					"---------------------+------------+------------+-----------");
			System.out.println(String.format(Locale.US,
					"%20s | %10d | %10d | %10.2f", "Stop times", nStopTimes,
					totalBytes / 1024, totalBytes * 1. / nStopTimes));
			System.out.println(String.format(Locale.US,
					"%20s | %10d | %10d | %10.2f", "Trips", nTrips,
					tripBytes / 1024, tripBytes * 1. / nTrips));
			System.out.println(
					String.format(Locale.US, "%20s | %10d | %10d | %10.2f",
							"Time patterns", nTimePatterns, tDataBytes / 1024,
							tDataBytes * 1. / nTimePatterns));
			System.out.println(
					String.format(Locale.US, "%20s | %10d | %10d | %10.2f",
							"Stop patterns", nStopPatterns, sDataBytes / 1024,
							sDataBytes * 1. / nStopPatterns));
			System.out.println(
					"---------------------+------------+------------+-----------");
		}
		closed = true;
	}
}
