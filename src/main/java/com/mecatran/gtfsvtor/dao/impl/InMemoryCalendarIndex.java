package com.mecatran.gtfsvtor.dao.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
	private Map<List<GtfsCalendar.Id>, OverlappingCalendarInfo> calendarOverlapCache = new HashMap<>();

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

	@Override
	public OverlappingCalendarInfo calendarOverlap(GtfsCalendar.Id calendarId1,
			GtfsCalendar.Id calendarId2) {
		// Note: this function works too if calendarId1 = calendarId2
		List<GtfsCalendar.Id> key = calendarId1.compareTo(calendarId2) < 0
				? Arrays.asList(calendarId1, calendarId2)
				: Arrays.asList(calendarId2, calendarId1);
		/*
		 * TODO Make this caching optional, as for certain pathological GTFS
		 * this may lead to a rather large cache size.
		 */
		return calendarOverlapCache.computeIfAbsent(key, p -> {
			// Compute the number of dates shared by the two calendars
			SortedSet<GtfsLogicalDate> dates1 = getCalendarApplicableDates(
					calendarId1);
			SortedSet<GtfsLogicalDate> dates2 = getCalendarApplicableDates(
					calendarId2);
			if (dates1.isEmpty() || dates2.isEmpty())
				return null;
			// We can do this trick as we have sorted set at hand
			if (dates1.last().compareTo(dates2.first()) < 0
					|| dates2.last().compareTo(dates1.first()) < 0)
				return null;
			// https://stackoverflow.com/questions/2851938/efficiently-finding-the-intersection-of-a-variable-number-of-sets-of-strings/39902694#39902694
			SortedSet<GtfsLogicalDate> smaller, larger;
			if (dates1.size() < dates2.size()) {
				smaller = dates1;
				larger = dates2;
			} else {
				smaller = dates2;
				larger = dates1;
			}
			List<GtfsLogicalDate> intersection = smaller.stream()
					.filter(larger::contains).collect(Collectors.toList());
			if (intersection.isEmpty())
				return null;
			OverlappingCalendarInfo info = new OverlappingCalendarInfo(
					intersection.size(), intersection.get(0),
					intersection.get(intersection.size() - 1));
			return info;
		});
	}

}
