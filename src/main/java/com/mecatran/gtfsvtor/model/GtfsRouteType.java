package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Do not use an enum, as route type can be extended and the allowed range is
 * quite flexible.
 */
public class GtfsRouteType {

	// Those constants are here for switch statements
	public static final int TRAM_CODE = 0;
	public static final int METRO_CODE = 1;
	public static final int RAIL_CODE = 2;
	public static final int BUS_CODE = 3;
	public static final int FERRY_CODE = 4;
	public static final int CABLE_CAR_CODE = 5;
	public static final int GONDOLA_CODE = 6;
	public static final int FUNICULAR_CODE = 7;
	// Note: Intercity bus code is not yet officially adopted
	// (see https://github.com/google/transit/pull/174)
	// As these may have higher maxspeed values, we support them nevertheless
	public static final int INTERCITY_BUS_CODE = 9;
	public static final int TROLLEYBUS_CODE = 11;
	public static final int MONORAIL_CODE = 12;

	public static final GtfsRouteType TRAM = new GtfsRouteType(TRAM_CODE);
	public static final GtfsRouteType METRO = new GtfsRouteType(METRO_CODE);
	public static final GtfsRouteType RAIL = new GtfsRouteType(RAIL_CODE);
	public static final GtfsRouteType BUS = new GtfsRouteType(BUS_CODE);
	public static final GtfsRouteType FERRY = new GtfsRouteType(FERRY_CODE);
	public static final GtfsRouteType CABLE_CAR = new GtfsRouteType(
			CABLE_CAR_CODE);
	public static final GtfsRouteType GONDOLA = new GtfsRouteType(GONDOLA_CODE);
	public static final GtfsRouteType FUNICULAR = new GtfsRouteType(
			FUNICULAR_CODE);
	public static final GtfsRouteType INTERCITY_BUS = new GtfsRouteType(
			INTERCITY_BUS_CODE);
	public static final GtfsRouteType TROLLEYBUS = new GtfsRouteType(
			TROLLEYBUS_CODE);
	public static final GtfsRouteType MONORAIL = new GtfsRouteType(
			MONORAIL_CODE);

	private static Map<Integer, GtfsRouteType> CACHE = new HashMap<>(10);

	private final int value;

	private GtfsRouteType(int value) {
		this.value = value;
	}

	public static GtfsRouteType fromValue(Integer type) {
		// TODO Synchronize?
		return type == null ? null
				: CACHE.computeIfAbsent(type, GtfsRouteType::new);
	}

	public int getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != getClass()) {
			return false;
		}
		GtfsRouteType other = (GtfsRouteType) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		// TODO Return value as string
		return "GtfsRouteType{" + value + "}";
	}
}
