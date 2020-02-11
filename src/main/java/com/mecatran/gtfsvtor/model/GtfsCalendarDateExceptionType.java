package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsCalendarDateExceptionType {

	ADDED(1), REMOVED(2);

	private final int value;

	private GtfsCalendarDateExceptionType(int value) {
		this.value = value;
	}

	public static GtfsCalendarDateExceptionType fromValue(int value) {
		switch (value) {
		case 1:
			return ADDED;
		case 2:
			return REMOVED;
		default:
			throw new IllegalArgumentException(
					"Invalid calendar date exception type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
