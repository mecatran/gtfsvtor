package com.mecatran.gtfsvtor.geospatial;

public final class PackedCoordinates {

	/*
	 * Store lat/lon in fixed-point integer format with 7 decimals (value
	 * multiplied by 10000000). E7 int coordinates do fit in 32 bits, allow for
	 * some extra spaces in case of overflow, and are easy to debug and store.
	 * The (worst) precision at the equator is 1.11 cm, which is in our use-case
	 * sufficient. Also converting using a multiple of 10 ensure that when
	 * converting back and forth we do not have spurrious rounding issues making
	 * the value slightly off on the 7th decimal place (useful to print back the
	 * exact same value as the one from the input data, which is a string in
	 * base-10).
	 */
	private static int E7_FACTOR = 10000000;
	public static long NULL = 0xFFFFFFFFFFFFFFFFL;
	private static int MAX_INT = E7_FACTOR * 200; // Allow slack
	private static double MAX_DBL = MAX_INT * 1. / E7_FACTOR;

	private static boolean debug = false;

	public static long pack(Double lat, Double lon) {
		if (lat == null || lon == null)
			return NULL;
		if (Double.isNaN(lat) || Double.isNaN(lon))
			return NULL;
		/*
		 * Range check. If out of range, clamp. Note that the range is
		 * [-200,200], which obviously will be happy to store out of range
		 * latitude/longitude. But the range check is not done here.
		 */
		if (lat < -MAX_DBL)
			lat = -MAX_DBL;
		if (lat > MAX_DBL)
			lat = MAX_DBL;
		if (lon < -MAX_DBL)
			lon = -MAX_DBL;
		if (lon > MAX_DBL)
			lon = MAX_DBL;
		int ilat = (int) (lat * E7_FACTOR);
		int ilon = (int) (lon * E7_FACTOR);
		long packed = ((long) ilat << 32) | (ilon & 0xFFFFFFFFL);
		if (debug) {
			System.out.println(String.format(
					"lat: %13.8f -> %12d  lon: %13.8f -> %12d  packed: %016x",
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
		double lat = ilat * 1. / E7_FACTOR;
		double lon = ilon * 1. / E7_FACTOR;
		if (debug) {
			System.out.println(String.format(
					"packed: %016x  lat: %12d -> %13.8f  lon: %12d -> %13.8f",
					packed, ilat, lat, ilon, lon));
		}
		return new GeoCoordinates(lat, lon);
	}
}
