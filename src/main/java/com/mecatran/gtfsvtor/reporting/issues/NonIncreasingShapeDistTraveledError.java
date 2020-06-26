package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Non increasing shape dist traveled")
public class NonIncreasingShapeDistTraveledError implements ReportIssue {

	private GtfsShape.Id shapeId;
	private GtfsShapePoint point1, point2;

	public NonIncreasingShapeDistTraveledError(GtfsShape.Id shapeId,
			GtfsShapePoint point1, GtfsShapePoint point2) {
		this.shapeId = shapeId;
		this.point1 = point1;
		this.point2 = point2;
	}

	public GtfsShape.Id getShapeId() {
		return shapeId;
	}

	public GtfsShapePoint getPoint1() {
		return point1;
	}

	public GtfsShapePoint getPoint2() {
		return point2;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Non-increasing shape dist traveled for shape ID {0} between seq {1} and {2}: {3} > {4}.",
				fmt.id(shapeId), fmt.id(point1.getPointSequence().toString()),
				fmt.id(point2.getPointSequence().toString()),
				fmt.var(fmt.distance(point1.getShapeDistTraveled())),
				fmt.var(fmt.distance(point2.getShapeDistTraveled())));
	}
}
