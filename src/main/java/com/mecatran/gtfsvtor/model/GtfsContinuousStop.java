package com.mecatran.gtfsvtor.model;

/** Pickup or drop-off along a trip segment, between scheduled stops. */
public enum GtfsContinuousStop {

	CONTINUOUS(0), NONE(1), PHONE_AGENCY(2), COORDINATE_WITH_DRIVER(3);

	private final int value;

	GtfsContinuousStop(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static GtfsContinuousStop fromValue(int value) {
		switch (value) {
		case 0:
			return CONTINUOUS;
		case 1:
			return NONE;
		case 2:
			return PHONE_AGENCY;
		case 3:
			return COORDINATE_WITH_DRIVER;
		default:
			throw new IllegalArgumentException("Invalid continuous stopping type: " + value);
		}
	}
}
