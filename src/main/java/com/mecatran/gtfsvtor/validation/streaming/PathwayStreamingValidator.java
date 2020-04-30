package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkFormat;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsPathwayMode;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.TooFastWalkingSpeed;
import com.mecatran.gtfsvtor.reporting.issues.WrongPathwayStopTypeError;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsPathway.class)
public class PathwayStreamingValidator
		implements StreamingValidator<GtfsPathway> {

	@ConfigurableOption(description = "Fast walking speed, in meters/seconds, above which a warning is generated")
	private double fastWalkingSpeedMps = 2.0;

	@ConfigurableOption(description = "Fast walking speed slack time, in seconds (added to transfer time before speed computation)")
	private int walkingTimeSlackSec = 120;

	@Override
	public void validate(Class<? extends GtfsPathway> clazz,
			GtfsPathway pathway, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();

		// ID is tested by DAO layer, mandatory fields by loader
		checkNonNull(pathway::getFromStopId, "from_stop_id", context);
		checkNonNull(pathway::getToStopId, "to_stop_id", context);
		checkFormat(pathway::getLength, "length", context, l -> l >= 0.,
				"non-negative float");
		checkFormat(pathway::getTraversalTime, "traversal_time", context,
				t -> t > 0, "positive integer");
		checkFormat(pathway::getStairCount, "stair_count", context, c -> c != 0,
				"non-null integer");
		checkFormat(pathway::getMinWidth, "min_width", context, w -> w > 0.,
				"positive float");

		ReadOnlyDao dao = context.getPartialDao();

		// check from/to stops reference
		GtfsStop fromStop = null;
		GtfsStop toStop = null;
		if (pathway.getFromStopId() != null) {
			fromStop = dao.getStop(pathway.getFromStopId());
			if (fromStop == null) {
				reportSink.report(new InvalidReferenceError(
						context.getSourceInfo(), "from_stop_id",
						pathway.getFromStopId().getInternalId(),
						GtfsStop.TABLE_NAME, "stop_id"));
			} else {
				if (fromStop.getType() == GtfsStopType.STATION) {
					reportSink.report(new WrongPathwayStopTypeError(
							context.getSourceInfo(), pathway, fromStop,
							"from_stop_id"));
				}
			}
		}
		if (pathway.getToStopId() != null) {
			toStop = dao.getStop(pathway.getToStopId());
			if (toStop == null) {
				reportSink.report(new InvalidReferenceError(
						context.getSourceInfo(), "to_stop_id",
						pathway.getToStopId().getInternalId(),
						GtfsStop.TABLE_NAME, "stop_id"));
			} else {
				if (toStop.getType() == GtfsStopType.STATION) {
					reportSink.report(new WrongPathwayStopTypeError(
							context.getSourceInfo(), pathway, toStop,
							"to_stop_id"));
				}
			}
		}

		// Check from-to distance and walk speed (WALKWAYs only)
		// TODO Check for speed for other pathway types
		if (fromStop != null && toStop != null
				&& pathway.getPathwayMode() != null
				&& pathway.getPathwayMode() == GtfsPathwayMode.WALKWAY
				&& pathway.getTraversalTime() != null
				&& pathway.getTraversalTime() > 0) {
			GeoCoordinates p1 = fromStop.getCoordinates();
			GeoCoordinates p2 = toStop.getCoordinates();
			if (p1 != null && p2 != null) {
				double d = Geodesics.fastDistanceMeters(p1, p2);
				double speedMps = d
						/ (pathway.getTraversalTime() + walkingTimeSlackSec);
				if (speedMps > fastWalkingSpeedMps) {
					reportSink.report(new TooFastWalkingSpeed(
							context.getSourceInfo(), fromStop, toStop, d,
							speedMps, fastWalkingSpeedMps,
							ReportIssueSeverity.WARNING));
				}
			}
		}

		// TODO other checks?
	}
}
