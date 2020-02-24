package com.mecatran.gtfsvtor.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;

import com.mecatran.gtfsvtor.dao.DaoSpatialIndex;
import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsStop;

public class InMemoryDaoSpatialIndex implements DaoSpatialIndex {

	private SpatialIndex spatialIndex;

	public InMemoryDaoSpatialIndex(ReadOnlyDao dao) {
		/*
		 * Note: do not use Quadtree, performance is very bad (around 24 sec for
		 * a stop too close validator on MBTA GTFS, vs 100 ms with STRtree).
		 */
		spatialIndex = new STRtree();
		for (GtfsStop stop : dao.getStops()) {
			GeoCoordinates p = stop.getCoordinates();
			if (p == null)
				continue;
			Envelope env = new Envelope(new Coordinate(p.getLon(), p.getLat()));
			spatialIndex.insert(env, stop);
		}
	}

	@Override
	public Collection<GtfsStop> getStopsAround(GeoCoordinates position,
			double distanceMeters, boolean exact) {
		// TODO Handle +/-180° wrap-around
		double dLat = Geodesics.deltaLat(distanceMeters);
		double dLon = Geodesics.deltaLon(distanceMeters, position.getLat());

		Envelope env = new Envelope(position.getLon() - dLon,
				position.getLon() + dLon, position.getLat() - dLat,
				position.getLat() + dLat);
		@SuppressWarnings("unchecked")
		List<GtfsStop> stops = (List<GtfsStop>) spatialIndex.query(env);
		List<GtfsStop> filteredStops = new ArrayList<>(stops.size());
		for (GtfsStop stop : stops) {
			if (exact) {
				double d = Geodesics.distanceMeters(stop.getCoordinates(),
						position);
				if (d <= distanceMeters)
					filteredStops.add(stop);
			} else {
				filteredStops.add(stop);
			}
		}
		return filteredStops;
	}
}
