package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;

public class GtfsStop implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "stops.txt";

	private GtfsStop.Id id;
	private GtfsStop.Id parentId;
	private GtfsStopType type;
	private String code;
	private String name;
	private Double lat;
	private Double lon;
	private String description;
	private GtfsZone.Id zoneId;
	private String url;
	private TimeZone timezone;
	private GtfsWheelchairAccess wheelchairBoarding;
	private GtfsLevel.Id levelId;
	private String platformCode;

	private long sourceLineNumber;

	public GtfsStop.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public GtfsStop.Id getParentId() {
		return parentId;
	}

	public Optional<GtfsStopType> getOptionalType() {
		return Optional.ofNullable(type);
	}

	public GtfsStopType getType() {
		return type == null ? GtfsStopType.STOP : type;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	/*
	 * Here lies a hack. We store Double.NaN in lat/lon when the input data is
	 * malformed, null when it is not present. This is necessary because lat/lon
	 * is sometimes mandatory, sometimes not. The standard getters (getLat,
	 * getLon) will hide this to the user.
	 */
	public Double getLatOrNaN() {
		return lat;
	}

	public Double getLat() {
		return lat == null ? null : Double.isNaN(lat) ? null : lat;
	}

	public Double getLonOrNaN() {
		return lon;
	}

	public Double getLon() {
		return lon == null ? null : Double.isNaN(lon) ? null : lon;
	}

	/**
	 * @return The coordinates associated to this stop. If one of lat or lon is
	 *         not defined, return null. If both lat and lon are 0.0, the
	 *         coordinates are most likely to be bogus, we also return null
	 *         (undefined). Otherwise return the associated position.
	 */
	public GeoCoordinates getCoordinates() {
		// TODO cache this?
		if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
			return new GeoCoordinates(lat, lon);
		} else {
			return null;
		}
	}

	public String getDescription() {
		return description;
	}

	public GtfsZone.Id getZoneId() {
		return zoneId;
	}

	public String getUrl() {
		return url;
	}

	public TimeZone getTimezone() {
		return timezone;
	}

	public Optional<GtfsWheelchairAccess> getWheelchairBoarding() {
		return Optional.ofNullable(wheelchairBoarding);
	}

	public GtfsWheelchairAccess getNonNullWheelchairBoarding() {
		return wheelchairBoarding == null ? GtfsWheelchairAccess.UNKNOWN
				: wheelchairBoarding;
	}

	public GtfsLevel.Id getLevelId() {
		return levelId;
	}

	public String getPlatformCode() {
		return platformCode;
	}

	@Override
	public String toString() {
		return "Stop{id=" + id + ",type=" + type + ",name='" + name + "'}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsStop> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsStop.Id.class);
		}
	}

	public static class Builder {
		private GtfsStop stop;

		public Builder(String id) {
			stop = new GtfsStop();
			stop.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			stop.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withParentId(GtfsStop.Id parentId) {
			stop.parentId = parentId;
			return this;
		}

		public Builder withType(GtfsStopType type) {
			stop.type = type;
			return this;
		}

		public Builder withCode(String code) {
			stop.code = code;
			return this;
		}

		public Builder withName(String name) {
			stop.name = name;
			return this;
		}

		public Builder withCoordinates(Double lat, Double lon) {
			stop.lat = lat;
			stop.lon = lon;
			return this;
		}

		public Builder withDescription(String description) {
			stop.description = description;
			return this;
		}

		public Builder withZoneId(GtfsZone.Id zoneId) {
			stop.zoneId = zoneId;
			return this;
		}

		public Builder withUrl(String url) {
			stop.url = url;
			return this;
		}

		public Builder withTimezone(TimeZone timezone) {
			stop.timezone = timezone;
			return this;
		}

		public Builder withWheelchairBoarding(
				GtfsWheelchairAccess wheelchairBoarding) {
			stop.wheelchairBoarding = wheelchairBoarding;
			return this;
		}

		public Builder withLevelId(GtfsLevel.Id levelId) {
			stop.levelId = levelId;
			return this;
		}

		public Builder withPlatformCode(String platformCode) {
			stop.platformCode = platformCode;
			return this;
		}

		public GtfsStop build() {
			return stop;
		}
	}
}
