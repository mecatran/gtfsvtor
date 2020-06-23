package com.mecatran.gtfsvtor.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.PackedCoordinates;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsShapePoint;

public class PackedUnsortedShapePoints {

	public interface Context {
	}

	private static final int INITIAL_SIZE = 50;
	private static final int NULL_SEQ = 0xFFFFFFFF;

	private int size;
	private long[] cdata;
	private int[] sdata;
	private float[] pdata;

	public PackedUnsortedShapePoints() {
		allocate(INITIAL_SIZE);
	}

	public void addShapePoint(Context context, GtfsShapePoint shapePoint) {
		if (size == cdata.length) {
			grow();
		}
		long cdata = PackedCoordinates.pack(shapePoint.getLat(),
				shapePoint.getLon());
		this.cdata[size] = cdata;

		int sdata = shapePoint.getPointSequence() == null ? NULL_SEQ
				: shapePoint.getPointSequence().getSequence();
		this.sdata[size] = sdata;

		Double shapeDistTraveled = shapePoint.getShapeDistTraveled();
		if (shapeDistTraveled != null) {
			if (this.pdata == null) {
				this.pdata = new float[this.cdata.length];
				for (int i = 0; i < size; i++)
					this.pdata[i] = Float.NaN;
			}
			this.pdata[size] = shapeDistTraveled.floatValue();
		} else if (this.pdata != null) {
			this.pdata[size] = Float.NaN;
		}

		size++;
	}

	private void grow() {
		// Make this 2 a parameter? Use previous shapes to get more insight?
		int len = cdata.length * 2;
		long[] cdata2 = new long[len];
		System.arraycopy(cdata, 0, cdata2, 0, cdata.length);
		cdata = cdata2;
		int[] sdata2 = new int[len];
		System.arraycopy(sdata, 0, sdata2, 0, sdata.length);
		sdata = sdata2;
		if (pdata != null) {
			float[] pdata2 = new float[len];
			System.arraycopy(pdata, 0, pdata2, 0, pdata.length);
			pdata = pdata2;
		}
	}

	public void sort(Context context) {
		List<GtfsShapePoint> shapePoints = getShapePoints(null, context);
		Collections.sort(shapePoints, GtfsShapePoint.POINT_SEQ_COMPARATOR);
		allocate(shapePoints.size());
		size = 0;
		shapePoints.forEach(st -> addShapePoint(context, st));
	}

	public List<GtfsShapePoint> getShapePoints(GtfsShape.Id shapeId,
			Context context) {
		List<GtfsShapePoint> ret = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			GtfsShapePoint.Builder builder = new SimpleGtfsShapePoint.Builder();
			builder.withShapeId(shapeId);
			long cdata = this.cdata[i];
			GeoCoordinates coords = PackedCoordinates.unpack(cdata);
			if (coords != null) {
				builder.withCoordinates(coords.getLat(), coords.getLon());
			}
			int sdata = this.sdata[i];
			if (sdata != NULL_SEQ)
				builder.withPointSequence(
						GtfsShapePointSequence.fromSequence(sdata));
			if (this.pdata != null) {
				float shapeDist = this.pdata[i];
				if (!Float.isNaN(shapeDist))
					builder.withShapeDistTraveled(Double.valueOf(shapeDist));
			}
			ret.add(builder.build());
		}
		return ret;
	}

	private void allocate(int n) {
		cdata = new long[n];
		sdata = new int[n];
		pdata = null;
	}

	public int getDataSize() {
		return cdata.length * 8 + sdata.length * 4
				+ (pdata == null ? 0 : pdata.length * 4);
	}
}
