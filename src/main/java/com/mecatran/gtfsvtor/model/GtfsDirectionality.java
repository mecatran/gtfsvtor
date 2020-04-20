package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsDirectionality {

	UNIDIRECTIONAL(0), BIDIRECTIONAL(1);

	private final int value;

	private GtfsDirectionality(int value) {
		this.value = value;
	}

	public static GtfsDirectionality fromValue(int value) {
		switch (value) {
		case 0:
			return UNIDIRECTIONAL;
		case 1:
			return BIDIRECTIONAL;
		default:
			throw new IllegalArgumentException(
					"Invalid directionality: " + value);
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
