package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsPickupType {

	DEFAULT_PICKUP(0), NO_PICKUP(1), PHONE_PICKUP(2), ASK_DRIVER_PICKUP(3);

	private final int value;

	private GtfsPickupType(int value) {
		this.value = value;
	}

	public static GtfsPickupType fromValue(int value) {
		switch (value) {
		case 0:
			return DEFAULT_PICKUP;
		case 1:
			return NO_PICKUP;
		case 2:
			return PHONE_PICKUP;
		case 3:
			return ASK_DRIVER_PICKUP;
		default:
			throw new IllegalArgumentException("Invalid pickup type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
