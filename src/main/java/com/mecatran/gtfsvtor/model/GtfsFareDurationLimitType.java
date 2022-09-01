package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsFareDurationLimitType {

	DEPARTURE_DEPARTURE(0), DEPARTURE_ARRIVAL(1), ARRIVAL_DEPARTURE(2), ARRIVAL_ARRIVAL(3);

	private final int value;

	private GtfsFareDurationLimitType(int value) {
		this.value = value;
	}

	public static GtfsFareDurationLimitType fromValue(int value) {
		switch (value) {
		case 0:
			return DEPARTURE_DEPARTURE;
		case 1:
			return DEPARTURE_ARRIVAL;
		case 2:
			return ARRIVAL_DEPARTURE;
		case 3:
			return ARRIVAL_ARRIVAL;
		default:
			throw new IllegalArgumentException(
					"Invalid fare duration limit type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
