package com.mecatran.gtfsvtor.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;

public interface LinearGeometryIndex {

	public interface ProjectedShapePattern {

		/**
		 * @return The list of point projection for this pattern.
		 */
		public List<? extends ProjectedPoint> getProjectedPoints();

		/**
		 * @return The list of all trip IDs for this pattern projection.
		 */
		public Collection<GtfsTrip.Id> getTripIds();

		/**
		 * @return The shape ID associated to this projection, if any.
		 */
		public Optional<GtfsShape.Id> getShapeId();
	}

	public interface ProjectedPoint {

		/**
		 * @return The arc-length coordinate on shape, in meters, from the first
		 *         trip stop point, up to this projected point on shape. The
		 *         value itself is useless, it is only useful to make the delta
		 *         between two values (arc-length distance from point to point).
		 */
		public double getArcLengthMeters();

		/**
		 * @return Distance from the stop to the projected point on shape used
		 *         for the linear distance. Note: this is not always the smaller
		 *         distance from the stop to the shape (for example if
		 *         shape_dist_traveled is provided, or in case of loops on
		 *         shapes w/o it). For shapeless trips, always 0.
		 */
		public double getDistanceToShapeMeters();

		/**
		 * @return Location of the projected point on the shape used to compute
		 *         linear distance. Note: this is not always the location of the
		 *         nearest point from the stop on shape (same remark as above).
		 *         For shapeless trips, always the location of the stop.
		 */
		public GeoCoordinates getProjectedPoint();

		/**
		 * @return The stop ID which is projected.
		 */
		public GtfsStop.Id getStopId();

		/**
		 * @return The stop sequence of the stop time in the trip pattern which
		 *         is projected.
		 */
		public GtfsTripStopSequence getStopSequence();
	}

	public ProjectedPoint getProjectedPoint(GtfsStopTime stopTime);

	public Double getLinearDistance(GtfsStopTime stopTime1,
			GtfsStopTime stopTime2);

	public Collection<? extends ProjectedShapePattern> getProjectedPatterns();
}
