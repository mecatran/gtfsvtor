package com.mecatran.gtfsvtor.dao;

import java.util.Collection;
import java.util.SortedSet;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;

public interface CalendarIndex {

	public Collection<GtfsCalendar.Id> getAllCalendarIds();

	public SortedSet<GtfsLogicalDate> getCalendarApplicableDates(
			GtfsCalendar.Id calendarId);

	public Collection<GtfsCalendar.Id> getCalendarIdsOnDate(
			GtfsLogicalDate date);
}
