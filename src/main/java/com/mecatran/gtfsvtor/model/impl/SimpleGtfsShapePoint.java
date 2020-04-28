package com.mecatran.gtfsvtor.model.impl;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;

public class SimpleGtfsShapePoint implements GtfsShapePoint {

	private GtfsShape.Id shapeId;
	private Double lat;
	private Double lon;
	private GtfsShapePointSequence pointSequence;
	private Double shapeDistTraveled;

	@Override
	public GtfsShape.Id getShapeId() {
		return shapeId;
	}

	@Override
	public Double getLat() {
		return lat;
	}

	@Override
	public Double getLon() {
		return lon;
	}

	@Override
	public GeoCoordinates getCoordinates() {
		// TODO cache this?
		if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
			return new GeoCoordinates(lat, lon);
		} else {
			return null;
		}
	}

	@Override
	public GtfsShapePointSequence getPointSequence() {
		return pointSequence;
	}

	@Override
	public int getInternalPointSequence() {
		return pointSequence == null ? Integer.MIN_VALUE
				: pointSequence.getSequence();
	}

	@Override
	public Double getShapeDistTraveled() {
		return shapeDistTraveled;
	}

	@Override
	public String toString() {
		return "SimpleShapePoint{shapeId=" + shapeId + ",seq=" + pointSequence
				+ ",lat=" + lat + ",lon=" + lon + "}";
	}

	public static class Builder implements GtfsShapePoint.Builder {
		private SimpleGtfsShapePoint point;

		public Builder() {
			point = new SimpleGtfsShapePoint();
		}

		@Override
		public Builder withShapeId(GtfsShape.Id id) {
			point.shapeId = id;
			return this;
		}

		@Override
		public Builder withCoordinates(Double lat, Double lon) {
			point.lat = lat;
			point.lon = lon;
			return this;
		}

		@Override
		public Builder withPointSequence(GtfsShapePointSequence pointSequence) {
			point.pointSequence = pointSequence;
			return this;
		}

		@Override
		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			point.shapeDistTraveled = shapeDistTraveled;
			return this;
		}

		@Override
		public SimpleGtfsShapePoint build() {
			return point;
		}
	}
}
