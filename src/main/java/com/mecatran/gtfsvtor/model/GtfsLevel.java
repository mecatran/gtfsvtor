package com.mecatran.gtfsvtor.model;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsLevel implements GtfsObject<String>, GtfsObjectWithSourceInfo {

	public static final String TABLE_NAME = "levels.txt";

	private GtfsLevel.Id id;
	private Double index;
	private String name;

	private DataObjectSourceInfo sourceInfo;

	public GtfsLevel.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
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
		return id == null || id.isEmpty() ? null : new Id(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsLevel> {

		private Id(String id) {
			super(id);
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

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			level.sourceInfo = sourceInfo;
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
