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

// @DefaultDisabledValidator
public class StopTooCloseValidator implements DaoValidator {

	@ConfigurableOption(description = "Distance between stops below which an info is generated")
	private double minDistanceMeters = 5.0;

	@ConfigurableOption(description = "Distance between stops below which a warning is generated")
	private double minDistanceMetersWarning = 1.0;

	@ConfigurableOption(description = "Distance between stops below which an error is generated")
	private double minDistanceMetersError = -1.0; /* -1 will disable errors */

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();
		DaoSpatialIndex dsi = dao.getSpatialIndex();

		dao.getStops().forEach(stop1 -> {
			GeoCoordinates p = stop1.getCoordinates();
			if (p == null)
				return;
			/*
			 * We filter stops2: different from stop1, same type, and ID greater
			 * than stop1 (to prevent duplicated warnings: A-B and B-A).
			 */
			List<GtfsStop> stops2 = dsi
					.getStopsAround(p, minDistanceMeters, true)
					.filter(stop2 -> !stop2.equals(stop1))
					.filter(stop2 -> stop2.getType().equals(stop1.getType()))
					.filter(stop2 -> stop2.getId().getInternalId()
							.compareTo(stop1.getId().getInternalId()) > 0)
					.collect(Collectors.toList());
			for (GtfsStop stop2 : stops2) {
				double distance = Geodesics.distanceMeters(p,
						stop2.getCoordinates());
				reportSink.report(new StopTooCloseIssue(stop1, stop2, distance,
						distance <= minDistanceMetersError
								? ReportIssueSeverity.ERROR
								: distance <= minDistanceMetersWarning
										? ReportIssueSeverity.WARNING
										: ReportIssueSeverity.INFO));
			}
		});
	}
}
