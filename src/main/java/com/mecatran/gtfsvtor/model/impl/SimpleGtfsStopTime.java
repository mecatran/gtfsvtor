package com.mecatran.gtfsvtor.model.impl;

import java.util.Comparator;
import java.util.Objects;
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
 * Unoptimized GtfsStopTime.
 */
public class SimpleGtfsStopTime implements GtfsStopTime {

	private GtfsTrip.Id tripId;
	// TODO Associate the values (departure/arrival) and cache
	private GtfsLogicalTime departureTime;
	private GtfsLogicalTime arrivalTime;
	// TODO Associate the values (stopid/sequence/headsign) and cache
	private GtfsStop.Id stopId;
	private GtfsTripStopSequence stopSequence;
	private String stopHeadsign;
	// TODO Associate the values (pickup/dropoff/timepoint) and cache
	private GtfsPickupType pickupType;
	private GtfsDropoffType dropoffType;
	private Double shapeDistTraveled;
	private GtfsTimepoint timepoint;

	public GtfsTrip.Id getTripId() {
		return tripId;
	}

	public GtfsLogicalTime getDepartureTime() {
		return departureTime;
	}

	public GtfsLogicalTime getArrivalTime() {
		return arrivalTime;
	}

	public GtfsStop.Id getStopId() {
		return stopId;
	}

	public GtfsTripStopSequence getStopSequence() {
		return stopSequence;
	}

	public String getStopHeadsign() {
		return stopHeadsign;
	}

	public Optional<GtfsPickupType> getPickupType() {
		return Optional.ofNullable(pickupType);
	}

	public Optional<GtfsDropoffType> getDropoffType() {
		return Optional.ofNullable(dropoffType);
	}

	public Double getShapeDistTraveled() {
		return shapeDistTraveled;
	}

	public Optional<GtfsTimepoint> getTimepoint() {
		return Optional.ofNullable(timepoint);
	}

	@Override
	public String toString() {
		return "StopTime{trip=" + tripId + ", arr=" + arrivalTime + ", dep="
				+ departureTime + ", stop=" + stopId + "}";
	}

	public static class Builder implements GtfsStopTime.Builder {
		private SimpleGtfsStopTime stopTime;

		public Builder() {
			stopTime = new SimpleGtfsStopTime();
		}

		@Override
		public Builder withTripId(GtfsTrip.Id tripId) {
			stopTime.tripId = tripId;
			return this;
		}

		@Override
		public Builder withDepartureTime(GtfsLogicalTime departureTime) {
			stopTime.departureTime = departureTime;
			return this;
		}

		@Override
		public Builder withArrivalTime(GtfsLogicalTime arrivalTime) {
			stopTime.arrivalTime = arrivalTime;
			return this;
		}

		@Override
		public Builder withStopId(GtfsStop.Id stopId) {
			stopTime.stopId = stopId;
			return this;
		}

		@Override
		public Builder withStopSequence(GtfsTripStopSequence stopSequence) {
			stopTime.stopSequence = stopSequence;
			return this;
		}

		@Override
		public Builder withStopHeadsign(String stopHeadsign) {
			stopTime.stopHeadsign = stopHeadsign;
			return this;
		}

		@Override
		public Builder withPickupType(GtfsPickupType pickupType) {
			stopTime.pickupType = pickupType;
			return this;
		}

		@Override
		public Builder withDropoffType(GtfsDropoffType dropoffType) {
			stopTime.dropoffType = dropoffType;
			return this;
		}

		@Override
		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			stopTime.shapeDistTraveled = shapeDistTraveled;
			return this;
		}

		@Override
		public Builder withTimepoint(GtfsTimepoint timepoint) {
			stopTime.timepoint = timepoint;
			return this;
		}

		@Override
		public SimpleGtfsStopTime build() {
			return stopTime;
		}
	}

	public static final Comparator<GtfsStopTime> STOP_SEQ_COMPARATOR = new Comparator<GtfsStopTime>() {
		@Override
		public int compare(GtfsStopTime time1, GtfsStopTime time2) {
			// Make sure we do not compare times from different trips
			assert Objects.equals(time1.getTripId(), time2.getTripId());
			// Sort undef stop sequence at start
			int seq1 = time1.getStopSequence() == null ? Integer.MIN_VALUE
					: time1.getStopSequence().getSequence();
			int seq2 = time2.getStopSequence() == null ? Integer.MIN_VALUE
					: time1.getStopSequence().getSequence();
			return Integer.compare(seq1, seq2);
		}
	};
}
