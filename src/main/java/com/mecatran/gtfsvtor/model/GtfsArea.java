package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsArea implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "areas.txt";

	private GtfsArea.Id id;
	private String name;

	private long sourceLineNumber;

	public GtfsArea.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Area{id=" + id + ",name=" + name + "}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsArea> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, Id.class);
		}
	}

	public static class Builder {
		private GtfsArea area;

		public Builder(String id) {
			area = new GtfsArea();
			area.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			area.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withName(String name) {
			area.name = name;
			return this;
		}

		public GtfsArea build() {
			return area;
		}
	}
}
