package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsTripDirectionId {

	DIRECTION0(0), DIRECTION1(1);

	private final int value;

	private GtfsTripDirectionId(int value) {
		this.value = value;
	}

	public static GtfsTripDirectionId fromValue(int value) {
		switch (value) {
		case 0:
			return DIRECTION0;
		case 1:
			return DIRECTION1;
		default:
			throw new IllegalArgumentException(
					"Invalid trip direction ID: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
