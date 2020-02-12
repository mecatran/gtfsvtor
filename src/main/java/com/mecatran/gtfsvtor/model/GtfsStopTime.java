package com.mecatran.gtfsvtor.model;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is an interface to allow for optimization on memory usage.
 */
public interface GtfsStopTime extends GtfsObject<String> {

	public static final String TABLE_NAME = "stop_times.txt";

	public GtfsTrip.Id getTripId();

	public GtfsLogicalTime getDepartureTime();

	public default GtfsLogicalTime getDepartureOrArrivalTime() {
		GtfsLogicalTime dep = getDepartureTime();
		return dep != null ? dep : getArrivalTime();
	}

	public GtfsLogicalTime getArrivalTime();

	public default GtfsLogicalTime getArrivalOrDepartureTime() {
		GtfsLogicalTime arr = getArrivalTime();
		return arr != null ? arr : getDepartureTime();
	}

	public GtfsStop.Id getStopId();

	public GtfsTripStopSequence getStopSequence();

	public String getStopHeadsign();

	public Optional<GtfsPickupType> getPickupType();

	public default GtfsPickupType getNonNullPickupType() {
		return getPickupType().orElse(GtfsPickupType.DEFAULT_PICKUP);
	}

	public Optional<GtfsDropoffType> getDropoffType();

	public default GtfsDropoffType getNonNullDropoffType() {
		return getDropoffType().orElse(GtfsDropoffType.DEFAULT_DROPOFF);
	}

	public Double getShapeDistTraveled();

	public Optional<GtfsTimepoint> getTimepoint();

	public default GtfsTimepoint getNonNullTimepoint() {
		return getTimepoint().orElse(GtfsTimepoint.EXACT);
	}

	public interface Builder {

		public Builder withTripId(GtfsTrip.Id tripId);

		public Builder withDepartureTime(GtfsLogicalTime departureTime);

		public Builder withArrivalTime(GtfsLogicalTime arrivalTime);

		public Builder withStopId(GtfsStop.Id stopId);

		public Builder withStopSequence(GtfsTripStopSequence stopSequence);

		public Builder withStopHeadsign(String stopHeadsign);

		public Builder withPickupType(GtfsPickupType pickupType);

		public Builder withDropoffType(GtfsDropoffType dropoffType);

		public Builder withShapeDistTraveled(Double shapeDistTraveled);

		public Builder withTimepoint(GtfsTimepoint timepoint);

		public GtfsStopTime build();
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
