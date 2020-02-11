package com.mecatran.gtfsvtor.dao;

import java.util.Collection;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsStop;

public interface DaoSpatialIndex {

	public Collection<GtfsStop> getStopsAround(GeoCoordinates position,
			double distanceMeters, boolean exact);
}
