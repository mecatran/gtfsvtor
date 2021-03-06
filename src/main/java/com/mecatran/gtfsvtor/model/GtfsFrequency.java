package com.mecatran.gtfsvtor.model;

import java.util.Optional;

public class GtfsFrequency
		implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "frequencies.txt";

	private GtfsTrip.Id tripId;
	private GtfsLogicalTime startTime;
	private GtfsLogicalTime endTime;
	private Integer headwaySeconds;
	private GtfsExactTime exactTimes;

	private long sourceLineNumber;

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public GtfsTrip.Id getTripId() {
		return tripId;
	}

	public GtfsLogicalTime getStartTime() {
		return startTime;
	}

	public GtfsLogicalTime getEndTime() {
		return endTime;
	}

	public Integer getHeadwaySeconds() {
		return headwaySeconds;
	}

	public Optional<GtfsExactTime> getExactTimes() {
		return Optional.ofNullable(exactTimes);
	}

	public GtfsExactTime getNonNullExactTime() {
		return exactTimes == null ? GtfsExactTime.FREQUENCY_BASED : exactTimes;
	}

	@Override
	public String toString() {
		return "Frequency{trip=" + tripId + ",start=" + startTime + ",end="
				+ endTime + ",headway=" + headwaySeconds + "}";
	}

	public static class Builder {
		private GtfsFrequency frequency;

		public Builder() {
			frequency = new GtfsFrequency();
		}

		public Builder withSourceLineNumber(long lineNumber) {
			frequency.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withTripId(GtfsTrip.Id tripId) {
			frequency.tripId = tripId;
			return this;
		}

		public Builder withStartTime(GtfsLogicalTime startTime) {
			frequency.startTime = startTime;
			return this;
		}

		public Builder withEndTime(GtfsLogicalTime endTime) {
			frequency.endTime = endTime;
			return this;
		}

		public Builder withHeadwaySeconds(Integer headwaySeconds) {
			frequency.headwaySeconds = headwaySeconds;
			return this;
		}

		public Builder withExactTimes(GtfsExactTime exactTimes) {
			frequency.exactTimes = exactTimes;
			return this;
		}

		public GtfsFrequency build() {
			return frequency;
		}
	}
}
