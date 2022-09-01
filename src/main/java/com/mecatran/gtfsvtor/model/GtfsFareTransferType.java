package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsFareTransferType {

	LEG_TRANSFER(0), LEG_TRANSFER_LEG(1), TRANSFER(2);

	private final int value;

	private GtfsFareTransferType(int value) {
		this.value = value;
	}

	public static GtfsFareTransferType fromValue(int value) {
		switch (value) {
		case 0:
			return LEG_TRANSFER;
		case 1:
			return LEG_TRANSFER_LEG;
		case 2:
			return TRANSFER;
		default:
			throw new IllegalArgumentException(
					"Invalid fare transfer type: " + value);
		}
	}

	public int getValue() {
		return value;
	}
}
