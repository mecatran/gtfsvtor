package com.mecatran.gtfsvtor.model.impl;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.PackedCoordinates;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;

/**
 * TODO - Remove this class, it is not used anymore.
 */
@Deprecated
public class SmallGtfsShapePoint implements GtfsShapePoint {

	private GtfsShape.Id shapeId; // Will be interned
	private long packedCoordinates;
	private int pointSeq;
	private float shapeDistTraveled;

	@Override
	public GtfsShape.Id getShapeId() {
		return shapeId;
	}

	@Override
	public Double getLat() {
		GeoCoordinates p = PackedCoordinates.unpack(packedCoordinates);
		return p == null ? null : p.getLat();
	}

	@Override
	public Double getLon() {
		GeoCoordinates p = PackedCoordinates.unpack(packedCoordinates);
		return p == null ? null : p.getLon();
	}

	@Override
	public GeoCoordinates getCoordinates() {
		return PackedCoordinates.unpack(packedCoordinates);
	}

	@Override
	public GtfsShapePointSequence getPointSequence() {
		return pointSeq == Integer.MIN_VALUE ? null
				: GtfsShapePointSequence.fromSequence(pointSeq);
	}

	@Override
	public int getInternalPointSequence() {
		return pointSeq;
	}

	@Override
	public Double getShapeDistTraveled() {
		return Float.isNaN(shapeDistTraveled) ? null
				: Double.valueOf(shapeDistTraveled);
	}

	@Override
	public String toString() {
		GeoCoordinates p = getCoordinates();
		return "SimpleShapePoint{shapeId=" + shapeId + ",seq=" + pointSeq
				+ ",lat=" + (p == null ? "?" : p.getLat()) + ",lon="
				+ (p == null ? "?" : p.getLon()) + "}";
	}

	@Deprecated
	public static class Builder implements GtfsShapePoint.Builder {
		private SmallGtfsShapePoint point;

		public Builder() {
			point = new SmallGtfsShapePoint();
		}

		@Override
		public Builder withShapeId(GtfsShape.Id id) {
			point.shapeId = id;
			return this;
		}

		@Override
		public Builder withCoordinates(Double lat, Double lon) {
			point.packedCoordinates = PackedCoordinates.pack(lat, lon);
			return this;
		}

		@Override
		public Builder withPointSequence(GtfsShapePointSequence pointSequence) {
			point.pointSeq = pointSequence == null ? Integer.MIN_VALUE
					: pointSequence.getSequence();
			return this;
		}

		@Override
		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			point.shapeDistTraveled = shapeDistTraveled == null ? Float.NaN
					: shapeDistTraveled.floatValue();
			return this;
		}

		@Override
		public SmallGtfsShapePoint build() {
			return point;
		}
	}
}
