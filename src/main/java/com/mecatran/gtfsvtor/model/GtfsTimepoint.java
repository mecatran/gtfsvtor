package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsTimepoint {

	APPROXIMATE(0), EXACT(1);

	private final int value;

	private GtfsTimepoint(int value) {
		this.value = value;
	}

	public static GtfsTimepoint fromValue(int value) {
		switch (value) {
		case 0:
			return APPROXIMATE;
		case 1:
			return EXACT;
		default:
			throw new IllegalArgumentException(
					"Invalid timepoint enum: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
