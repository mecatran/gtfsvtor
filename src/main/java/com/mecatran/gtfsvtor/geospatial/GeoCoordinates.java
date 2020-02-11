package com.mecatran.gtfsvtor.geospatial;

import java.util.Locale;

public class GeoCoordinates {

	private double lat;
	private double lon;

	public GeoCoordinates(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "GeoCoordinates{%.6f,%.6f}", lat, lon);
	}
}
