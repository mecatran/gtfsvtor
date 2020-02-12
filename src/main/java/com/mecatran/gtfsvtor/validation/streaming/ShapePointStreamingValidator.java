package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsShapePoint.class)
public class ShapePointStreamingValidator
		implements StreamingValidator<GtfsShapePoint> {

	@Override
	public void validate(Class<? extends GtfsShapePoint> clazz,
			GtfsShapePoint shapePoint, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		// Trip id / stop sequence is primary key and tested by DAO
		checkNonNull(shapePoint::getLat, "shape_pt_lat", context);
		checkNonNull(shapePoint::getLon, "shape_pt_lon", context);
		if (shapePoint.getPointSequence() != null
				&& shapePoint.getPointSequence().getSequence() < 0)
			reportSink.report(new InvalidFieldFormatError(
					context.getSourceInfo(), "shape_pt_sequence",
					Integer.toString(
							shapePoint.getPointSequence().getSequence()),
					"positive integer"));
		if (shapePoint.getShapeDistTraveled() != null
				&& shapePoint.getShapeDistTraveled() < 0.0)
			reportSink.report(new InvalidFieldFormatError(
					context.getSourceInfo(), "shape_dist_traveled",
					Double.toString(shapePoint.getShapeDistTraveled()),
					"positive floating point"));
	}
}
