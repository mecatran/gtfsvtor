package com.mecatran.gtfsvtor.model;

import java.util.Comparator;
import java.util.Objects;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;

public interface GtfsShapePoint extends GtfsObject<Void> {

	public static final String TABLE_NAME = "shapes.txt";

	public GtfsShape.Id getShapeId();

	public Double getLat();

	public Double getLon();

	/**
	 * @return The coordinates associated to this stop. If one of lat or lon is
	 *         not defined, return null. If both lat and lon are 0.0, the
	 *         coordinates are most likely to be bogus, we also return null
	 *         (undefined). Otherwise return the associated position.
	 */
	public GeoCoordinates getCoordinates();

	public GtfsShapePointSequence getPointSequence();

	/**
	 * @return The point sequence as integer, or Integer.MIN_VALUE if
	 *         invalid/empty/undefined.
	 */
	public int getInternalPointSequence();

	public Double getShapeDistTraveled();

	public interface Builder {

		public Builder withShapeId(GtfsShape.Id id);

		public Builder withCoordinates(Double lat, Double lon);

		public Builder withPointSequence(GtfsShapePointSequence pointSequence);

		public Builder withShapeDistTraveled(Double shapeDistTraveled);

		public GtfsShapePoint build();
	}

	public static final Comparator<GtfsShapePoint> POINT_SEQ_COMPARATOR = new Comparator<GtfsShapePoint>() {
		@Override
		public int compare(GtfsShapePoint point1, GtfsShapePoint point2) {
			// Make sure we do not compare points from different shapes
			assert Objects.equals(point1.getShapeId(), point2.getShapeId());
			// Sort undef point sequence at start
			return Integer.compare(point1.getInternalPointSequence(),
					point2.getInternalPointSequence());
		}
	};
}
