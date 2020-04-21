package com.mecatran.gtfsvtor.dao;

import java.util.stream.Stream;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsStop;

public interface DaoSpatialIndex {

	public Stream<GtfsStop> getStopsAround(GeoCoordinates position,
			double distanceMeters, boolean exact);
}
