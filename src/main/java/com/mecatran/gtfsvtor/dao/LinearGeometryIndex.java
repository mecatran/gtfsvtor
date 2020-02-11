package com.mecatran.gtfsvtor.dao;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsStopTime;

public interface LinearGeometryIndex {

	public interface ProjectedPoint {

		/**
		 * @return A linear distance on shape, in meters, from the first trip
		 *         stop point, up to this projected point on shape. The value
		 *         itself is useless, it is only useful to make the delta
		 *         between two values (linear distance from point to point).
		 */
		public double getLinearDistanceMeters();

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
		 * @return True if a shape is associated to the trip, false otherwise.
		 */
		public boolean hasShape();
	}

	public ProjectedPoint getProjectedPoint(GtfsStopTime stopTime);

	public Double getLinearDistance(GtfsStopTime stopTime1,
			GtfsStopTime stopTime2);
}
