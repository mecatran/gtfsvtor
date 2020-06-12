package com.mecatran.gtfsvtor.dao.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
		dao.getStops().forEach(stop -> {
			Optional<GeoCoordinates> p = stop.getValidCoordinates();
			if (p.isPresent()) {
				Envelope env = new Envelope(
						new Coordinate(p.get().getLon(), p.get().getLat()));
				spatialIndex.insert(env, stop);
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<GtfsStop> getStopsAround(GeoCoordinates position,
			double distanceMeters, boolean exact) {
		// TODO Handle +/-180° wrap-around
		double dLat = Geodesics.deltaLat(distanceMeters);
		double dLon = Geodesics.deltaLon(distanceMeters, position.getLat());

		Envelope env = new Envelope(position.getLon() - dLon,
				position.getLon() + dLon, position.getLat() - dLat,
				position.getLat() + dLat);
		return ((List<GtfsStop>) spatialIndex.query(env)).stream().filter(
				// For indexed stops, we expect valid coordinates
				stop -> exact ?
						Geodesics.distanceMeters(stop.getValidCoordinates().get(), position)
								<= distanceMeters : true);
	}
}
