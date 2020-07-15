package com.mecatran.gtfsvtor.model;

/**
 */
public enum GtfsTranslationTable {

	AGENCY("agency"), STOPS("stops"), ROUTES("routes"), TRIPS("trips"),
	STOP_TIMES("stop_times"), PATHWAYS("pathways"), LEVELS("levels"),
	FEED_INFO("feed_info"), ATTRIBUTIONS("attributions");

	private final String tablename;

	private GtfsTranslationTable(String tablename) {
		this.tablename = tablename;
	}

	public static GtfsTranslationTable fromValue(String tablename) {
		switch (tablename) {
		case "agency":
			return AGENCY;
		case "stops":
			return STOPS;
		case "routes":
			return ROUTES;
		case "trips":
			return TRIPS;
		case "stop_times":
			return STOP_TIMES;
		case "pathways":
			return PATHWAYS;
		case "levels":
			return LEVELS;
		case "feed_info":
			return FEED_INFO;
		case "attributions":
			return ATTRIBUTIONS;
		default:
			throw new IllegalArgumentException(
					"Invalid tablename: " + tablename);
		}
	}

	public String getValue() {
		return tablename;
	}
}
