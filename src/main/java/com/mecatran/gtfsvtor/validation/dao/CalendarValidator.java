package com.mecatran.gtfsvtor.validation.dao;

import java.util.ArrayList;
import java.util.List;

import com.mecatran.gtfsvtor.dao.CalendarIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.EmptyCalendarWarning;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class CalendarValidator implements DaoValidator {

	@ConfigurableOption
	private Boolean checkEmptyCalendars = true;

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
	}
}
