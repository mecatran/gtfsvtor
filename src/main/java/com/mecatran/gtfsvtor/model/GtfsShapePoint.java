package com.mecatran.gtfsvtor.model;

import java.util.Comparator;
import java.util.Objects;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;

public class GtfsShapePoint implements GtfsObject<Void> {

	public static final String TABLE_NAME = "shapes.txt";

	private GtfsShape.Id shapeId;
	private Double lat;
	private Double lon;
	private GtfsShapePointSequence pointSequence;
	private Double shapeDistTraveled;

	public GtfsShape.Id getShapeId() {
		return shapeId;
	}

	public Double getLat() {
		return lat;
	}

	public Double getLon() {
		return lon;
	}

	/**
	 * @return The coordinates associated to this stop. If one of lat or lon is
	 *         not defined, return null. If both lat and lon are 0.0, the
	 *         coordinates are most likely to be bogus, we also return null
	 *         (undefined). Otherwise return the associated position.
	 */
	public GeoCoordinates getCoordinates() {
		// TODO cache this?
		if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
			return new GeoCoordinates(lat, lon);
		} else {
			return null;
		}
	}

	public GtfsShapePointSequence getPointSequence() {
		return pointSequence;
	}

	public Double getShapeDistTraveled() {
		return shapeDistTraveled;
	}

	@Override
	public String toString() {
		return "ShapePoint{shapeId=" + shapeId + ",seq=" + pointSequence
				+ ",lat=" + lat + ",lon=" + lon + "}";
	}

	public static class Builder {
		private GtfsShapePoint point;

		public Builder() {
			point = new GtfsShapePoint();
		}

		public Builder withShapeId(GtfsShape.Id id) {
			point.shapeId = id;
			return this;
		}

		public Builder withCoordinates(Double lat, Double lon) {
			point.lat = lat;
			point.lon = lon;
			return this;
		}

		public Builder withPointSequence(GtfsShapePointSequence pointSequence) {
			point.pointSequence = pointSequence;
			return this;
		}

		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			point.shapeDistTraveled = shapeDistTraveled;
			return this;
		}

		public GtfsShapePoint build() {
			return point;
		}
	}

	public static final Comparator<GtfsShapePoint> POINT_SEQ_COMPARATOR = new Comparator<GtfsShapePoint>() {
		@Override
		public int compare(GtfsShapePoint point1, GtfsShapePoint point2) {
			// Make sure we do not compare points from different shapes
			assert Objects.equals(point1.getShapeId(), point2.getShapeId());
			// Sort undef point sequence at start
			int seq1 = point1.getPointSequence() == null ? Integer.MIN_VALUE
					: point1.getPointSequence().getSequence();
			int seq2 = point2.getPointSequence() == null ? Integer.MIN_VALUE
					: point2.getPointSequence().getSequence();
			return Integer.compare(seq1, seq2);
		}
	};
}
