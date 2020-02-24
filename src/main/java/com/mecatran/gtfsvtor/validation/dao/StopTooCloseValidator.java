package com.mecatran.gtfsvtor.validation.dao;

import java.util.List;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.DaoSpatialIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.StopTooCloseIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;

// @DefaultDisabledValidator
public class StopTooCloseValidator implements DaoValidator {

	@ConfigurableOption
	private double minDistanceMeters = 5.0;

	@ConfigurableOption
	private double minWarningDistanceMeters = 1.0;

	/* -1 will disable errors */
	@ConfigurableOption
	private double minErrorDistanceMeters = -1.0;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		DaoSpatialIndex dsi = dao.getSpatialIndex();

		for (GtfsStop stop1 : dao.getStops()) {
			GeoCoordinates p = stop1.getCoordinates();
			if (p == null)
				continue;
			/*
			 * We filter stops2: different from stop1, same type, and ID greater
			 * than stop1 (to prevent duplicated warnings: A-B and B-A).
			 */
			List<GtfsStop> stops2 = dsi
					.getStopsAround(p, minDistanceMeters, true).stream()
					.filter(stop2 -> !stop2.equals(stop1))
					.filter(stop2 -> stop2.getType().equals(stop1.getType()))
					.filter(stop2 -> stop2.getId().getInternalId()
							.compareTo(stop1.getId().getInternalId()) > 0)
					.collect(Collectors.toList());
			for (GtfsStop stop2 : stops2) {
				double distance = Geodesics.distanceMeters(p,
						stop2.getCoordinates());
				reportSink.report(new StopTooCloseIssue(stop1, stop2, distance,
						distance <= minErrorDistanceMeters
								? ReportIssueSeverity.ERROR
								: distance <= minWarningDistanceMeters
										? ReportIssueSeverity.WARNING
										: ReportIssueSeverity.INFO));
			}
		}
	}
}
