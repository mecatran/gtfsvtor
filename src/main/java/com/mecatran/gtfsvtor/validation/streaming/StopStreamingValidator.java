package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkCoordinates;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkUrl;

import com.mecatran.gtfsvtor.geospatial.GeoBounds;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.StopTooCloseToOriginError;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsStop.class)
public class StopStreamingValidator implements StreamingValidator<GtfsStop> {

	@ConfigurableOption(description = "BoundingBox in which the stop's lat/lon should be "
			+ "contained, specify as minLat,minLon,maxLat,maxLon")
	private GeoBounds boundingBox = new GeoBounds(-90, -180, 90, 180);

	@Override
	public void validate(Class<? extends GtfsStop> clazz, GtfsStop stop,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		GtfsStopType stopType = stop.getType();
		boolean namePosMandatory = stopType == GtfsStopType.STOP
				|| stopType == GtfsStopType.STATION
				|| stopType == GtfsStopType.ENTRANCE;
		if (namePosMandatory) {
			checkNonNull(stop::getLat, "stop_lat", context);
			checkNonNull(stop::getLon, "stop_lon", context);
		}
		if (stop.getLat() != null && stop.getLon() != null) {
			if (Math.abs(stop.getLat()) < 1e-3
					&& Math.abs(stop.getLon()) < 1e-3) {
				reportSink.report(new StopTooCloseToOriginError(stop),
						context.getSourceInfo());
			} else {
				checkCoordinates(stop::getLat, stop::getLon, "stop_lat",
						"stop_lon", boundingBox, context);
			}
		}
		checkUrl(stop::getUrl, "stop_url", context);
		if (stop.getParentId() != null && stop.getTimezone() != null) {
			reportSink.report(new InvalidFieldValueIssue(context.getSourceRef(),
					stop.getTimezone().getID(),
					"stop having a parent should not define a timezone",
					"stop_timezone"), context.getSourceInfo());
		}
		if (stop.getName() != null && stop.getDescription() != null
				&& stop.getName().equalsIgnoreCase(stop.getDescription())) {
			reportSink.report(
					new InvalidFieldValueIssue(context.getSourceRef(),
							stop.getDescription(),
							"Description should not be the same as name",
							"stop_desc", "stop_name")
									.withSeverity(ReportIssueSeverity.WARNING),
					context.getSourceInfo());
		}
		// Note: Parent station validity is checked in the reference validator
	}
}
