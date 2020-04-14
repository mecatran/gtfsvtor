package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsTransferType {

	RECOMMENDED(0), SYNCHRONIZED(1), TIMED(2), NONE(3);

	private final int value;

	private GtfsTransferType(int value) {
		this.value = value;
	}

	public static GtfsTransferType fromValue(int value) {
		switch (value) {
		case 0:
			return RECOMMENDED;
		case 1:
			return SYNCHRONIZED;
		case 2:
			return TIMED;
		case 3:
			return NONE;
		default:
			throw new IllegalArgumentException(
					"Invalid transfer type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
