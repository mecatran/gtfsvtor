package com.mecatran.gtfsvtor.model;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;

public class TestGeospatial {

	@Test
	public void testGeodesics() {
		GeoCoordinates a = new GeoCoordinates(0, 0);
		GeoCoordinates b = new GeoCoordinates(45, 0);
		GeoCoordinates c = new GeoCoordinates(0, 45);
		GeoCoordinates d = new GeoCoordinates(90, 45);
		final double P8 = Geodesics.EARTH_RADIUS * Math.PI / 4;
		final double P4 = Geodesics.EARTH_RADIUS * Math.PI / 2;
		assertEquals(P8, Geodesics.distanceMeters(a, b), 1);
		assertEquals(P8, Geodesics.distanceMeters(b, a), 1);
		assertEquals(P8, Geodesics.distanceMeters(a, c), 1);
		assertEquals(P8, Geodesics.distanceMeters(c, a), 1);
		assertEquals(0., Geodesics.distanceMeters(a, a), 1.e-6);
		assertEquals(0., Geodesics.distanceMeters(b, b), 1.e-6);
		assertEquals(0., Geodesics.distanceMeters(c, c), 1.e-6);
		assertEquals(P4, Geodesics.distanceMeters(a, d), 1);
		assertEquals(P4, Geodesics.distanceMeters(d, a), 1);

		Random rand = new Random(42);
		for (int i = 0; i < 100; i++) {
			GeoCoordinates p1 = new GeoCoordinates(rand.nextDouble() * 160 - 80,
					rand.nextDouble() * 300 - 150);
			double dp = rand.nextDouble() * 100;
			double dp2 = dp * Math.sqrt(2.);
			GeoCoordinates p2 = new GeoCoordinates(
					p1.getLat() + Geodesics.deltaLat(dp), p1.getLon());
			GeoCoordinates p3 = new GeoCoordinates(p1.getLat(),
					p1.getLon() + Geodesics.deltaLon(dp, p1.getLat()));
			GeoCoordinates p4 = new GeoCoordinates(
					p1.getLat() + Geodesics.deltaLat(dp),
					p1.getLon() + Geodesics.deltaLon(dp, p1.getLat()));
			assertEquals(dp, Geodesics.distanceMeters(p1, p2), 1.e-5);
			assertEquals(dp, Geodesics.distanceMeters(p2, p1), 1.e-5);
			assertEquals(dp, Geodesics.distanceMeters(p1, p3), 1.e-5);
			assertEquals(dp, Geodesics.distanceMeters(p3, p1), 1.e-5);
			assertEquals(dp2, Geodesics.distanceMeters(p1, p4), 1.e-2);
			assertEquals(dp2, Geodesics.distanceMeters(p4, p1), 1.e-2);
		}
	}

	@Test
	public void testFastDistance() {
		/*
		 * Compare exact and fast distance for small distances, < 1km. They
		 * should not differ for more than 1e-4m (1mm).
		 */
		Random rand = new Random(42);
		for (int i = 0; i < 100; i++) {
			GeoCoordinates p1 = new GeoCoordinates(rand.nextDouble() * 160 - 80,
					rand.nextDouble() * 300 - 150);
			GeoCoordinates p2 = new GeoCoordinates(
					p1.getLat() + Geodesics.deltaLat(rand.nextDouble() * 1000),
					p1.getLon() + Geodesics.deltaLon(rand.nextDouble() * 1000,
							p1.getLat()));
			double ed = Geodesics.distanceMeters(p1, p2);
			double fd = Geodesics.fastDistanceMeters(p1, p2);
			assertEquals(ed, fd, 1.e-4);
		}
	}

	// TODO Test segment distance
}
