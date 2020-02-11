package com.mecatran.gtfsvtor.geospatial;

public class Geodesics {

	public static final double EARTH_RADIUS = 6371 * 1000;

	/**
	 * Compute exact distance between two points (haversine distance).
	 */
	public static double distanceMeters(GeoCoordinates p1, GeoCoordinates p2) {
		double dLat = Math.toRadians(p2.getLat() - p1.getLat());
		double dLon = Math.toRadians(p2.getLon() - p1.getLon());
		double sinLat2 = Math.sin(dLat / 2);
		double sinLon2 = Math.sin(dLon / 2);
		double a = sinLat2 * sinLat2 + Math.cos(Math.toRadians(p1.getLat()))
				* Math.cos(Math.toRadians(p2.getLat())) * sinLon2 * sinLon2;
		double c = 2 * Math.asin(Math.sqrt(a));
		return EARTH_RADIUS * c;
	}

	/**
	 * Approximation of the haversine formula, but much faster (use only one
	 * cosine and one square root). Works only if lat1 ~ lat2 and lon1 ~ lon2
	 * (works on an equirectangular plane centered on the middle point between
	 * p1 and p2).
	 * 
	 * @param p1 The first point coordinates
	 * @param p2 The second point coordinates
	 * @return The approximated distance, in meters
	 */
	public static double fastDistanceMeters(GeoCoordinates p1,
			GeoCoordinates p2) {
		double cosLat = Math
				.cos(Math.toRadians((p1.getLat() + p2.getLat()) / 2));
		return fastDistanceMeters(p1, p2, cosLat);
	}

	/**
	 * Optimized version, factoring out the cosine projection factor.
	 * 
	 * @param cosLat The cosine of the projection center latitude in radians.
	 */
	public static double fastDistanceMeters(GeoCoordinates p1,
			GeoCoordinates p2, double cosLat) {
		double dLat = Math.toRadians(p2.getLat() - p1.getLat());
		double dLon = Math.toRadians(p2.getLon() - p1.getLon()) * cosLat;
		return EARTH_RADIUS * Math.sqrt(dLat * dLat + dLon * dLon);
	}

	private static final double ONE_MINUS = Math.nextAfter(1.0, -1);

	/**
	 * Compute the distance between a point P and a segment [AB]. Approximated
	 * version, as all computations are done in an equi-rectangular plane
	 * projection centered on P.
	 * 
	 * @param p Position of the point P
	 * @param a Position of the first end-point A
	 * @param b Position of the second end-point B
	 * @return A pair of values: The first double is the distance in meters
	 *         between P and [AB], the second double is the linear coordinate of
	 *         C in the [AB] vector basis where C is the projection of P on line
	 *         (AB), clamped to [0..1] (t=0: A, t=1: B)
	 */
	public static double[] fastDistanceMeters(GeoCoordinates p,
			GeoCoordinates a, GeoCoordinates b) {
		double cosLat = Math.cos(Math.toRadians(p.getLat()));
		return fastDistanceMeters(p, a, b, cosLat);
	}

	/**
	 * Optimized version, factoring out the cosine projection factor.
	 * 
	 * @param cosLat The cosine of the projection center latitude in radians.
	 */
	public static double[] fastDistanceMeters(GeoCoordinates p,
			GeoCoordinates a, GeoCoordinates b, double cosLat) {
		double xP = Math.toRadians(p.getLat());
		double yP = Math.toRadians(p.getLon()) * cosLat;
		double xA = Math.toRadians(a.getLat());
		double yA = Math.toRadians(a.getLon()) * cosLat;
		double xB = Math.toRadians(b.getLat());
		double yB = Math.toRadians(b.getLon()) * cosLat;

		// Compute [AB] length
		double l2 = (xA - xB) * (xA - xB) + (yA - yB) * (yA - yB);
		double d2, t;
		if (l2 == 0) {
			// Pathological d(AB)=0 case, d = d(PA)
			d2 = (xP - xA) * (xP - xA) + (yP - yA) * (yP - yA);
			t = 0.5; // This value could be anything from 0 to 1
		} else {
			// Compute t, linear coordinate of C in the [AB] vector basis
			// and where C is the projection of P on line (AB).
			t = ((xP - xA) * (xB - xA) + (yP - yA) * (yB - yA)) / l2;
			if (t <= 0.0) {
				// C outside [AB] on A side: d = d(PA)
				d2 = (xP - xA) * (xP - xA) + (yP - yA) * (yP - yA);
				t = 0.0;
			} else {
				// Normalize the result: return in [0,1[ range
				if (t > ONE_MINUS) {
					t = ONE_MINUS;
				}
				// C inside [AB]: d = d(PC), C = A + t.B
				double xC = xA + t * (xB - xA);
				double yC = yA + t * (yB - yA);
				d2 = (xP - xC) * (xP - xC) + (yP - yC) * (yP - yC);
			}
		}
		return new double[] { EARTH_RADIUS * Math.sqrt(d2), t };
	}

	/**
	 * Compute bearing in DEGREE between two points, in the range [0..360].
	 */
	public static double bearingDegrees(GeoCoordinates p1, GeoCoordinates p2) {
		double rLat1 = Math.toRadians(p1.getLat());
		double rLat2 = Math.toRadians(p2.getLat());
		double dLon = Math.toRadians(p2.getLon() - p1.getLon());
		double cos2 = Math.cos(rLat2);
		double y = Math.sin(dLon) * cos2;
		double x = Math.cos(rLat1) * Math.sin(rLat2)
				- Math.sin(rLat1) * cos2 * Math.cos(dLon);
		double bearing = Math.toDegrees(Math.atan2(y, x));
		if (bearing < 0)
			bearing += 360.0;
		return bearing;
	}

	/**
	 * @param meters A distance in meter alongside the E/W axis.
	 * @param lat The latitude, in degree.
	 * @return The delta in longitude corresponding to the E/W distance in
	 *         meters.
	 */
	public static double deltaLon(double meters, double lat) {
		return Math.toDegrees(meters / EARTH_RADIUS)
				/ Math.cos(Math.toRadians(lat));
	}

	/**
	 * @param meters A distance in meter alongside the S/N axis.
	 * @return The delta in latitude corresponding to the S/N distance in
	 *         meters.
	 */
	public static double deltaLat(double meters) {
		return Math.toDegrees(meters / EARTH_RADIUS);
	}

}
