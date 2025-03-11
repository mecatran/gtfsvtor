package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsLocationGroup implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "location_groups.txt";

	private GtfsLocationGroup.Id id;
	private String name;

	private long sourceLineNumber;


	public GtfsLocationGroup.Id getId() {
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
		return "LocationGroup{id=" + id + ",name='" + name + "'}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsLocationGroup> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsLocationGroup.Id.class);
		}
	}

	public static class Builder {
		private GtfsLocationGroup locationGroup;

		public Builder(String id) {
			locationGroup = new GtfsLocationGroup();
			locationGroup.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			locationGroup.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withName(String name) {
			locationGroup.name = name == null ? null : name.intern();
			return this;
		}

		public GtfsLocationGroup build() {
			return locationGroup;
		}
	}
}
