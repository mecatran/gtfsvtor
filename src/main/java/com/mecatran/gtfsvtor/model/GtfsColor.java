package com.mecatran.gtfsvtor.model;

import java.text.ParseException;

/**
 */
public class GtfsColor {

	private final int rgb;

	public static final GtfsColor WHITE = new GtfsColor(0xFFFFFF);
	public static final GtfsColor BLACK = new GtfsColor(0x000000);

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

	public String toHtmlString() {
		return String.format("#%06X", rgb);
	}

	/**
	 * Compute the brightness of an sRGB color using the formula from
	 * http://www.w3.org/TR/2000/WD-AERT-20000426#color-contrast
	 *
	 * @return Brightness, ranging [0..1]
	 */
	public double getBrightness() {
		int r = (rgb & 0xff0000) >> 16;
		int g = (rgb & 0x00ff00) >> 8;
		int b = (rgb & 0x0000ff);
		return (299 * r + 587 * g + 114 * b) / 255000.;
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
