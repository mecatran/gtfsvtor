package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsExactTime {

	FREQUENCY_BASED(0), SCHEDULE_BASED(1);

	private final int value;

	private GtfsExactTime(int value) {
		this.value = value;
	}

	public static GtfsExactTime fromValue(int value) {
		switch (value) {
		case 0:
			return FREQUENCY_BASED;
		case 1:
			return SCHEDULE_BASED;
		default:
			throw new IllegalArgumentException("Invalid exact_times: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
