package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public abstract class GtfsLegGroup implements GtfsObject<String> {

	// Unused class, only here to define a leg group ID consistently
	// (GTFS Fares V2)

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsLegGroup> {

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
}
