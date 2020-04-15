package com.mecatran.gtfsvtor.model;

public class GtfsFareRule implements GtfsObject<String> {

	public static final String TABLE_NAME = "fare_rules.txt";

	private GtfsFareAttribute.Id fareId;
	private GtfsRoute.Id routeId;
	private GtfsZone.Id originId;
	private GtfsZone.Id destinationId;
	private GtfsZone.Id containsId;

	public GtfsFareAttribute.Id getFareId() {
		return fareId;
	}

	public GtfsRoute.Id getRouteId() {
		return routeId;
	}

	public GtfsZone.Id getOriginId() {
		return originId;
	}

	public GtfsZone.Id getDestinationId() {
		return destinationId;
	}

	public GtfsZone.Id getContainsId() {
		return containsId;
	}

	@Override
	public String toString() {
		return "GtfsFareRule{fareId=" + fareId + ",routeId=" + routeId
				+ ",originId=" + originId + ",destinationId=" + destinationId
				+ ",containsId=" + containsId + "}";
	}

	public static class Builder {
		private GtfsFareRule fareRule;

		public Builder() {
			fareRule = new GtfsFareRule();
		}

		public Builder withFareId(GtfsFareAttribute.Id fareId) {
			fareRule.fareId = fareId;
			return this;
		}

		public Builder withRouteId(GtfsRoute.Id routeId) {
			fareRule.routeId = routeId;
			return this;
		}

		public Builder withOriginId(GtfsZone.Id originId) {
			fareRule.originId = originId;
			return this;
		}

		public Builder withDestinationId(GtfsZone.Id destinationId) {
			fareRule.destinationId = destinationId;
			return this;
		}

		public Builder withContainsId(GtfsZone.Id containsId) {
			fareRule.containsId = containsId;
			return this;
		}

		public GtfsFareRule build() {
			return fareRule;
		}
	}
}
