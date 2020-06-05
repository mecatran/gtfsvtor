package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsLevel implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "levels.txt";

	private GtfsLevel.Id id;
	private Double index;
	private String name;

	private long sourceLineNumber;

	public GtfsLevel.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public Double getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Level{id=" + id + ",index=" + index + ",name=" + name + "}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsLevel> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsLevel.Id.class);
		}
	}

	public static class Builder {
		private GtfsLevel level;

		public Builder(String id) {
			level = new GtfsLevel();
			level.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			level.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withIndex(Double index) {
			level.index = index;
			return this;
		}

		public Builder withName(String name) {
			level.name = name;
			return this;
		}

		public GtfsLevel build() {
			return level;
		}
	}
}
