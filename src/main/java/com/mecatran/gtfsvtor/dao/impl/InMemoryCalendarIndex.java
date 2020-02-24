package com.mecatran.gtfsvtor.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendar.Id;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;

public class InMemoryCalendarIndex implements CalendarIndex {

	private Set<GtfsCalendar.Id> allCalendarIds = new HashSet<>();
	private Map<GtfsCalendar.Id, SortedSet<GtfsLogicalDate>> datesPerCalendar = new HashMap<>();
	private SetMultimap<GtfsLogicalDate, GtfsCalendar.Id> calendarsPerDate = HashMultimap
			.create();

	protected InMemoryCalendarIndex(ReadOnlyDao dao) {
		/*
		 * Here we do not validate any fields for mandatory values. If not
		 * provided, we just ignore them or assume sane default values (null is
		 * empty or invalid day of the week assumed as not active for example).
		 */
		for (GtfsCalendar calendar : dao.getCalendars()) {
			allCalendarIds.add(calendar.getId());
			if (calendar.getStartDate() == null
					|| calendar.getEndDate() == null) {
				// Ignore incomplete calendar
				continue;
			}
			GtfsLogicalDate date = calendar.getStartDate();
			while (date.compareTo(calendar.getEndDate()) <= 0) {
				int dow = date.getDayOfTheWeek();
				boolean active = calendar.isActiveOnDow(dow);
				if (active) {
					SortedSet<GtfsLogicalDate> datesForId = datesPerCalendar
							.get(calendar.getId());
					if (datesForId == null) {
						datesForId = new TreeSet<>();
						datesPerCalendar.put(calendar.getId(), datesForId);
					}
					datesForId.add(date);
				}
				date = date.next();
			}
		}
		/* Handle exceptions */
		for (GtfsCalendarDate calDate : dao.getCalendarDates()) {
			allCalendarIds.add(calDate.getCalendarId());
			if (calDate.getDate() == null
					|| calDate.getExceptionType() == null) {
				// Ignore incomplete calendar date
				continue;
			}
			SortedSet<GtfsLogicalDate> datesForId = datesPerCalendar
					.get(calDate.getCalendarId());
			if (datesForId == null) {
				if (calDate
						.getExceptionType() == GtfsCalendarDateExceptionType.REMOVED) {
					// No action needed, no-op removal
					continue;
				}
				datesForId = new TreeSet<>();
				datesPerCalendar.put(calDate.getCalendarId(), datesForId);
			}
			switch (calDate.getExceptionType()) {
			case ADDED:
				datesForId.add(calDate.getDate());
				break;
			case REMOVED:
				datesForId.remove(calDate.getDate());
				break;
			}
		}
		/* Build the reversed index: calendar IDs for each date */
		for (Map.Entry<GtfsCalendar.Id, SortedSet<GtfsLogicalDate>> kv : datesPerCalendar
				.entrySet()) {
			GtfsCalendar.Id calendarId = kv.getKey();
			for (GtfsLogicalDate date : kv.getValue()) {
				calendarsPerDate.put(date, calendarId);
			}
		}
	}

	@Override
	public Collection<Id> getAllCalendarIds() {
		return Collections.unmodifiableSet(allCalendarIds);
	}

	@Override
	public SortedSet<GtfsLogicalDate> getCalendarApplicableDates(
			GtfsCalendar.Id calendarId) {
		SortedSet<GtfsLogicalDate> dates = datesPerCalendar.get(calendarId);
		if (dates == null)
			return Collections.emptySortedSet();
		return Collections.unmodifiableSortedSet(dates);
	}

	@Override
	public Collection<GtfsCalendar.Id> getCalendarIdsOnDate(
			GtfsLogicalDate date) {
		return Collections.unmodifiableCollection(calendarsPerDate.get(date));
	}

}
