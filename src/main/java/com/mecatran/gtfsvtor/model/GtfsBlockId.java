package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basically, a simple class encapsulating a string, with caching.
 */
public class GtfsBlockId {

	private final String blockId;

	private final static Map<String, GtfsBlockId> CACHE = new HashMap<>();

	private GtfsBlockId(String blockId) {
		this.blockId = blockId;
	}

	public static GtfsBlockId fromValue(String blockId) {
		if (blockId == null || blockId.isEmpty())
			return null;
		// TODO Synchronize
		return CACHE.computeIfAbsent(blockId, GtfsBlockId::new);
	}

	public String getValue() {
		return blockId;
	}

	@Override
	public int hashCode() {
		return blockId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GtfsBlockId)) {
			return false;
		}
		GtfsBlockId other = (GtfsBlockId) obj;
		return Objects.equals(blockId, other.blockId);
	}

	@Override
	public String toString() {
		return blockId;
	}
}
