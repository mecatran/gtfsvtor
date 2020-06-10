package com.mecatran.gtfsvtor.validation.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDateExceptionType;
import com.mecatran.gtfsvtor.model.GtfsFeedInfo;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.EmptyCalendarWarning;
import com.mecatran.gtfsvtor.reporting.issues.ExpiredFeedWarning;
import com.mecatran.gtfsvtor.reporting.issues.FutureFeedWarning;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.NoServiceError;
import com.mecatran.gtfsvtor.reporting.issues.NoServiceExceptionWarning;
import com.mecatran.gtfsvtor.reporting.issues.TooManyDaysWithoutServiceIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class CalendarValidator implements DaoValidator {

	@ConfigurableOption(description = "Check for calendars not applicable on any date")
	private boolean checkEmptyCalendars = true;

	@ConfigurableOption(description = "Check for expired feed (last service date in the past)")
	private boolean checkExpired = true;

	@ConfigurableOption(description = "Expired feed cutoff date")
	private GtfsLogicalDate expiredCutoffDate = GtfsLogicalDate
			.today(TimeZone.getDefault());

	@ConfigurableOption(description = "Check for feed not active yet (first service date in the future)")
	private boolean checkFuture = true;

	@ConfigurableOption(description = "Future feed cutoff date")
	private GtfsLogicalDate futureCutoffDate = GtfsLogicalDate
			.today(TimeZone.getDefault());

	@ConfigurableOption(description = "Maximum number of contiguous dates w/o service")
	private int maxDaysWithoutService = 7;

	@ConfigurableOption(description = "Feed effective calendar time span in days below which we do not check for no service exception")
	private int minDaysForCheckingNoServiceException = 182;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();

		calIndex.getAllCalendarIds().forEach(calId -> {
			if (checkEmptyCalendars) {
				if (calIndex.getCalendarApplicableDates(calId).isEmpty()) {
					List<DataObjectSourceRef> sourceRefs = new ArrayList<>();
					GtfsCalendar calendar = dao.getCalendar(calId);
					if (calendar != null) {
						sourceRefs.add(calendar.getSourceRef());
					}
					dao.getCalendarDates(calId).map(date -> date.getSourceRef())
							.forEach(sourceRefs::add);
					reportSink.report(
							new EmptyCalendarWarning(calId, sourceRefs));
				}
			}
		});

		GtfsLogicalDate firstDate = null;
		GtfsLogicalDate lastDate = null;

		List<GtfsLogicalDate> allDates = calIndex.getSortedDates()
				.collect(Collectors.toList());
		if (allDates.isEmpty()) {
			reportSink.report(new NoServiceError());
		} else {
			firstDate = allDates.get(0);
			lastDate = allDates.get(allDates.size() - 1);
			GtfsLogicalDate date = firstDate;
			int daysWoServiceCounter = 0;
			GtfsLogicalDate firstDayWoService = null;
			while (date.compareTo(lastDate) <= 0) {
				long nTripCount = calIndex.getTripCountOnDate(date);
				if (nTripCount == 0) {
					if (firstDayWoService == null)
						firstDayWoService = date;
					daysWoServiceCounter++;
				} else {
					if (daysWoServiceCounter > maxDaysWithoutService) {
						reportSink.report(new TooManyDaysWithoutServiceIssue(
								firstDayWoService, date.offset(-1),
								daysWoServiceCounter));
					}
					firstDayWoService = null;
					daysWoServiceCounter = 0;
				}
				date = date.next();
			}
		}

		// Take first-last date from feed info if present
		// to check for expired / future feed.
		GtfsFeedInfo feedInfo = dao.getFeedInfo();
		if (feedInfo != null) {
			if (feedInfo.getFeedStartDate() != null)
				firstDate = feedInfo.getFeedStartDate();
			if (feedInfo.getFeedEndDate() != null)
				lastDate = feedInfo.getFeedEndDate();
		}

		// Use now in JVM default timezone as default value
		if (lastDate != null && checkExpired
				&& lastDate.compareTo(expiredCutoffDate) < 0) {
			reportSink.report(
					new ExpiredFeedWarning(lastDate, expiredCutoffDate));
		}
		if (firstDate != null && checkFuture
				&& firstDate.compareTo(futureCutoffDate) > 0) {
			reportSink
					.report(new FutureFeedWarning(firstDate, futureCutoffDate));
		}

		// Check for no service exception
		if (firstDate != null && lastDate != null) {
			int rangeDays = GtfsLogicalDate.deltaDays(firstDate, lastDate);
			if (rangeDays > minDaysForCheckingNoServiceException) {
				boolean noException = calIndex.getAllCalendarIds()
						.allMatch(calId -> calIndex
								.getEffectiveExceptionDates(calId)
								.count() == 0);
				if (noException) {
					reportSink.report(
							new NoServiceExceptionWarning(firstDate, lastDate));
				}
			}
		}

		dao.getCalendars().forEach(calendar -> {
			// Calendar is not active any day of the week
			// and does not have any positive exception
			if (IntStream.range(0, 7)
					.allMatch(dow -> !calendar.isActiveOnDow(dow))
					&& dao.getCalendarDates(calendar.getId())
							.filter(caldate -> caldate
									.getExceptionType() == GtfsCalendarDateExceptionType.ADDED)
							.count() == 0) {
				reportSink.report(
						new InvalidFieldValueIssue(calendar.getSourceRef(), "",
								"calendar is not active any day of the week",
								"monday", "tuesday", "wednesday", "thursday",
								"friday", "saturday", "sunday"));
			}
		});
	}
}
