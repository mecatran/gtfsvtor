package com.mecatran.gtfsvtor.model;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class GtfsLogicalDate implements Comparable<GtfsLogicalDate> {

	// TODO Create specific enum for DOW
	public static final int DOW_MONDAY = 0;
	public static final int DOW_TUESDAY = 1;
	public static final int DOW_WEDNESDAY = 2;
	public static final int DOW_THURSDAY = 3;
	public static final int DOW_FRIDAY = 4;
	public static final int DOW_SATURDAY = 5;
	public static final int DOW_SUNDAY = 6;

	private int year;
	private int month;
	private int day;
	private int julianDay;

	private static Map<Integer, GtfsLogicalDate> CACHE = new HashMap<>(10000);

	// You are not allowed to build a date
	private GtfsLogicalDate(int year, int month, int day, int julianDay) {
		// Check if calendar is valid first, otherwise doom will ensue
		GregorianCalendar cal = calendarForYmd(year, month, day);
		int year2 = cal.get(Calendar.YEAR);
		int month2 = cal.get(Calendar.MONTH) + 1;
		int day2 = cal.get(Calendar.DAY_OF_MONTH);
		if (year != year2 || month2 != month || day2 != day) {
			throw new IllegalArgumentException(String.format(
					"Invalid date: %04d-%02d-%02d (did you mean %04d-%02d-%02d ?)",
					year, month, day, year2, month2, day2));
		}
		this.year = year2;
		this.month = month2;
		this.day = day2;
		this.julianDay = julianDay;
	}

	static void clearCache() {
		CACHE.clear();
	}

	/**
	 * Build a date from GTFS format.
	 */
	public static GtfsLogicalDate parseFromYYYYMMDD(String yyyymmdd)
			throws ParseException {
		if (yyyymmdd == null)
			throw new NullPointerException();
		if (yyyymmdd.length() != 8)
			throw new ParseException(
					"Invalid date length (" + yyyymmdd
							+ "), should be 8 but is " + yyyymmdd.length(),
					yyyymmdd.length());
		int year, month, day;
		try {
			year = Integer.parseInt(yyyymmdd.substring(0, 4));
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid format for " + yyyymmdd, 0);
		}
		try {
			month = Integer.parseInt(yyyymmdd.substring(4, 6));
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid format for " + yyyymmdd, 4);
		}
		try {
			day = Integer.parseInt(yyyymmdd.substring(6, 8));
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid format for " + yyyymmdd, 6);
		}
		try {
			return getDate(year, month, day);
		} catch (IllegalArgumentException e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}

	public static GtfsLogicalDate getDate(int year, int month, int day) {
		int julianDay = computeJulianDay(year, month, day);
		// TODO synchronize CACHE?
		return CACHE.computeIfAbsent(julianDay,
				jd -> new GtfsLogicalDate(year, month, day, jd));
	}

	public GtfsLogicalDate offset(int nDays) {
		// The lines below explain why we use a cache
		int julianDay2 = julianDay + nDays;
		GtfsLogicalDate ret = CACHE.get(julianDay2);
		if (ret != null)
			return ret;
		// Use slower code
		GregorianCalendar cal = calendarForYmd(year, month, day);
		cal.add(Calendar.DATE, nDays);
		return getDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));
	}

	public GtfsLogicalDate next() {
		return offset(1);
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public int getJulianDay() {
		return julianDay;
	}

	/**
	 * @return The julian week of the date: the julian day / 7. The "julian
	 *         week" starts on Monday.
	 */
	public int getJulianWeek() {
		return julianDay / 7;
	}

	/**
	 * @return The day of the week. 0 is Monday, 6 is Sunday.
	 */
	public int getDayOfTheWeek() {
		return julianDay % 7;
	}

	/**
	 * See http://en.wikipedia.org/wiki/Julian_day
	 * 
	 * @return The julian day of the given date
	 */
	static int computeJulianDay(int year, int month, int day) {
		int a = (14 - month) / 12;
		int y = year + 4800 - a;
		int m = month + 12 * a - 3;
		int jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400
				- 32045;
		return jdn;
	}

	private GregorianCalendar calendarForYmd(int year, int month, int day) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(julianDay);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GtfsLogicalDate))
			return false;
		GtfsLogicalDate other = (GtfsLogicalDate) obj;
		return other.julianDay == this.julianDay;
	}

	@Override
	public String toString() {
		return String.format("%04d-%02d-%02d", year, month, day);
	}

	@Override
	public int compareTo(GtfsLogicalDate o) {
		return Integer.compare(julianDay, o.julianDay);
	}
}
