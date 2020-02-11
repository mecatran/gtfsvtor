package com.mecatran.gtfsvtor.model;

import java.text.ParseException;

/**
 */
public class GtfsColor {

	private final int rgb;

	private GtfsColor(int rgb) {
		this.rgb = rgb;
	}

	public static GtfsColor parseHexTriplet(String hexColor)
			throws ParseException {
		if (hexColor == null || hexColor.isEmpty())
			return null;
		if (hexColor.length() != 6)
			throw new ParseException("Invalid hex color: " + hexColor, 0);
		try {
			Integer rgb = Integer.parseInt(hexColor, 16);
			// TODO Intern value
			return new GtfsColor(rgb);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid hex color: " + hexColor, 0);
		}
	}

	public int getRgb() {
		return rgb;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(rgb);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GtfsColor)) {
			return false;
		}
		GtfsColor other = (GtfsColor) obj;
		return rgb == other.rgb;
	}

	@Override
	public String toString() {
		return String.format("GtfsColor{%06X}", rgb);
	}
}
