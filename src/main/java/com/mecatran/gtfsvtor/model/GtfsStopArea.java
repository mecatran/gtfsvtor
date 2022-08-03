package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

import com.mecatran.gtfsvtor.utils.Pair;

/**
 * A transient object used to load a stop-area relationship, but not stored in
 * the DAO per se. Only used to properly define the loader schema and check for
 * duplicated pairs.
 */
public class GtfsStopArea
		implements GtfsObject<Pair<String, String>>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "stop_areas.txt";

	private GtfsStopArea.Id id;

	private long sourceLineNumber;

	public GtfsStopArea.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	@Override
	public String toString() {
		return "StopArea{id=" + id + "}";
	}

	public static Id id(String areaId, String stopId) {
		return areaId == null || stopId == null || areaId.isEmpty()
				|| stopId.isEmpty() ? null : Id.build(areaId, stopId);
	}

	public static class Id
			extends GtfsAbstractId<Pair<String, String>, GtfsStopArea> {

		private Id(Pair<String, String> areaAndStopIds) {
			super(areaAndStopIds);
		}

		private static Map<Pair<String, String>, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String areaId, String stopId) {
			return CACHE.computeIfAbsent(new Pair<>(areaId, stopId), Id::new);
		}

		public GtfsArea.Id getAreaId() {
			return GtfsArea.id(getInternalId().getFirst());
		}

		public GtfsStop.Id getStopId() {
			return GtfsStop.id(getInternalId().getSecond());
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, Id.class);
		}
	}

	public static class Builder {
		private GtfsStopArea stopArea;

		public Builder(String areaId, String stopId) {
			stopArea = new GtfsStopArea();
			stopArea.id = GtfsStopArea.id(areaId, stopId);
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
