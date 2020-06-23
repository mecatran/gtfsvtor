package com.mecatran.gtfsvtor.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;

/**
 * A list of stop times packed in dedicated arrays.
 * 
 * This implementation is a bit more memory-intensive than the other packed
 * "default" implementation (PackedStopTimes), but can adapt to random stop
 * times (stop times can be added at almost no-cost any time).
 * 
 * At the end of loading, we post-process the packed data to sort entries and
 * intern common time / stop structures.
 */
public class PackedUnsortedStopTimes {

	public interface Context {

		public int indexStopId(GtfsStop.Id stopId);

		public GtfsStop.Id getStopIdIndex(int stopIdIndex);

		public PackedUnsortedTimePattern intern(
				PackedUnsortedTimePattern tData);

		public PackedUnsortedStopPattern intern(
				PackedUnsortedStopPattern sData);
	}

	private static final int INITIAL_SIZE = 10;

	// tdata
	// 24 bits (0..16777215) -> arrival time, ie 4660 h
	private static final long ARRTIME_MASK = 0x0000000000FFFFFFL;
	private static final int ARRTIME_SHIFT = 0;
	// 24 bits (0..16777215) -> departure time, ie 4660 h
	private static final long DEPTIME_MASK = 0x0000FFFFFF000000L;
	private static final int DEPTIME_SHIFT = 24;
	// Allow for up to -24h
	private static final int TIME_SHIFT = 24 * 60 * 60;
	private static final int NULL_TIME = 0xFFFFF;
	// 4 bits (0..15) -> dropoff type
	private static final long DROPOFF_MASK = 0x000F000000000000L;
	private static final int DROPOFF_SHIFT = 48;
	private static final int NULL_DROPOFF = 0xF;
	// 4 bits (0..15) -> pickup type
	private static final long PICKUP_MASK = 0x00F0000000000000L;
	private static final int PICKUP_SHIFT = 52;
	private static final int NULL_PICKUP = 0xF;
	// 2 bits -> time point
	private static final long TIMEPOINT_MASK = 0x0300000000000000L;
	private static final int TIMEPOINT_SHIFT = 56;
	private static final int NULL_TIMEPOINT = 0x3;

	// sdata
	// 32 bits -> stop ID index (could be lower)
	private static final long STOPIDX_MASK = 0x00000000FFFFFFFFL;
	private static final int STOPIDX_SHIFT = 0;
	private static final int NULL_STOPIDX = 0xFFFFFFFF;
	// 32 bits -> stop seq
	private static final long STOPSEQ_MASK = 0xFFFFFFFF00000000L;
	private static final int STOPSEQ_SHIFT = 32;

	private int size;
	private int baseTime;
	private long[] tdata; // times data
	private long[] sdata; // stop and seq data
	private float[] pdata; // shape dist data (can be null)
	private String[] hdata; // headsigns (can be null)

	public PackedUnsortedStopTimes() {
		allocate(INITIAL_SIZE);
	}

	public void addStopTime(Context context, GtfsStopTime stopTime) {
		if (size == tdata.length) {
			grow();
		}
		long tdata = 0L;
		GtfsLogicalTime arrTime = stopTime.getArrivalTime();
		tdata = setData(tdata, ARRTIME_MASK, ARRTIME_SHIFT, arrTime == null
				? NULL_TIME
				: arrTime.getSecondSinceMidnight() + TIME_SHIFT - baseTime);
		GtfsLogicalTime depTime = stopTime.getDepartureTime();
		tdata = setData(tdata, DEPTIME_MASK, DEPTIME_SHIFT, depTime == null
				? NULL_TIME
				: depTime.getSecondSinceMidnight() + TIME_SHIFT - baseTime);
		GtfsDropoffType dropoff = stopTime.getDropoffType().orElse(null);
		tdata = setData(tdata, DROPOFF_MASK, DROPOFF_SHIFT,
				dropoff == null ? NULL_DROPOFF : dropoff.getValue());
		GtfsPickupType pickup = stopTime.getPickupType().orElse(null);
		tdata = setData(tdata, PICKUP_MASK, PICKUP_SHIFT,
				pickup == null ? NULL_PICKUP : pickup.getValue());
		GtfsTimepoint timepoint = stopTime.getTimepoint().orElse(null);
		tdata = setData(tdata, TIMEPOINT_MASK, TIMEPOINT_SHIFT,
				timepoint == null ? NULL_TIMEPOINT : timepoint.getValue());
		this.tdata[size] = tdata;

		long sdata = 0L;
		int stopIndex = stopTime.getStopId() == null ? NULL_STOPIDX
				: context.indexStopId(stopTime.getStopId());
		sdata = setData(sdata, STOPIDX_MASK, STOPIDX_SHIFT, stopIndex);
		sdata = setData(sdata, STOPSEQ_MASK, STOPSEQ_SHIFT,
				stopTime.getStopSequence().getSequence());
		this.sdata[size] = sdata;

		Double shapeDistTraveled = stopTime.getShapeDistTraveled();
		if (shapeDistTraveled != null) {
			if (this.pdata == null) {
				this.pdata = new float[this.tdata.length];
				for (int i = 0; i < size; i++)
					this.pdata[i] = Float.NaN;
			}
			this.pdata[size] = shapeDistTraveled.floatValue();
		} else if (this.pdata != null) {
			this.pdata[size] = Float.NaN;
		}

		String stopHeadsign = stopTime.getStopHeadsign();
		if (stopHeadsign != null) {
			if (this.hdata == null) {
				this.hdata = new String[this.tdata.length];
				// Default to null
			}
			this.hdata[size] = stopHeadsign.intern();
		}

		size++;
	}

	private void grow() {
		// Make this 2 a parameter? Use previous trips to get more insight?
		int len = tdata.length * 2;
		long[] tdata2 = new long[len];
		System.arraycopy(tdata, 0, tdata2, 0, tdata.length);
		tdata = tdata2;
		long[] sdata2 = new long[len];
		System.arraycopy(sdata, 0, sdata2, 0, sdata.length);
		sdata = sdata2;
		if (pdata != null) {
			float[] pdata2 = new float[len];
			System.arraycopy(pdata, 0, pdata2, 0, pdata.length);
			pdata = pdata2;
		}
		if (hdata != null) {
			String[] hdata2 = new String[len];
			System.arraycopy(hdata, 0, hdata2, 0, hdata.length);
			hdata = hdata2;
		}
	}

	public void sort(Context context) {
		List<GtfsStopTime> stopTimes = getStopTimes(null, context);
		Collections.sort(stopTimes, GtfsStopTime.STOP_SEQ_COMPARATOR);
		// Compute baseTime : the first defined time.
		// If not found, fallback to 0. Whatever value is fine.
		for (GtfsStopTime stopTime : stopTimes) {
			if (stopTime.getArrivalTime() != null) {
				baseTime = stopTime.getArrivalTime().getSecondSinceMidnight();
				break;
			}
			if (stopTime.getDepartureTime() != null) {
				baseTime = stopTime.getDepartureTime().getSecondSinceMidnight();
				break;
			}
		}
		if (baseTime < 0)
			baseTime = 0;
		allocate(stopTimes.size());
		size = 0;
		stopTimes.forEach(st -> addStopTime(context, st));
		this.tdata = context.intern(new PackedUnsortedTimePattern(tdata))
				.getTData();
		this.sdata = context.intern(new PackedUnsortedStopPattern(sdata))
				.getSData();
		// TODO Intern shape dist and headsign, if needed
	}

	/*
	 * We could have returned, instead of fat objects, a collection of flyweight
	 * GtfsStopTime, only storing a simple integer index and a reference towards
	 * the packed stop times. Or a custom List<GtfsStopTime> implementation,
	 * with lazy instance creation on get().
	 *
	 * But is this really needed? The streaming stop times validation ensure the
	 * number of calls to getStopTimes() is low, and we need to keep the
	 * full-fat-object implementation anyway for the sort() method.
	 *
	 * "Premature optimization is the root of all evil" --D.E. Knuth
	 */
	public List<GtfsStopTime> getStopTimes(GtfsTrip.Id tripId,
			Context context) {
		List<GtfsStopTime> ret = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			GtfsStopTime.Builder builder = new SimpleGtfsStopTime.Builder();
			builder.withTripId(tripId);
			long tdata = this.tdata[i];
			int arr = getData(tdata, ARRTIME_MASK, ARRTIME_SHIFT);
			if (arr != NULL_TIME)
				builder.withArrivalTime(
						GtfsLogicalTime.getTime(arr - TIME_SHIFT + baseTime));
			int dep = getData(tdata, DEPTIME_MASK, DEPTIME_SHIFT);
			if (dep != NULL_TIME)
				builder.withDepartureTime(
						GtfsLogicalTime.getTime(dep - TIME_SHIFT + baseTime));
			int dropoff = getData(tdata, DROPOFF_MASK, DROPOFF_SHIFT);
			if (dropoff != NULL_DROPOFF)
				builder.withDropoffType(GtfsDropoffType.fromValue(dropoff));
			int pickup = getData(tdata, PICKUP_MASK, PICKUP_SHIFT);
			if (pickup != NULL_PICKUP)
				builder.withPickupType(GtfsPickupType.fromValue(pickup));
			int timepoint = getData(tdata, TIMEPOINT_MASK, TIMEPOINT_SHIFT);
			if (timepoint != NULL_TIMEPOINT)
				builder.withTimepoint(GtfsTimepoint.fromValue(timepoint));
			long sdata = this.sdata[i];
			int stopIndex = getData(sdata, STOPIDX_MASK, STOPIDX_SHIFT);
			if (stopIndex != NULL_STOPIDX) {
				builder.withStopId(context.getStopIdIndex(stopIndex));
			}
			int stopseq = getData(sdata, STOPSEQ_MASK, STOPSEQ_SHIFT);
			builder.withStopSequence(
					GtfsTripStopSequence.fromSequence(stopseq));
			if (this.pdata != null) {
				float shapeDist = this.pdata[i];
				if (!Float.isNaN(shapeDist))
					builder.withShapeDistTraveled(Double.valueOf(shapeDist));
			}
			if (this.hdata != null) {
				builder.withStopHeadsign(this.hdata[i]);
			}
			ret.add(builder.build());
		}
		return ret;
	}

	public int getShapeDistDataSize() {
		return pdata == null ? 0 : pdata.length * 4;
	}

	public int getHeadsignDataSize() {
		return hdata == null ? 0 : hdata.length * 4;
	}

	private void allocate(int n) {
		tdata = new long[n];
		sdata = new long[n];
		pdata = null;
		hdata = null;
	}

	private final int getData(long data, long mask, int shift) {
		int val = (int) ((data & mask) >> shift);
		return val;
	}

	private final long setData(long data, long mask, int shift, int value) {
		long val = ((long) value << shift) & mask;
		data &= ~mask;
		data |= val;
		return data;
	}
}
