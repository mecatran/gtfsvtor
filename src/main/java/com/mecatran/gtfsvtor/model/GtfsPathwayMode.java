package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsPathwayMode {

	WALKWAY(1), STAIRS(2), MOVING_SIDEWALK(3), ESCALATOR(4), ELEVATOR(5),
	FARE_GATE(6), EXIT_GATE(7);

	private final int value;

	private GtfsPathwayMode(int value) {
		this.value = value;
	}

	public static GtfsPathwayMode fromValue(int value) {
		switch (value) {
		case 1:
			return WALKWAY;
		case 2:
			return STAIRS;
		case 3:
			return MOVING_SIDEWALK;
		case 4:
			return ESCALATOR;
		case 5:
			return ELEVATOR;
		case 6:
			return FARE_GATE;
		case 7:
			return EXIT_GATE;
		default:
			throw new IllegalArgumentException(
					"Invalid pathway mode: " + value);
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
