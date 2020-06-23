package com.mecatran.gtfsvtor.model.impl;

import java.util.Optional;

import com.mecatran.gtfsvtor.model.GtfsDropoffType;
import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsPickupType;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTimepoint;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;

/**
 * Optimized GtfsStopTime for size. Store only two pointers (trip ID, shared
 * stop ID/seq/headsign/shapedist) and a long (times, flags), thus only three
 * 64bits values on most machines (or maybe even 32bits for both pointers with
 * compressed OOP trick, if the heap is less than 32G), plus object overhead.
 *
 * TODO Remove this class, should not be needed anymore (see
 * PackedUnsortedStopTimes)
 */
@Deprecated
public class SmallGtfsStopTime implements GtfsStopTime {

	// 24 bits (0..16777215) -> arrival time, ie 4660 h
	private static final long ARRTIME_MASK = 0x0000000000FFFFFFL;
	private static final int ARRTIME_SHIFT = 0;
	private static final int NULL_TIME = 0xFFFFF;
	// 24 bits (0..16777215) -> departure time, ie 4660 h
	private static final long DEPTIME_MASK = 0x0000FFFFFF000000L;
	private static final int DEPTIME_SHIFT = 24;
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

	// Trip ID will be interned by the builder
	private GtfsTrip.Id tripId;
	// Often reused
	private StopIdSeqAndHeadsign stopIdSeqAndHeadsign;
	private long data; // times and flags

	public GtfsTrip.Id getTripId() {
		return tripId;
	}

	public GtfsLogicalTime getDepartureTime() {
		int ssm = getData(DEPTIME_MASK, DEPTIME_SHIFT);
		return ssm == NULL_TIME ? null : GtfsLogicalTime.getTime(ssm);
	}

	public GtfsLogicalTime getArrivalTime() {
		int ssm = getData(ARRTIME_MASK, ARRTIME_SHIFT);
		return ssm == NULL_TIME ? null : GtfsLogicalTime.getTime(ssm);
	}

	public GtfsStop.Id getStopId() {
		return stopIdSeqAndHeadsign.getStopId();
	}

	public GtfsTripStopSequence getStopSequence() {
		return stopIdSeqAndHeadsign.getStopSequence();
	}

	public String getStopHeadsign() {
		return stopIdSeqAndHeadsign.getStopHeadsign();
	}

	public Optional<GtfsPickupType> getPickupType() {
		int pt = getData(PICKUP_MASK, PICKUP_SHIFT);
		return pt == NULL_PICKUP ? Optional.empty()
				: Optional.of(GtfsPickupType.fromValue(pt));
	}

	public Optional<GtfsDropoffType> getDropoffType() {
		int pt = getData(DROPOFF_MASK, DROPOFF_SHIFT);
		return pt == NULL_DROPOFF ? Optional.empty()
				: Optional.of(GtfsDropoffType.fromValue(pt));
	}

	public Double getShapeDistTraveled() {
		return stopIdSeqAndHeadsign.getShapeDistTraveled();
	}

	public Optional<GtfsTimepoint> getTimepoint() {
		int tp = getData(TIMEPOINT_MASK, TIMEPOINT_SHIFT);
		return tp == NULL_TIMEPOINT ? Optional.empty()
				: Optional.of(GtfsTimepoint.fromValue(tp));
	}

	@Override
	public String toString() {
		return "StopTime{trip=" + tripId + ", arr=" + getArrivalTime()
				+ ", dep=" + getDepartureTime() + ", stop=" + getStopId() + "}";
	}

	private int getData(long mask, int shift) {
		int val = (int) ((this.data & mask) >> shift);
		return val;
	}

	private void setData(long mask, int shift, int value) {
		long val = ((long) value << shift) & mask;
		this.data &= ~mask;
		this.data |= val;
	}

	@Deprecated
	public static class Builder implements GtfsStopTime.Builder {
		private SmallGtfsStopTime stopTime;
		private StopIdSeqAndHeadsign.Builder stopStuffBuilder;

		public Builder() {
			stopTime = new SmallGtfsStopTime();
			stopStuffBuilder = new StopIdSeqAndHeadsign.Builder();
		}

		@Override
		public Builder withTripId(GtfsTrip.Id tripId) {
			stopTime.tripId = tripId;
			return this;
		}

		@Override
		public Builder withDepartureTime(GtfsLogicalTime departureTime) {
			stopTime.setData(DEPTIME_MASK, DEPTIME_SHIFT,
					departureTime == null ? NULL_TIME
							: departureTime.getSecondSinceMidnight());
			return this;
		}

		@Override
		public Builder withArrivalTime(GtfsLogicalTime arrivalTime) {
			stopTime.setData(ARRTIME_MASK, ARRTIME_SHIFT,
					arrivalTime == null ? NULL_TIME
							: arrivalTime.getSecondSinceMidnight());
			return this;
		}

		@Override
		public Builder withStopId(GtfsStop.Id stopId) {
			stopStuffBuilder.withStopId(stopId);
			return this;
		}

		@Override
		public Builder withStopSequence(GtfsTripStopSequence stopSequence) {
			stopStuffBuilder.withStopSequence(stopSequence);
			return this;
		}

		@Override
		public Builder withStopHeadsign(String stopHeadsign) {
			stopStuffBuilder.withStopHeadsign(stopHeadsign);
			return this;
		}

		@Override
		public Builder withPickupType(GtfsPickupType pickupType) {
			stopTime.setData(PICKUP_MASK, PICKUP_SHIFT,
					pickupType == null ? NULL_PICKUP : pickupType.getValue());
			return this;
		}

		@Override
		public Builder withDropoffType(GtfsDropoffType dropoffType) {
			stopTime.setData(DROPOFF_MASK, DROPOFF_SHIFT,
					dropoffType == null ? NULL_DROPOFF
							: dropoffType.getValue());
			return this;
		}

		@Override
		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			stopStuffBuilder.withShapeDistTraveled(shapeDistTraveled);
			return this;
		}

		@Override
		public Builder withTimepoint(GtfsTimepoint timepoint) {
			stopTime.setData(TIMEPOINT_MASK, TIMEPOINT_SHIFT,
					timepoint == null ? NULL_TIMEPOINT : timepoint.getValue());
			return this;
		}

		@Override
		public SmallGtfsStopTime build() {
			stopTime.stopIdSeqAndHeadsign = stopStuffBuilder.build();
			return stopTime;
		}
	}
}
