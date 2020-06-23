package com.mecatran.gtfsvtor.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mecatran.gtfsvtor.dao.impl.PackedShapePoints;
import com.mecatran.gtfsvtor.dao.impl.PackingShapePointsDao;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsShapePointSequence;

public class TestPackedShapePoints {

	@Test
	public void testSample() throws ParseException {
		PackedShapePoints.Context ctx = new PackingShapePointsDao.DefaultContext();
		List<GtfsShapePoint> shapePoints = new ArrayList<>();

		// Simple basic test
		// Tested lat flags: 0, 2, 4, 5, 6, 7, 11, 12, 13
		// Tested lon flags: 0, 1, 2, 5, 6, 9, 13
		// Tested seq flags: 1, 2, 3, 4, 5, 6
		shapePoints.add(shapePoint("S1", -1000000, 0.0, null));
		shapePoints.add(shapePoint("S1", 0, 45.0, -180.0));
		shapePoints
				.add(shapePoint("S1", 0, 45.000001, 179.9998768, -123.45678));
		shapePoints.add(shapePoint("S1", 4, 45.000002, 179.9998787, 0.0));
		shapePoints
				.add(shapePoint("S1", 5, 45.000022, -179.9876223, 1.124e-12));
		shapePoints.add(shapePoint("S1", 300, 45.0000231, -179.9876223, 1.e30));
		shapePoints.add(shapePoint("S1", 10001, 45.000042, 0.0, 2.e30));
		shapePoints.add(shapePoint("S1", 10002, null, null));
		shapePoints.add(shapePoint("S1", 10008, 44.999887, null));
		shapePoints.add(shapePoint("S1", 20001, -45.1234567, 0.001));
		shapePoints.add(shapePoint("S1", 20002, -45.4999999, 0.002001));
		shapePoints.add(shapePoint("S1", 20002, -45.4999999, 0.0030011));
		testList(ctx, shapePoints);
	}

	@Test
	public void testDeltas() throws ParseException {
		PackedShapePoints.Context ctx = new PackingShapePointsDao.DefaultContext();
		for (double delta : Arrays.asList(0., 0.0000001, 0.000001, 0.0000015,
				0.00001, 0.0001, 0.001, 0.01, 0.1, 0.111, 1.0, 10., 20., 180.,
				360.)) {
			for (double base : Arrays.asList(-180., -179.999, -90., -0.001, 0.,
					0.001, 45., 45.123, 90.0, 179.999, 180.)) {
				for (double sign : Arrays.asList(-1, 1)) {
					double lat1 = base;
					double lon1 = base;
					double lat2 = base + delta * sign;
					double lon2 = base + delta * sign;
					if (lat2 > 100.)
						lat2 = 100.;
					if (lat2 < -100.)
						lat2 = -100.;
					if (lon2 > 200.)
						lon2 = 200.;
					if (lon2 < -200.)
						lon2 = -200.;
					List<GtfsShapePoint> shapePoints = new ArrayList<>();
					shapePoints.add(shapePoint("S1", 1, lat1, lon1,
							base - delta * sign));
					shapePoints.add(shapePoint("S1", 2, lat2, lon2,
							base + delta * sign));
					testList(ctx, shapePoints);
				}
			}
		}
	}

	@Test
	public void testSeqDeltas() throws ParseException {
		PackedShapePoints.Context ctx = new PackingShapePointsDao.DefaultContext();
		for (int delta : Arrays.asList(0, 1, 100, 1000, 10000, 100000,
				1000000)) {
			for (int base : Arrays.asList(-100000, -100, 0, 1, 10000, 100000,
					1000000)) {
				List<GtfsShapePoint> shapePoints = new ArrayList<>();
				shapePoints.add(shapePoint("S1", base, 0.0, 0.0));
				shapePoints.add(shapePoint("S1", base + delta, 1.0, 0.0));
				testList(ctx, shapePoints);
			}
		}
	}

	private GtfsShapePoint shapePoint(String shapeId, Integer seq, Double lat,
			Double lon) {
		return shapePoint(shapeId, seq, lat, lon, null);
	}

	private GtfsShapePoint shapePoint(String shapeId, Integer seq, Double lat,
			Double lon, Double shp) {
		GtfsShapePoint.Builder bld = new SimpleGtfsShapePoint.Builder();
		bld.withShapeId(GtfsShape.id(shapeId));
		if (seq != null)
			bld.withPointSequence(GtfsShapePointSequence.fromSequence(seq));
		bld.withCoordinates(lat, lon);
		bld.withShapeDistTraveled(shp);
		return bld.build();
	}

	private void testList(PackedShapePoints.Context context,
			List<GtfsShapePoint> shapePoints) {
		GtfsShape.Id shapeId = shapePoints.get(0).getShapeId();
		PackedShapePoints psp = new PackedShapePoints(context, shapePoints);
		List<GtfsShapePoint> shapePoints2 = psp.getShapePoints(shapeId,
				context);
		assertShapePoints(shapePoints, shapePoints2);
	}

	public static void assertShapePoints(List<GtfsShapePoint> shapePoints1,
			List<GtfsShapePoint> shapePoints2) {
		assertEquals(shapePoints1.size(), shapePoints2.size());
		for (int i = 0; i < shapePoints1.size(); i++) {
			GtfsShapePoint sp1 = shapePoints1.get(i);
			GtfsShapePoint sp2 = shapePoints2.get(i);
			assertEquals(sp1.getShapeId(), sp2.getShapeId());
			/*
			 * There can be slight rounding errors in case the lat/lon data is
			 * fed with a higher precision than E7. Allow for some delta (+/-1
			 * E7 unit).
			 */
			assertEqualsNullable(sp1.getLat(), sp2.getLat(), 1. / 1000000);
			assertEqualsNullable(sp1.getLon(), sp2.getLon(), 1. / 1000000);
			assertEquals(sp1.getPointSequence(), sp2.getPointSequence());
			double e = 0.0;
			if (sp1.getShapeDistTraveled() != null
					&& sp2.getShapeDistTraveled() != null) {
				e = Math.abs(
						sp1.getShapeDistTraveled() + sp2.getShapeDistTraveled())
						/ 1.e7 + Float.MIN_NORMAL * 2;
			}
			assertEqualsNullable(sp1.getShapeDistTraveled(),
					sp2.getShapeDistTraveled(), e);
		}
	}

	private static void assertEqualsNullable(Double d1, Double d2,
			double epsilon) {
		assertTrue(d1 == null && d2 == null || d1 != null && d2 != null);
		if (d1 != null && d2 != null) {
			assertEquals(d1, d2, epsilon);
		}
	}
}
