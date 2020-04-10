package com.mecatran.gtfsvtor.validation.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.EmptyCalendarWarning;
import com.mecatran.gtfsvtor.reporting.issues.ExpiredFeedWarning;
import com.mecatran.gtfsvtor.reporting.issues.FutureFeedWarning;
import com.mecatran.gtfsvtor.reporting.issues.NoServiceError;
import com.mecatran.gtfsvtor.reporting.issues.TooManyDaysWithoutServiceIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class CalendarValidator implements DaoValidator {

	@ConfigurableOption(description = "Check for calendars not applicable on any date")
	private boolean checkEmptyCalendars = true;

	@ConfigurableOption(description = "Check for expired feed (last service date in the past)")
	private boolean checkExpired = true;

	@ConfigurableOption(description = "Expired feed cutoff date. Default to 'today' in default TZ if none.")
	private GtfsLogicalDate expiredCutoffDate = null;

	@ConfigurableOption(description = "Check for feed not active yet (first service date in the future)")
	private boolean checkFuture = true;

	@ConfigurableOption(description = "Future feed cutoff date. Default to 'today' in default TZ if none.")
	private GtfsLogicalDate futureCutoffDate = null;

	@ConfigurableOption(description = "Maximum number of contiguous dates w/o service")
	private int maxDaysWithoutService = 7;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();

		for (GtfsCalendar.Id calId : calIndex.getAllCalendarIds()) {
			if (checkEmptyCalendars) {
				if (calIndex.getCalendarApplicableDates(calId).isEmpty()) {
					List<DataObjectSourceInfo> sourceInfos = new ArrayList<>();
					GtfsCalendar calendar = dao.getCalendar(calId);
					if (calendar != null) {
						sourceInfos.add(calendar.getSourceInfo());
					}
					for (GtfsCalendarDate date : dao.getCalendarDates(calId)) {
						sourceInfos.add(date.getSourceInfo());
					}
					reportSink.report(
							new EmptyCalendarWarning(calId, sourceInfos));
				}
			}
		}

		List<GtfsLogicalDate> allDates = calIndex.getSortedDates();
		if (allDates.isEmpty()) {
			reportSink.report(new NoServiceError());
		} else {
			GtfsLogicalDate firstDate = allDates.get(0);
			GtfsLogicalDate lastDate = allDates.get(allDates.size() - 1);
			GtfsLogicalDate date = firstDate;
			int daysWoServiceCounter = 0;
			GtfsLogicalDate firstDayWoService = null;
			while (date.compareTo(lastDate) <= 0) {
				int nTripCount = calIndex.getTripCountOnDate(date);
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

			// Use now in JVM default timezone as default value
			GtfsLogicalDate today = GtfsLogicalDate
					.today(TimeZone.getDefault());
			if (checkExpired) {
				GtfsLogicalDate cutoff = expiredCutoffDate == null ? today
						: expiredCutoffDate;
				if (lastDate.compareTo(cutoff) < 0) {
					reportSink.report(new ExpiredFeedWarning(lastDate, cutoff));
				}
			}
			if (checkFuture) {
				GtfsLogicalDate cutoff = futureCutoffDate == null ? today
						: futureCutoffDate;
				if (firstDate.compareTo(cutoff) > 0) {
					reportSink.report(new FutureFeedWarning(firstDate, cutoff));
				}
			}
		}
	}
}
