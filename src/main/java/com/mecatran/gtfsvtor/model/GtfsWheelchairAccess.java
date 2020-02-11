package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsWheelchairAccess {

	UNKNOWN(0), ALLOWED(1), NONE(2);

	private final int value;

	private GtfsWheelchairAccess(int value) {
		this.value = value;
	}

	public static GtfsWheelchairAccess fromValue(int value) {
		switch (value) {
		case 0:
			return UNKNOWN;
		case 1:
			return ALLOWED;
		case 2:
			return NONE;
		default:
			throw new IllegalArgumentException(
					"Invalid wheelchair access type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
