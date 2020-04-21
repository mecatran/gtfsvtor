package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsTrip implements GtfsObject<String>, GtfsObjectWithSourceInfo {

	public static final String TABLE_NAME = "trips.txt";

	private GtfsTrip.Id id;
	private GtfsRoute.Id routeId;
	private GtfsCalendar.Id serviceId;
	private String headsign;
	private String shortName;
	private GtfsBlockId blockId;
	private GtfsTripDirectionId directionId;
	private GtfsShape.Id shapeId;
	private GtfsWheelchairAccess wheelchairAccessible;
	private GtfsBikeAccess bikesAllowed;

	private DataObjectSourceInfo sourceInfo;

	public GtfsTrip.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
	}

	public GtfsRoute.Id getRouteId() {
		return routeId;
	}

	public GtfsCalendar.Id getServiceId() {
		return serviceId;
	}

	public String getHeadsign() {
		return headsign;
	}

	public String getShortName() {
		return shortName;
	}

	public GtfsBlockId getBlockId() {
		return blockId;
	}

	public GtfsTripDirectionId getDirectionId() {
		return directionId;
	}

	public GtfsShape.Id getShapeId() {
		return shapeId;
	}

	public Optional<GtfsWheelchairAccess> getWheelchairAccessible() {
		return Optional.ofNullable(wheelchairAccessible);
	}

	public GtfsWheelchairAccess getNonNullWheelchairAccessible() {
		return wheelchairAccessible == null ? GtfsWheelchairAccess.UNKNOWN
				: wheelchairAccessible;
	}

	public Optional<GtfsBikeAccess> getBikesAllowed() {
		return Optional.ofNullable(bikesAllowed);
	}

	public GtfsBikeAccess getNonNullBikesAllowed() {
		return bikesAllowed == null ? GtfsBikeAccess.UNKNOWN : bikesAllowed;
	}

	@Override
	public String toString() {
		return "Trip{id=" + id + ",route=" + routeId + ",service=" + serviceId
				+ "}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsTrip> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsTrip.Id.class);
		}
	}

	public static class Builder {
		private GtfsTrip trip;

		public Builder(String id) {
			trip = new GtfsTrip();
			trip.id = id(id);
		}

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			trip.sourceInfo = sourceInfo;
			return this;
		}

		public Builder withRouteId(GtfsRoute.Id routeId) {
			trip.routeId = routeId;
			return this;
		}

		public Builder withServiceId(GtfsCalendar.Id serviceId) {
			trip.serviceId = serviceId;
			return this;
		}

		public Builder withHeadsign(String headsign) {
			trip.headsign = headsign;
			return this;
		}

		public Builder withShortName(String shortName) {
			trip.shortName = shortName;
			return this;
		}

		public Builder withBlockId(GtfsBlockId blockId) {
			trip.blockId = blockId;
			return this;
		}

		public Builder withDirectionId(GtfsTripDirectionId directionId) {
			trip.directionId = directionId;
			return this;
		}

		public Builder withShapeId(GtfsShape.Id shapeId) {
			trip.shapeId = shapeId;
			return this;
		}

		public Builder withWheelchairAccessible(
				GtfsWheelchairAccess wheelchairAccessible) {
			trip.wheelchairAccessible = wheelchairAccessible;
			return this;
		}

		public Builder withBikesAllowed(GtfsBikeAccess bikesAllowed) {
			trip.bikesAllowed = bikesAllowed;
			return this;
		}

		public GtfsTrip build() {
			return trip;
		}
	}
}
