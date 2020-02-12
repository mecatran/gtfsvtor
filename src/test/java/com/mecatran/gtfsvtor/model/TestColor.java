package com.mecatran.gtfsvtor.model;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

public class TestColor {

	@Test
	public void testColor() throws ParseException {

		assertEquals(0x000000, GtfsColor.BLACK.getRgb());
		assertEquals(0xFFFFFF, GtfsColor.WHITE.getRgb());
		assertEquals(0.0, GtfsColor.BLACK.getBrightness(), 1e-20);
		assertEquals(1.0, GtfsColor.WHITE.getBrightness(), 1e-20);

		GtfsColor red = GtfsColor.parseHexTriplet("FF0000");
		assertEquals(0xFF0000, red.getRgb());
		assertEquals(0.299, red.getBrightness(), 1e-20);

		GtfsColor green = GtfsColor.parseHexTriplet("00ff00");
		assertEquals(0x00FF00, green.getRgb());
		assertEquals(0.587, green.getBrightness(), 1e-20);

		GtfsColor blue = GtfsColor.parseHexTriplet("0000Ff");
		assertEquals(0x0000FF, blue.getRgb());
		assertEquals(0.114, blue.getBrightness(), 1e-20);
	}
}
