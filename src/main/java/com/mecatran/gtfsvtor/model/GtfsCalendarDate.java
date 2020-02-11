package com.mecatran.gtfsvtor.model;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsCalendarDate
		implements GtfsObject<Void>, GtfsObjectWithSourceInfo {

	public static final String TABLE_NAME = "calendar_dates.txt";

	private GtfsCalendar.Id calendarId;
	private GtfsLogicalDate date;
	private GtfsCalendarDateExceptionType exceptionType;

	private DataObjectSourceInfo sourceInfo;

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
	}

	public GtfsCalendar.Id getCalendarId() {
		return calendarId;
	}

	public GtfsLogicalDate getDate() {
		return date;
	}

	public GtfsCalendarDateExceptionType getExceptionType() {
		return exceptionType;
	}

	public static class Builder {
		private GtfsCalendarDate calendarDate;

		public Builder() {
			calendarDate = new GtfsCalendarDate();
		}

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			calendarDate.sourceInfo = sourceInfo;
			return this;
		}

		public Builder withCalendarId(GtfsCalendar.Id calendarId) {
			calendarDate.calendarId = calendarId;
			return this;
		}

		public Builder withDate(GtfsLogicalDate date) {
			calendarDate.date = date;
			return this;
		}

		public Builder withExceptionType(
				GtfsCalendarDateExceptionType exceptionType) {
			calendarDate.exceptionType = exceptionType;
			return this;
		}

		public GtfsCalendarDate build() {
			return calendarDate;
		}
	}
}
