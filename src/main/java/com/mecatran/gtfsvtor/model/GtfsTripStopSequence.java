package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsTripStopSequence implements Comparable<GtfsTripStopSequence> {

	private int sequence;

	private static Map<Integer, GtfsTripStopSequence> CACHE = new HashMap<>(
			1000);

	private GtfsTripStopSequence(int sequence) {
		this.sequence = sequence;
	}

	public static GtfsTripStopSequence fromSequence(int sequence) {
		// TODO Synchronize?
		return CACHE.computeIfAbsent(sequence, GtfsTripStopSequence::new);
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
		if (!(obj instanceof GtfsTripStopSequence))
			return false;
		GtfsTripStopSequence other = (GtfsTripStopSequence) obj;
		return other.sequence == sequence;
	}

	@Override
	public String toString() {
		return Integer.toString(sequence);
	}

	@Override
	public int compareTo(GtfsTripStopSequence o) {
		return Integer.compare(sequence, o.sequence);
	}
}
