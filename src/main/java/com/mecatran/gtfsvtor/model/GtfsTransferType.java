package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsTransferType {

	// See http://gtfs.org/schedule/reference/#linked-trips for more information
	// on type 4 and 5.
	RECOMMENDED(0), SYNCHRONIZED(1), TIMED(2), NONE(3), TRIP_IN_SEAT(4),
	TRIP_ALIGHT(5);

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
		case 4:
			return TRIP_IN_SEAT;
		case 5:
			return TRIP_ALIGHT;
		default:
			throw new IllegalArgumentException(
					"Invalid transfer type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
