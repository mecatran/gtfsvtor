package com.mecatran.gtfsvtor.validation.dao;

import com.mecatran.gtfsvtor.dao.DaoSpatialIndex;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DifferentStationTooCloseWarning;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

/*
 * Check if a parent station exist nearby a stop who is not the parent.
 *
 * TODO Make the same kind of test for quay -> parent stop
 */
public class DifferentStationTooCloseValidator implements DaoValidator {

	@ConfigurableOption(description = "Distance between stop and station below which a warning is generated")
	private double maxDistanceMetersWarning = 2;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		DaoSpatialIndex spatialIndex = dao.getSpatialIndex();
		ReportSink reportSink = context.getReportSink();

		dao.getStops().forEach(stop -> {
			if (stop.getType() != GtfsStopType.STOP)
				return;
			if (stop.getParentId() == null)
				return;
			GeoCoordinates pStop = stop.getCoordinates();
			if (pStop == null)
				return;
			GtfsStop station = dao.getStop(stop.getParentId());
			if (station == null)
				return;
			GeoCoordinates pStation = station.getCoordinates();
			if (pStation == null)
				return;
			spatialIndex.getStopsAround(pStop, maxDistanceMetersWarning, true)
					.filter(s -> s.getType() == GtfsStopType.STATION)
					.filter(s -> !s.equals(station)).forEach(station2 -> {
						double distance = Geodesics.distanceMeters(pStop,
								station2.getCoordinates());
						reportSink.report(new DifferentStationTooCloseWarning(
								stop, station, station2, distance));
					});
		});
	}
}
