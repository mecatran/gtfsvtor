package com.mecatran.gtfsvtor.geospatial;

import java.util.Locale;

public class GeoBounds {

	private GeoCoordinates min;
	private GeoCoordinates max;

	public GeoBounds(double minLat, double minLon, double maxLat,
			double maxLon) {
		this.min = new GeoCoordinates(minLat, minLon);
		this.max = new GeoCoordinates(maxLat, maxLon);
	}

	public GeoCoordinates getMin() {
		return min;
	}

	public GeoCoordinates getMax() {
		return max;
	}

	public boolean contains(double lat, double lon) {
		return min.getLat() <= lat && lat <= max.getLat() && min.getLon() <= lon
				&& lon <= max.getLon();
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "GeoBounds{%.8f,%.8f,%.8f,%.8f}",
				min.getLat(), min.getLon(), max.getLat(), max.getLon());
	}
}
