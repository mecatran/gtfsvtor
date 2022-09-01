package com.mecatran.gtfsvtor.model;

import java.util.Arrays;
import java.util.List;

/**
 * A transient object used to load a stop-area relationship, but not stored in
 * the DAO per se. Only used to properly define the loader schema and check for
 * duplicated pairs.
 */
public class GtfsStopArea
		implements GtfsObject<List<String>>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "stop_areas.txt";

	private GtfsArea.Id areaId;
	private GtfsStop.Id stopId;

	private long sourceLineNumber;

	public GtfsStopArea.Id getId() {
		return id(areaId, stopId);
	}

	public GtfsArea.Id getAreaId() {
		return areaId;
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
		return "StopArea{areaId=" + areaId + ",stopId=" + stopId + "}";
	}

	public static Id id(GtfsArea.Id areaId, GtfsStop.Id stopId) {
		if (areaId == null || stopId == null)
			return null;
		return new Id(areaId, stopId);
	}

	public static class Id extends GtfsCompositeId<String, GtfsStopArea> {

		private Id(GtfsArea.Id areaId, GtfsStop.Id stopId) {
			super(Arrays.asList(areaId, stopId));
		}
	}

	public static class Builder {
		private GtfsStopArea stopArea;

		public Builder(GtfsArea.Id areaId, GtfsStop.Id stopId) {
			stopArea = new GtfsStopArea();
			stopArea.areaId = areaId;
			stopArea.stopId = stopId;
		}

		public Builder withSourceLineNumber(long lineNumber) {
			stopArea.sourceLineNumber = lineNumber;
			return this;
		}

		public GtfsStopArea build() {
			return stopArea;
		}
	}
}
