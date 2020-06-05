package com.mecatran.gtfsvtor.reporting.issues;

import java.util.ArrayList;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Useless calendar date")
public class UselessCalendarDateWarning implements ReportIssue {

	private GtfsCalendarDate calendarDate;
	private GtfsCalendar calendar;
	private List<SourceRefWithFields> sourceInfos;

	public UselessCalendarDateWarning(GtfsCalendarDate calendarDate,
			GtfsCalendar calendar) {
		this.calendarDate = calendarDate;
		this.calendar = calendar;
		this.sourceInfos = new ArrayList<>();
		sourceInfos.add(new SourceRefWithFields(calendarDate.getSourceRef(),
				"service_id"));
		if (calendar != null)
			sourceInfos.add(new SourceRefWithFields(calendar.getSourceRef(),
					"service_id"));
	}

	public GtfsCalendarDate getCalendarDate() {
		return calendarDate;
	}

	public GtfsCalendar getCalendar() {
		return calendar;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		if (calendar != null) {
			switch (calendarDate.getExceptionType()) {
			case ADDED:
				// Positive exception
				fmt.text(
						"Useless calendar date: it is specified as 'added' (type {0}), but the calendar {1} is already active on {2}.",
						fmt.pre(GtfsCalendarDateExceptionType.ADDED.getValue()),
						fmt.id(calendarDate.getCalendarId()),
						fmt.date(calendarDate.getDate()));
				break;
			case REMOVED:
				// Negative exception
				fmt.text(
						"Useless calendar date: it is specified as 'removed' (type {0}), but the calendar {1} is not active on {2}.",
						fmt.pre(GtfsCalendarDateExceptionType.REMOVED
								.getValue()),
						fmt.id(calendarDate.getCalendarId()),
						fmt.date(calendarDate.getDate()));
				break;
			}
		} else {
			// Since there is no calendar, this is a negative exception.
			fmt.text(
					"Useless calendar date: it is specified as 'removed' (type {0}), but there is no calendar defined with the ID {1}.",
					fmt.pre(GtfsCalendarDateExceptionType.REMOVED.getValue()),
					fmt.id(calendarDate.getCalendarId()));
		}
	}
}
