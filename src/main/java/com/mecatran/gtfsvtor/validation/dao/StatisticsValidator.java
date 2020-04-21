package com.mecatran.gtfsvtor.validation.dao;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.StatisticsInfo;
import com.mecatran.gtfsvtor.validation.DaoValidator;

/**
 * This validator only generate informational message on the number of loaded
 * entities in the DAO.
 */
public class StatisticsValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		reportSink.report(new StatisticsInfo(String.format(
				"Loaded %d feed-info, %d agencies, %d routes, %d stops, %d calendars, %d calendar dates, %d trips, %d stop times, %d transfers, %d pathways, %d fare attributes, %d fare rules",
				dao.getFeedInfo() == null ? 0 : 1, dao.getAgencies().count(),
				dao.getRoutes().count(), dao.getStops().count(),
				dao.getCalendars().count(), dao.getCalendarDates().count(),
				dao.getTrips().count(), dao.getStopTimesCount(),
				dao.getTransfers().count(), dao.getPathways().count(),
				dao.getFareAttributes().count(), dao.getFareRulesCount())));
	}
}
