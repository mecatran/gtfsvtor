package com.mecatran.gtfsvtor.dao;

import java.util.Collection;
import java.util.SortedSet;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;

public interface CalendarIndex {

	public static class OverlappingCalendarInfo {
		private int daysCount;
		private GtfsLogicalDate from, to;

		public OverlappingCalendarInfo(int daysCount, GtfsLogicalDate from,
				GtfsLogicalDate to) {
			this.daysCount = daysCount;
			this.from = from;
			this.to = to;
		}

		public int getDaysCount() {
			return daysCount;
		}

		public GtfsLogicalDate getFrom() {
			return from;
		}

		public GtfsLogicalDate getTo() {
			return to;
		}
	}

	public Collection<GtfsCalendar.Id> getAllCalendarIds();

	public SortedSet<GtfsLogicalDate> getCalendarApplicableDates(
			GtfsCalendar.Id calendarId);

	public Collection<GtfsCalendar.Id> getCalendarIdsOnDate(
			GtfsLogicalDate date);

	/**
	 * @return The overlapping calendar info (number of overlapping days,
	 *         from-to range), or null if the calendars do not overlap.
	 */
	public OverlappingCalendarInfo calendarOverlap(GtfsCalendar.Id calendarId1,
			GtfsCalendar.Id calendarId2);
}
