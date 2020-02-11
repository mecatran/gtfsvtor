package com.mecatran.gtfsvtor.validation.dao;

import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class UnusedObjectsValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		/* Look for unused agencies */
		for (GtfsAgency agency : dao.getAgencies()) {
			if (dao.getRoutesOfAgency(agency.getId()).isEmpty()) {
				reportSink.report(new UnusedObjectWarning(agency.getId(),
						agency.getSourceInfo(), "agency_id"));
			}
		}

		/* Look for unused stops */
		Set<GtfsStop.Id> unusedStopsIds = dao.getStops().stream()
				.filter(s -> s.getType() == GtfsStopType.STOP)
				.map(GtfsStop::getId).collect(Collectors.toSet());
		for (GtfsTrip trip : dao.getTrips()) {
			for (GtfsStopTime stopTime : dao.getStopTimesOfTrip(trip.getId())) {
				unusedStopsIds.remove(stopTime.getStopId());
			}
		}
		for (GtfsStop.Id unusedStopId : unusedStopsIds) {
			GtfsStop stop = dao.getStop(unusedStopId);
			reportSink.report(new UnusedObjectWarning(unusedStopId,
					stop.getSourceInfo(), "stop_id"));
		}
	}
}
