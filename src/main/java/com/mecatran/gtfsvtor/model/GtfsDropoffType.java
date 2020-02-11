package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsDropoffType {

	DEFAULT_DROPOFF(0), NO_DROPOFF(1), PHONE_DROPOFF(2), ASK_DRIVER_DROPOFF(3);

	private final int value;

	private GtfsDropoffType(int value) {
		this.value = value;
	}

	public static GtfsDropoffType fromValue(int value) {
		switch (value) {
		case 0:
			return DEFAULT_DROPOFF;
		case 1:
			return NO_DROPOFF;
		case 2:
			return PHONE_DROPOFF;
		case 3:
			return ASK_DRIVER_DROPOFF;
		default:
			throw new IllegalArgumentException(
					"Invalid dropoff type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
