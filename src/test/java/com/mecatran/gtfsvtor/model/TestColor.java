package com.mecatran.gtfsvtor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

public class TestColor {

	@Test
	public void testColorBrightness() throws ParseException {

		assertEquals(0x000000, GtfsColor.BLACK.getRgb());
		assertEquals(0xFFFFFF, GtfsColor.WHITE.getRgb());
		assertEquals(0.0, GtfsColor.BLACK.getBrightness(), 1e-20);
		assertEquals(1.0, GtfsColor.WHITE.getBrightness(), 1e-20);

	}

	@Test
	public void testColorDistance() throws ParseException {

		// Primary colors
		GtfsColor red = GtfsColor.parseHexTriplet("FF0000");
		assertEquals(0xFF0000, red.getRgb());
		assertEquals(0.299, red.getBrightness(), 1e-20);

		GtfsColor green = GtfsColor.parseHexTriplet("00ff00");
		assertEquals(0x00FF00, green.getRgb());
		assertEquals(0.587, green.getBrightness(), 1e-20);

		GtfsColor blue = GtfsColor.parseHexTriplet("0000Ff");
		assertEquals(0x0000FF, blue.getRgb());
		assertEquals(0.114, blue.getBrightness(), 1e-20);

		// Secondary colors
		GtfsColor yellow = GtfsColor.parseHexTriplet("FFFF00");
		assertEquals(0.886, yellow.getBrightness(), 1e-20);
		GtfsColor magenta = GtfsColor.parseHexTriplet("FF00FF");
		assertEquals(0.413, magenta.getBrightness(), 1e-20);
		GtfsColor cyan = GtfsColor.parseHexTriplet("00FFFF");
		assertEquals(0.701, cyan.getBrightness(), 1e-20);

		assertEquals(1.0, GtfsColor.WHITE.getDistance(GtfsColor.BLACK), 1e-10);

		GtfsColor[] allColors = new GtfsColor[] { GtfsColor.BLACK,
				GtfsColor.WHITE, red, green, blue, yellow, magenta, cyan };
		for (int i = 0; i < allColors.length; i++) {
			for (int j = 0; j < allColors.length; j++) {
				GtfsColor c1 = allColors[i];
				GtfsColor c2 = allColors[j];
				double d = c1.getDistance(c2);
				assertTrue(d >= 0.0);
				assertTrue(d <= 1.0);
				if (i == j)
					assertEquals(0.0, d, 1e-20);
			}
		}
	}
}
