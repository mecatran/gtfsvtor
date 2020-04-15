package com.mecatran.gtfsvtor.model;

/**
 * TODO Should this be an enum or a simple integer?
 */
public enum GtfsNumTransfers {

	NONE(0), ONCE(1), TWICE(2), UNLIMITED(Integer.MAX_VALUE);

	private final int value;

	private GtfsNumTransfers(int value) {
		this.value = value;
	}

	public static GtfsNumTransfers fromValue(int value) {
		switch (value) {
		case 0:
			return NONE;
		case 1:
			return ONCE;
		case 2:
			return TWICE;
		case Integer.MAX_VALUE:
			return UNLIMITED;
		default:
			throw new IllegalArgumentException(
					"Invalid num transfer type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
