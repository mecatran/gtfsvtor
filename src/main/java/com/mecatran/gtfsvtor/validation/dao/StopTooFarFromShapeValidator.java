package com.mecatran.gtfsvtor.validation.dao;

import java.util.Optional;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex.ProjectedPoint;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromShapeIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class StopTooFarFromShapeValidator implements DaoValidator {

	@ConfigurableOption(description = "Distance from stop to projected point on shape above which a warning is generated")
	private double maxDistanceMetersWarning = 50.0;

	@ConfigurableOption(description = "Distance from stop to projected point on shape above which an error is generated")
	private double maxDistanceMetersError = 100.0;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();
		ReportSink reportSink = context.getReportSink();

		lgi.getProjectedPatterns().filter(psp -> psp.getShapeId().isPresent())
				.forEach(psp -> {
					for (ProjectedPoint pp : psp.getProjectedPoints()) {
						Optional<Double> odts = pp.getDistanceToShapeMeters();
						if (!odts.isPresent()) {
							// In case of invalid stop or coordinate, do not
							// report an issue
							continue;
						}
						double dts = odts.get();
						ReportIssueSeverity severity = null;
						if (dts > maxDistanceMetersError) {
							severity = ReportIssueSeverity.ERROR;
						} else if (dts > maxDistanceMetersWarning) {
							severity = ReportIssueSeverity.WARNING;
						}
						if (severity == null) {
							continue;
						}
						// We have an issue here
						GtfsStop stop = dao.getStop(pp.getStopId());
						StopTooFarFromShapeIssue issue = new StopTooFarFromShapeIssue(
								stop, pp.getStopSequence(),
								psp.getShapeId().get(),
								psp.getTripIds().iterator().next(), dts,
								pp.getArcLengthMeters().orElse(Double.NaN),
								pp.getProjectedPoint().orElse(null), severity);
						reportSink.report(issue);
					}
				});
	}
}
