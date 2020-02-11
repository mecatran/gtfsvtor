package com.mecatran.gtfsvtor.geospatial;

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
}
