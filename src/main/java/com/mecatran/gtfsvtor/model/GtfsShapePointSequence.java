package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsShapePointSequence
		implements Comparable<GtfsShapePointSequence> {

	private int sequence;

	private static Map<Integer, GtfsShapePointSequence> CACHE = new HashMap<>(
			1000);

	private GtfsShapePointSequence(int sequence) {
		this.sequence = sequence;
	}

	public static GtfsShapePointSequence fromSequence(int sequence) {
		// TODO Synchronize?
		return CACHE.computeIfAbsent(sequence, GtfsShapePointSequence::new);
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(sequence);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GtfsShapePointSequence))
			return false;
		GtfsShapePointSequence other = (GtfsShapePointSequence) obj;
		return other.sequence == sequence;
	}

	@Override
	public String toString() {
		return Integer.toString(sequence);
	}

	@Override
	public int compareTo(GtfsShapePointSequence o) {
		return Integer.compare(sequence, o.sequence);
	}
}
