package com.mecatran.gtfsvtor.validation.streaming;

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
		if (shapePoint.getPointSequence() != null
				&& shapePoint.getPointSequence().getSequence() < 0)
			reportSink.report(
					new InvalidFieldFormatError(context.getSourceRef(),
							"shape_pt_sequence",
							Integer.toString(shapePoint.getPointSequence()
									.getSequence()),
							"positive integer"),
					context.getSourceInfo());
		if (shapePoint.getShapeDistTraveled() != null
				&& shapePoint.getShapeDistTraveled() < 0.0)
			reportSink.report(
					new InvalidFieldFormatError(context.getSourceRef(),
							"shape_dist_traveled",
							Double.toString(shapePoint.getShapeDistTraveled()),
							"positive floating point"),
					context.getSourceInfo());
	}
}
