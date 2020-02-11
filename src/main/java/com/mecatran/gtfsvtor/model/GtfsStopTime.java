package com.mecatran.gtfsvtor.model;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * TODO Make this class an interface to allow for optimization on memory usage
 * (for example using some kind of flyweight pattern).
 */
public class GtfsStopTime implements GtfsObject<String> {

	public static final String TABLE_NAME = "stop_times.txt";

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

	public GtfsLogicalTime getDepartureOrArrivalTime() {
		return departureTime != null ? departureTime : arrivalTime;
	}

	public GtfsLogicalTime getArrivalTime() {
		return arrivalTime;
	}

	public GtfsLogicalTime getArrivalOrDepartureTime() {
		return arrivalTime != null ? arrivalTime : departureTime;
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

	public GtfsPickupType getNonNullPickupType() {
		return pickupType == null ? GtfsPickupType.DEFAULT_PICKUP : pickupType;
	}

	public Optional<GtfsDropoffType> getDropoffType() {
		return Optional.ofNullable(dropoffType);
	}

	public GtfsDropoffType getNonNullDropoffType() {
		return dropoffType == null ? GtfsDropoffType.DEFAULT_DROPOFF
				: dropoffType;
	}

	public Double getShapeDistTraveled() {
		return shapeDistTraveled;
	}

	public Optional<GtfsTimepoint> getTimepoint() {
		return Optional.ofNullable(timepoint);
	}

	public GtfsTimepoint getNonNullTimepoint() {
		return timepoint == null ? GtfsTimepoint.EXACT : timepoint;
	}

	@Override
	public String toString() {
		return "StopTime{trip=" + tripId + ", arr=" + arrivalTime + ", dep="
				+ departureTime + ", stop=" + stopId + "}";
	}

	public static class Builder {
		private GtfsStopTime stopTime;

		public Builder() {
			stopTime = new GtfsStopTime();
		}

		public Builder withTripId(GtfsTrip.Id tripId) {
			stopTime.tripId = tripId;
			return this;
		}

		public Builder withDepartureTime(GtfsLogicalTime departureTime) {
			stopTime.departureTime = departureTime;
			return this;
		}

		public Builder withArrivalTime(GtfsLogicalTime arrivalTime) {
			stopTime.arrivalTime = arrivalTime;
			return this;
		}

		public Builder withStopId(GtfsStop.Id stopId) {
			stopTime.stopId = stopId;
			return this;
		}

		public Builder withStopSequence(GtfsTripStopSequence stopSequence) {
			stopTime.stopSequence = stopSequence;
			return this;
		}

		public Builder withStopHeadsign(String stopHeadsign) {
			stopTime.stopHeadsign = stopHeadsign;
			return this;
		}

		public Builder withPickupType(GtfsPickupType pickupType) {
			stopTime.pickupType = pickupType;
			return this;
		}

		public Builder withDropoffType(GtfsDropoffType dropoffType) {
			stopTime.dropoffType = dropoffType;
			return this;
		}

		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			stopTime.shapeDistTraveled = shapeDistTraveled;
			return this;
		}

		public Builder withTimepoint(GtfsTimepoint timepoint) {
			stopTime.timepoint = timepoint;
			return this;
		}

		public GtfsStopTime build() {
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
