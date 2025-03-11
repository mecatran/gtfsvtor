package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsLocationGroupStop implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "location_group_stops.txt";

	private GtfsLocationGroup.Id locationGroupid;
	private GtfsStop.Id stopId;

	private long sourceLineNumber;


	public GtfsLocationGroup.Id getLocationGroupId() {
		return locationGroupid;
	}

	public GtfsStop.Id getStopId() {
		return stopId;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	@Override
	public String toString() {
		return "LocationGroupStop{locationGroupId=" + locationGroupid + ",stopId='" + stopId + "'}";
	}

	public static class Builder {
		private GtfsLocationGroupStop locationGroupStop;

		public Builder() {
			locationGroupStop = new GtfsLocationGroupStop();
		}

		public Builder withSourceLineNumber(long lineNumber) {
			locationGroupStop.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withLocationGroupId(GtfsLocationGroup.Id locationGroupid) {
			locationGroupStop.locationGroupid = locationGroupid;
			return this;
		}

		public Builder withStopId(GtfsStop.Id stopId) {
			locationGroupStop.stopId = stopId;
			return this;
		}

		public GtfsLocationGroupStop build() {
			return locationGroupStop;
		}
	}
}
