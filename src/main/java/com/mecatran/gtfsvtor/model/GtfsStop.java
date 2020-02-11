package com.mecatran.gtfsvtor.model;

import java.util.Optional;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsStop implements GtfsObject<String>, GtfsObjectWithSourceInfo {

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
	// TODO Level ID
	private String platformCode;

	private DataObjectSourceInfo sourceInfo;

	public GtfsStop.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
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

	public Double getLat() {
		return lat;
	}

	public Double getLon() {
		return lon;
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

	public String getPlatformCode() {
		return platformCode;
	}

	@Override
	public String toString() {
		return "Stop{id=" + id + ",type=" + type + ",name='" + name + "'}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : new Id(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsStop> {

		private Id(String id) {
			super(id);
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

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			stop.sourceInfo = sourceInfo;
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

		public Builder withPlatformCode(String platformCode) {
			stop.platformCode = platformCode;
			return this;
		}

		public GtfsStop build() {
			return stop;
		}
	}
}
