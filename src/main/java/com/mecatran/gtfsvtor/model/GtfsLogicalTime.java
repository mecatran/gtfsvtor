package com.mecatran.gtfsvtor.model;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class GtfsLogicalTime implements Comparable<GtfsLogicalTime> {

	/*
	 * Seconds since (noon minus twelve hours), that is seconds since midnight
	 * on days which are not subject to time-saving change
	 */
	private int ssm;

	private static Map<Integer, GtfsLogicalTime> CACHE = new HashMap<>(
			3 * 24 * 60 * 60);

	// You are not allowed to build a time
	private GtfsLogicalTime(int secondsSinceMidnight) {
		this.ssm = secondsSinceMidnight;
	}

	/**
	 * Build a date from GTFS format.
	 */
	public static GtfsLogicalTime parseFromHH_MM_SS(String hh_mm_ss)
			throws ParseException {
		if (hh_mm_ss == null || hh_mm_ss.isEmpty())
			return null;
		int len = hh_mm_ss.length();
		if ((len != 7 && len != 8) || hh_mm_ss.charAt(len - 6) != ':'
				|| hh_mm_ss.charAt(len - 3) != ':')
			throw new ParseException(
					"Invalid time format, should be hh:mm:ss. Found: "
							+ hh_mm_ss,
					0);
		String hstr = hh_mm_ss.substring(0, len - 6);
		String mstr = hh_mm_ss.substring(len - 5, len - 3);
		String sstr = hh_mm_ss.substring(len - 2, len);
		int hour, minute, second;
		try {
			hour = Integer.parseInt(hstr);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid format for hour: " + hstr, 0);
		}
		try {
			minute = Integer.parseInt(mstr);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid format for minute: " + mstr, 0);
		}
		try {
			second = Integer.parseInt(sstr);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid format for second: " + sstr, 0);
		}
		// TODO Check max value for hour? 7 days is enough? Can it be negative?
		if (hour < 0 || hour > 24 * 7)
			throw new ParseException("Hour out of range: " + hour, 0);
		if (minute < 0 || minute > 60)
			throw new ParseException("Minute out of range: " + minute, 0);
		if (second < 0 || second > 60)
			throw new ParseException("Second out of range: " + second, 0);
		return getTime(hour, minute, second);
	}

	public static GtfsLogicalTime getTime(int hour, int minute, int second) {
		int ssm = hour * 3600 + minute * 60 + second;
		// TODO synchronize CACHE?
		return CACHE.computeIfAbsent(ssm, GtfsLogicalTime::new);
	}

	public int getHour() {
		return ssm / 3600;
	}

	public int getMinute() {
		return (ssm / 60) % 60;
	}

	public int getSecond() {
		return ssm % 60;
	}

	public int getSecondSinceMidnight() {
		return ssm;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(ssm);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GtfsLogicalTime))
			return false;
		GtfsLogicalTime other = (GtfsLogicalTime) obj;
		return other.ssm == ssm;
	}

	@Override
	public String toString() {
		return String.format("%02d:%02d:%02d", getHour(), getMinute(),
				getSecond());
	}

	@Override
	public int compareTo(GtfsLogicalTime o) {
		return Integer.compare(ssm, o.ssm);
	}
}
