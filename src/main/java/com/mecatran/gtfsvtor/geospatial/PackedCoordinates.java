package com.mecatran.gtfsvtor.geospatial;

public class PackedCoordinates {

	/*
	 * Here we assume lat/lon ranges to be symetric. Keep a bit of overhead to
	 * keep clamped values out of bounds for the validator to be able to
	 * complain.
	 */
	/* Latitude range is between -90 to +90. */
	private static double MAX_LAT = 90.000001;
	/* Longitude range is between -180 to +180. */
	private static double MAX_LON = 180.000001;

	private static double MAGIC_LAT_FACTOR = Integer.MAX_VALUE * 1. / MAX_LAT;
	private static double MAGIC_LON_FACTOR = Integer.MAX_VALUE * 1. / MAX_LON;

	/* A stop at the pole is unlikely */
	public static long NULL = 0xFFFFFFFFFFFFFFFFL;

	private static boolean debug = false;

	public static long pack(Double lat, Double lon) {
		if (lat == null || lon == null)
			return NULL;
		if (Double.isNaN(lat) || Double.isNaN(lon))
			return NULL;
		/*
		 * Range check. If out of range, clamp.
		 */
		if (lat < -MAX_LAT)
			lat = -MAX_LAT;
		if (lat > MAX_LAT)
			lat = MAX_LAT;
		if (lon < -MAX_LON)
			lon = -MAX_LON;
		if (lon > MAX_LON)
			lon = MAX_LON;
		int ilat = (int) (lat * MAGIC_LAT_FACTOR);
		int ilon = (int) (lon * MAGIC_LON_FACTOR);
		long packed = ((long) ilat << 32) | (ilon & 0xFFFFFFFFL);
		if (debug) {
			System.out.println(String.format(
					"lat: %13.8f -> %08x  lon: %13.8f -> %08x  packed: %016x",
					lat, ilat, lon, ilon, packed));
		}
		return packed;
	}

	public static GeoCoordinates unpack(long packed) {
		if (packed == NULL)
			return null;
		/* Make sure we cast to int to resurrect negative numbers */
		int ilat = (int) (packed >> 32);
		int ilon = (int) (packed & 0x00000000FFFFFFFFL);
		double lat = ilat * 1. / MAGIC_LAT_FACTOR;
		double lon = ilon * 1. / MAGIC_LON_FACTOR;
		if (debug) {
			System.out.println(String.format(
					"packed: %016x  lat: %08x -> %13.8f  lon: %08x -> %13.8f",
					packed, ilat, lat, ilon, lon));
		}
		return new GeoCoordinates(lat, lon);
	}
}
