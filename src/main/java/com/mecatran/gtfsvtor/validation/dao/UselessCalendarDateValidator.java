package com.mecatran.gtfsvtor.validation.dao;

import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.UselessCalendarDateWarning;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class UselessCalendarDateValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		CalendarIndex calIndex = dao.getCalendarIndex();
		ReportSink reportSink = context.getReportSink();

		calIndex.getAllCalendarIds().forEach(calId -> {
			// Only process calendar dates with a date
			Set<GtfsCalendarDate> uselessExceptions = dao
					.getCalendarDates(calId)
					.filter(calDate -> calDate.getDate() != null
							&& calDate.getExceptionType() != null)
					.collect(Collectors.toSet());
			calIndex.getEffectiveExceptionDates(calId)
					.forEach(caldate -> uselessExceptions.remove(caldate));
			GtfsCalendar calendar = dao.getCalendar(calId); // May be null
			uselessExceptions.forEach(calDate -> reportSink
					.report(new UselessCalendarDateWarning(calDate, calendar)));
		});
	}
}
