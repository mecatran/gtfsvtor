package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

public class GtfsCalendar
		implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "calendar.txt";

	private GtfsCalendar.Id id;
	private Boolean monday;
	private Boolean tuesday;
	private Boolean wednesday;
	private Boolean thursday;
	private Boolean friday;
	private Boolean saturday;
	private Boolean sunday;
	private GtfsLogicalDate startDate;
	private GtfsLogicalDate endDate;

	private long sourceLineNumber;

	public GtfsCalendar.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public Boolean isMonday() {
		return monday;
	}

	public Boolean isTuesday() {
		return tuesday;
	}

	public Boolean isWednesday() {
		return wednesday;
	}

	public Boolean isThursday() {
		return thursday;
	}

	public Boolean isFriday() {
		return friday;
	}

	public Boolean isSaturday() {
		return saturday;
	}

	public Boolean isSunday() {
		return sunday;
	}

	public boolean isActiveOnDow(int dow) {
		switch (dow) {
		case GtfsLogicalDate.DOW_MONDAY:
			return monday != null && monday;
		case GtfsLogicalDate.DOW_TUESDAY:
			return tuesday != null && tuesday;
		case GtfsLogicalDate.DOW_WEDNESDAY:
			return wednesday != null && wednesday;
		case GtfsLogicalDate.DOW_THURSDAY:
			return thursday != null && thursday;
		case GtfsLogicalDate.DOW_FRIDAY:
			return friday != null && friday;
		case GtfsLogicalDate.DOW_SATURDAY:
			return saturday != null && saturday;
		case GtfsLogicalDate.DOW_SUNDAY:
			return sunday != null && sunday;
		default:
			throw new IllegalArgumentException(
					"Day of week index out of range: " + dow);
		}
	}

	public GtfsLogicalDate getStartDate() {
		return startDate;
	}

	public GtfsLogicalDate getEndDate() {
		return endDate;
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsCalendar> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsCalendar.Id.class);
		}
	}

	public static class Builder {
		private GtfsCalendar calendar;

		public Builder(String id) {
			calendar = new GtfsCalendar();
			calendar.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			calendar.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withDow(Boolean monday, Boolean tuesday,
				Boolean wednesday, Boolean thursday, Boolean friday,
				Boolean saturday, Boolean sunday) {
			calendar.monday = monday;
			calendar.tuesday = tuesday;
			calendar.wednesday = wednesday;
			calendar.thursday = thursday;
			calendar.friday = friday;
			calendar.saturday = saturday;
			calendar.sunday = sunday;
			return this;
		}

		public Builder withStartDate(GtfsLogicalDate startDate) {
			calendar.startDate = startDate;
			return this;
		}

		public Builder withEndDate(GtfsLogicalDate endDate) {
			calendar.endDate = endDate;
			return this;
		}

		public GtfsCalendar build() {
			return calendar;
		}
	}
}
