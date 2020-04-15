package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsPaymentMethod {

	ON_BOARD(0), BEFORE_BOARDING(1);

	private final int value;

	private GtfsPaymentMethod(int value) {
		this.value = value;
	}

	public static GtfsPaymentMethod fromValue(int value) {
		switch (value) {
		case 0:
			return ON_BOARD;
		case 1:
			return BEFORE_BOARDING;
		default:
			throw new IllegalArgumentException(
					"Invalid payment type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
