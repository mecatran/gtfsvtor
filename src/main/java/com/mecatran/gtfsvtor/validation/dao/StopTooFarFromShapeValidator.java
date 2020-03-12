package com.mecatran.gtfsvtor.validation.dao;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex.ProjectedPoint;
import com.mecatran.gtfsvtor.dao.LinearGeometryIndex.ProjectedShapePattern;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromShapeIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class StopTooFarFromShapeValidator implements DaoValidator {

	@ConfigurableOption
	private double maxWarningDistanceMeters = 50.0;

	@ConfigurableOption
	private double maxErrorDistanceMeters = 100.0;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		LinearGeometryIndex lgi = dao.getLinearGeometryIndex();
		ReportSink reportSink = context.getReportSink();

		for (ProjectedShapePattern psp : lgi.getProjectedPatterns()) {
			if (!psp.getShapeId().isPresent()) {
				// Do not test if no shape
				continue;
			}
			for (ProjectedPoint pp : psp.getProjectedPoints()) {
				double dts = pp.getDistanceToShapeMeters();
				ReportIssueSeverity severity = null;
				if (dts > maxErrorDistanceMeters) {
					severity = ReportIssueSeverity.ERROR;
				} else if (dts > maxWarningDistanceMeters) {
					severity = ReportIssueSeverity.WARNING;
				}
				if (severity == null) {
					continue;
				}
				// We have an issue here
				GtfsStop stop = dao.getStop(pp.getStopId());
				StopTooFarFromShapeIssue issue = new StopTooFarFromShapeIssue(
						stop, pp.getStopSequence(), psp.getShapeId().get(),
						psp.getTripIds().iterator().next(),
						pp.getDistanceToShapeMeters(), pp.getArcLengthMeters(),
						pp.getProjectedPoint(), severity);
				reportSink.report(issue);
			}
		}
	}
}
