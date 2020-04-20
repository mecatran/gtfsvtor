package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsStopType {

	STOP(0), STATION(1), ENTRANCE(2), NODE(3), BOARDING_AREA(4);

	private final int value;

	private GtfsStopType(int value) {
		this.value = value;
	}

	public static GtfsStopType fromValue(Integer value) {
		if (value == null)
			return null;
		switch (value) {
		case 0:
			return STOP;
		case 1:
			return STATION;
		case 2:
			return ENTRANCE;
		case 3:
			return NODE;
		case 4:
			return BOARDING_AREA;
		default:
			throw new IllegalArgumentException("Invalid stop type: " + value);
		}
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name() + "(" + value + ")";
	}
}
