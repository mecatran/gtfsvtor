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

	/**
	 * Maps routeType (possibly defined according to google's extended route types)
	 * to an official GTFS route type in the range between 1 and 12.
	 * If there is no mapping defined, this function returns the unchanged route type.
	 * Note that this function returns route type 9 (intercity bus) as well, which
	 * is not (yet?) officially adopted in the GTFS standard.
	 * @param routeType route type (possibly extension)
	 * @return official GTFS route type, if mapping defined, original routeType else
	 */
	public static int mapExtendedToBaseRouteTypeCode(int routeType) {
			if (routeType >= 100 && routeType < 200) {
				return GtfsRouteType.RAIL_CODE;
			} else if (routeType >= 200 && routeType < 300) {
				return GtfsRouteType.INTERCITY_BUS_CODE;
			} else if (routeType >= 300 && routeType < 400) {
				return GtfsRouteType.RAIL_CODE;
			} else if (routeType >= 400 && routeType < 700 || routeType == 10) {
				// Google's 10 (Commuter train) is not yet an official routeType
				return GtfsRouteType.METRO_CODE;
			} else if (routeType >= 700 && routeType < 800) {
				// Map 701 (RegionalBus), 702 (ExpressBus)
				// 715 (Demand and Response Bus Service) and 717 (share taxi service) to
				// INTERCITY_BUS (=> default maxSpeed 120km/h)
				if (routeType == 701 || routeType == 702 || routeType == 715 || routeType == 717) {
					return GtfsRouteType.INTERCITY_BUS_CODE;
				}
				return GtfsRouteType.BUS_CODE;
			} else if (routeType >= 800 && routeType < 900) {
				return GtfsRouteType.TROLLEYBUS_CODE;
			} else if (routeType >= 900 && routeType < 1000) {
				return GtfsRouteType.TRAM_CODE;
			} else if (routeType >= 1000 && routeType < 1100) {
				return GtfsRouteType.FERRY_CODE;
			} else if (routeType >= 1100 && routeType < 1200) {
				// No base type for airborne travel
			} else if (routeType >= 1200 && routeType < 1300) {
				return GtfsRouteType.FERRY_CODE;
			} else if (routeType >= 1300 && routeType < 1400) {
				return GtfsRouteType.GONDOLA_CODE;
			} else if (routeType >= 1400 && routeType < 1500) {
				return GtfsRouteType.FUNICULAR_CODE;
			} else if (routeType >= 1500 && routeType < 1600) {
				// Taxis are no buses, but that looks like closest match
				// and is in line with feedvalidator's maxSpeed 100 km/h
				return GtfsRouteType.BUS_CODE;
			} else if (routeType >= 1600 && routeType < 1700) {
				// No base type for self drive travel, return unchanged
			} else if (routeType == 1701) {
				return GtfsRouteType.CABLE_CAR_CODE;
			}
			// Horse Carriage (8), Horse Drawn Carriage(1702) and all other unknown
			// codes are returned as is, resulting in default handling
			return routeType;
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
