package com.mecatran.gtfsvtor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.geospatial.PackedCoordinates;

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

	@Test
	public void testPackedCoordinates() {

		testOne(0.0, 0.0);
		testOne(0.000001, 0.000001);
		testOne(45.0, 90.0);
		testOne(90.0, 180.0);
		testOne(-0.000001, -0.000001);
		testOne(-1.0, -1.0);
		testOne(-45.0, -90.0);
		testOne(-90.0, -180.0);

		// Max distance rounding error should be less than 1.2cm
		double d;
		double MAX_ERR_DISTANCE_METERS = 1.2e-2;

		Random rand = new Random(42L);
		for (int i = 0; i < 1000; i++) {
			double lat = rand.nextDouble() * 180 - 90;
			double lon = rand.nextDouble() * 360 - 180;
			GeoCoordinates p1 = new GeoCoordinates(lat, lon);
			GeoCoordinates p2 = PackedCoordinates
					.unpack(PackedCoordinates.pack(p1.getLat(), p1.getLon()));
			assertEquals(lat, p2.getLat(), 1e-7);
			assertEquals(lon, p2.getLon(), 1e-7);
			d = Geodesics.distanceMeters(p1, p2);
			assertTrue(d < MAX_ERR_DISTANCE_METERS);
		}

		d = Geodesics.distanceMeters(
				PackedCoordinates.unpack(0x0000000000000000L),
				PackedCoordinates.unpack(0x0000000000000001L));
		assertTrue(d < MAX_ERR_DISTANCE_METERS);
		d = Geodesics.distanceMeters(
				PackedCoordinates.unpack(0x0000000000000000L),
				PackedCoordinates.unpack(0x0000000100000000L));
		assertTrue(d < MAX_ERR_DISTANCE_METERS);
		d = Geodesics.distanceMeters(
				PackedCoordinates.unpack(0x7FFFFFFE7FFFFFFEL),
				PackedCoordinates.unpack(0x7FFFFFFF7FFFFFFFL));
		assertTrue(d < MAX_ERR_DISTANCE_METERS);
		d = Geodesics.distanceMeters(
				PackedCoordinates.unpack(0x8000000080000000L),
				PackedCoordinates.unpack(0x8000000180000001L));
		assertTrue(d < MAX_ERR_DISTANCE_METERS);
		d = Geodesics.distanceMeters(
				PackedCoordinates.unpack(0xFFFFFFFEFFFFFFFDL),
				PackedCoordinates.unpack(0xFFFFFFFFFFFFFFFEL));
		assertTrue(d < MAX_ERR_DISTANCE_METERS);
	}

	private void testOne(double lat, double lon) {
		assertEquals(lat, PackedCoordinates
				.unpack(PackedCoordinates.pack(lat, lon)).getLat(), 1e-7);
		assertEquals(lon, PackedCoordinates
				.unpack(PackedCoordinates.pack(lat, lon)).getLon(), 1e-7);
	}
}
