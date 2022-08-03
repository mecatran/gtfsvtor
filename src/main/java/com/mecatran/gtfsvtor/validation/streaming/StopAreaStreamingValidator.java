package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopArea;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsStopArea.class)
public class StopAreaStreamingValidator
		implements StreamingValidator<GtfsStopArea> {

	@Override
	public void validate(Class<? extends GtfsStopArea> clazz,
			GtfsStopArea stopArea, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();

		// Check area and stop reference
		if (stopArea.getId() != null) {
			if (dao.getArea(stopArea.getId().getAreaId()) == null) {
				reportSink.report(
						new InvalidReferenceError(context.getSourceRef(),
								"area_id",
								stopArea.getId().getAreaId().getInternalId(),
								GtfsArea.TABLE_NAME, "area_id"),
						context.getSourceInfo());
			}
			if (dao.getStop(stopArea.getId().getStopId()) == null) {
				reportSink.report(
						new InvalidReferenceError(context.getSourceRef(),
								"stop_id",
								stopArea.getId().getStopId().getInternalId(),
								GtfsStop.TABLE_NAME, "stop_id"),
						context.getSourceInfo());
			}
		}
	}
}
