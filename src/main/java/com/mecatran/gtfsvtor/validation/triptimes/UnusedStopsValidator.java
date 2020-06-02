package com.mecatran.gtfsvtor.validation.triptimes;

import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.TripTimesValidator;

public class UnusedStopsValidator implements TripTimesValidator {

	private Set<GtfsStop.Id> unusedStopsIds;

	@Override
	public void start(Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		/* Build the list of all stops */
		unusedStopsIds = dao.getStops()
				.filter(s -> s.getType() == GtfsStopType.STOP)
				.map(GtfsStop::getId).collect(Collectors.toSet());
	}

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		/* Remove all stops from unused stops */
		for (GtfsStopTime stopTime : tripAndTimes.getStopTimes()) {
			unusedStopsIds.remove(stopTime.getStopId());
		}
	}

	@Override
	public void end(Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		for (GtfsStop.Id unusedStopId : unusedStopsIds) {
			GtfsStop stop = dao.getStop(unusedStopId);
			reportSink.report(new UnusedObjectWarning("stop", unusedStopId,
					stop.getSourceInfo(), "stop_id"));
		}
	}
}
