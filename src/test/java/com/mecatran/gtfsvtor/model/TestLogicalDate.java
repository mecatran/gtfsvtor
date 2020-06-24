package com.mecatran.gtfsvtor.model;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class TestLogicalDate {

	@Test
	public void testJulianDay() {
		assertEquals(2440588, GtfsLogicalDate.computeJulianDay(1970, 1, 1));
		assertEquals(2415021, GtfsLogicalDate.computeJulianDay(1900, 1, 1));
		assertEquals(2451545, GtfsLogicalDate.computeJulianDay(2000, 1, 1));
		assertEquals(2459215, GtfsLogicalDate.computeJulianDay(2020, 12, 31));
		GtfsLogicalDate epoch = GtfsLogicalDate.getDate(1970, 1, 1);
		assertEquals(2440588, epoch.getJulianDay());
		// 1970, Jan 1st is a Thursday (DOW=3)
		assertEquals(3, epoch.getDayOfTheWeek());
		GtfsLogicalDate date = GtfsLogicalDate.getDate(2020, 12, 31);
		assertEquals(2459215, date.getJulianDay());
		// 2020, Dec 31 is a Thursday (DOW=3)
		assertEquals(3, date.getDayOfTheWeek());

		/*
		 * Test if Julian day computation looks sane for every day between 1800
		 * and 2100
		 */
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.YEAR, 1800);
		// Julian day of Jan 1st, 1800
		int jday = 2378497;
		// Julian day of Jan 1st, 1900
		// int jday = 2415021;
		while (true) {
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH) + 1;
			int year = cal.get(Calendar.YEAR);
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			dow = (dow + 5) % 7;
			if (year > 2100)
				break;
			int jdayCheck = GtfsLogicalDate.computeJulianDay(year, month, day);
			assertEquals(jdayCheck, jday);
			assertEquals(dow, jday % 7);
			cal.add(Calendar.DATE, 1);
			jday++;
		}
	}

	@Test
	public void testOffset() {
		GtfsLogicalDate date1 = GtfsLogicalDate.getDate(2020, 12, 31);
		GtfsLogicalDate date2 = date1.offset(1);
		assertEquals(1, date2.getDay());
		assertEquals(1, date2.getMonth());
		assertEquals(2021, date2.getYear());

		GtfsLogicalDate date3 = GtfsLogicalDate.getDate(2020, 2, 29);
		GtfsLogicalDate date4 = date3.offset(1);
		assertEquals(1, date4.getDay());
		assertEquals(3, date4.getMonth());
		assertEquals(2020, date4.getYear());
	}

	@Test
	public void testEquals() {
		GtfsLogicalDate date1 = GtfsLogicalDate.getDate(2020, 1, 10);
		GtfsLogicalDate date2 = GtfsLogicalDate.getDate(2020, 1, 10);
		assertEquals(date1, date2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongDateFormat() {
		GtfsLogicalDate.clearCache();
		GtfsLogicalDate.getDate(2020, 2, 30);
	}

	@Test(expected = ParseException.class)
	public void testWrongDateFormat2() throws ParseException {
		GtfsLogicalDate.clearCache();
		GtfsLogicalDate.parseFromYYYYMMDD("20200230");
	}

	@Test(expected = ParseException.class)
	public void testJulianDayParseConflict() throws ParseException {
		GtfsLogicalDate.clearCache();
		// Just make sure the two dates will map to the same julian day
		int jday1 = GtfsLogicalDate.computeJulianDay(2007, 31, 8);
		int jday2 = GtfsLogicalDate.computeJulianDay(2009, 7, 11);
		assertEquals(jday1, jday2);
		// The second date SHOULD throw a parse exception
		// Even if a new date instance is not built due to cache
		GtfsLogicalDate.parseFromYYYYMMDD("20090711");
		GtfsLogicalDate.parseFromYYYYMMDD("20073108");
	}

}
