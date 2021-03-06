package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.NonIncreasingShapeDistTraveledError;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class ShapeDistValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		dao.getShapeIds().forEach(shapeId -> {
			List<GtfsShapePoint> shapePoints = dao.getPointsOfShape(shapeId);
			GtfsShapePoint lastShapePoint = null;
			for (GtfsShapePoint shapePoint : shapePoints) {
				if (lastShapePoint != null) {
					if (lastShapePoint.getShapeDistTraveled() != null
							&& shapePoint.getShapeDistTraveled() != null
							&& lastShapePoint
									.getShapeDistTraveled() > shapePoint
											.getShapeDistTraveled()) {
						reportSink.report(
								new NonIncreasingShapeDistTraveledError(shapeId,
										lastShapePoint, shapePoint));
					}
				}
				lastShapePoint = shapePoint;
			}
		});
	}
}
