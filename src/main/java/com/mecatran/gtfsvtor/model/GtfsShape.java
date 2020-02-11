package com.mecatran.gtfsvtor.model;

public abstract class GtfsShape implements GtfsObject<String> {

	// Unused class, only here to define an ID consistently

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : new Id(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsShape> {

		private Id(String id) {
			super(id);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, Id.class);
		}
	}
}
