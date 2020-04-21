package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsPathway implements GtfsObject<String> {

	public static final String TABLE_NAME = "pathways.txt";

	private GtfsPathway.Id id;
	private GtfsStop.Id fromStopId;
	private GtfsStop.Id toStopId;
	private GtfsPathwayMode pathwayMode;
	private GtfsDirectionality bidirectional;
	private Double length;
	private Integer traversalTime;
	private Integer stairCount;
	private Double maxSlope;
	private Double minWidth;
	private String signpostedAs;
	private String reversedSignpostedAs;

	public GtfsPathway.Id getId() {
		return id;
	}

	public GtfsStop.Id getFromStopId() {
		return fromStopId;
	}

	public GtfsStop.Id getToStopId() {
		return toStopId;
	}

	public GtfsPathwayMode getPathwayMode() {
		return pathwayMode;
	}

	public GtfsDirectionality getBidirectional() {
		return bidirectional;
	}

	public Double getLength() {
		return length;
	}

	public Integer getTraversalTime() {
		return traversalTime;
	}

	public Integer getStairCount() {
		return stairCount;
	}

	public Double getMaxSlope() {
		return maxSlope;
	}

	public Double getMinWidth() {
		return minWidth;
	}

	public String getSignpostedAs() {
		return signpostedAs;
	}

	public String getReverseSignpostedAs() {
		return reversedSignpostedAs;
	}

	@Override
	public String toString() {
		return "Pathway{id=" + id + ",from=" + fromStopId + ",to=" + toStopId
				+ "}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsPathway> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsPathway.Id.class);
		}
	}

	public static class Builder {
		private GtfsPathway pathway;

		public Builder(String id) {
			pathway = new GtfsPathway();
			pathway.id = id(id);
		}

		public Builder withFromStopId(GtfsStop.Id fromStopId) {
			pathway.fromStopId = fromStopId;
			return this;
		}

		public Builder withToStopId(GtfsStop.Id toStopId) {
			pathway.toStopId = toStopId;
			return this;
		}

		public Builder withPathwayMode(GtfsPathwayMode pathwayMode) {
			pathway.pathwayMode = pathwayMode;
			return this;
		}

		public Builder withBidirectional(GtfsDirectionality bidirectional) {
			pathway.bidirectional = bidirectional;
			return this;
		}

		public Builder withLength(Double length) {
			pathway.length = length;
			return this;
		}

		public Builder withTraversalTime(Integer traversalTime) {
			pathway.traversalTime = traversalTime;
			return this;
		}

		public Builder withStairCount(Integer stairCount) {
			pathway.stairCount = stairCount;
			return this;
		}

		public Builder withMaxSlope(Double maxSlope) {
			pathway.maxSlope = maxSlope;
			return this;
		}

		public Builder withMinWitdth(Double minWidth) {
			pathway.minWidth = minWidth;
			return this;
		}

		public Builder withSignpostedAs(String signpostedAs) {
			pathway.signpostedAs = signpostedAs;
			return this;
		}

		public Builder withReversedSignpostedAs(String reversedSignpostedAs) {
			pathway.reversedSignpostedAs = reversedSignpostedAs;
			return this;
		}

		public GtfsPathway build() {
			return pathway;
		}
	}
}
