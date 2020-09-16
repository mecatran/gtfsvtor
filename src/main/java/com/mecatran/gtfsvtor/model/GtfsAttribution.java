package com.mecatran.gtfsvtor.model;

import java.util.Optional;

public class GtfsAttribution
		implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "attributions.txt";

	private GtfsAttribution.Id id;
	private GtfsAgency.Id agencyId;
	private GtfsRoute.Id routeId;
	private GtfsTrip.Id tripId;
	private String organizationName;
	private Boolean isProducer;
	private Boolean isOperator;
	private Boolean isAuthority;
	private String attributionUrl;
	private String attributionEmail;
	private String attributionPhone;

	private long sourceLineNumber;

	public Optional<GtfsAttribution.Id> getId() {
		return Optional.ofNullable(id);
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public Optional<GtfsAgency.Id> getAgencyId() {
		return Optional.ofNullable(agencyId);
	}

	public Optional<GtfsRoute.Id> getRouteId() {
		return Optional.ofNullable(routeId);
	}

	public Optional<GtfsTrip.Id> getTripId() {
		return Optional.ofNullable(tripId);
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public Optional<Boolean> getIsProducer() {
		return Optional.ofNullable(isProducer);
	}

	public boolean getNonNullIsProducer() {
		return isProducer == null ? false : isProducer;
	}

	public Optional<Boolean> getIsOperator() {
		return Optional.ofNullable(isOperator);
	}

	public boolean getNonNullIsOperator() {
		return isOperator == null ? false : isOperator;
	}

	public Optional<Boolean> getIsAuthority() {
		return Optional.ofNullable(isAuthority);
	}

	public boolean getNonNullIsAuthority() {
		return isAuthority == null ? false : isAuthority;
	}

	public Optional<String> getAttributionUrl() {
		return Optional.ofNullable(attributionUrl);
	}

	public Optional<String> getAttributionEmail() {
		return Optional.ofNullable(attributionEmail);
	}

	public Optional<String> getAttributionPhone() {
		return Optional.ofNullable(attributionPhone);
	}

	@Override
	public String toString() {
		return "Attribution{id=" + id + ",organizationName='" + organizationName
				+ "'}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsAttribution> {

		private Id(String id) {
			super(id);
		}

		private static Id build(String id) {
			return new Id(id);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsAttribution.Id.class);
		}
	}

	public static class Builder {
		private GtfsAttribution attribution;

		public Builder(String id) {
			attribution = new GtfsAttribution();
			attribution.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			attribution.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withAgencyId(GtfsAgency.Id agencyId) {
			attribution.agencyId = agencyId;
			return this;
		}

		public Builder withRouteId(GtfsRoute.Id routeId) {
			attribution.routeId = routeId;
			return this;
		}

		public Builder withTripId(GtfsTrip.Id tripId) {
			attribution.tripId = tripId;
			return this;
		}

		public Builder withOrganizationName(String organizationName) {
			attribution.organizationName = organizationName;
			return this;
		}

		public Builder withIsProducer(Boolean isProducer) {
			attribution.isProducer = isProducer;
			return this;
		}

		public Builder withIsOperator(Boolean isOperator) {
			attribution.isOperator = isOperator;
			return this;
		}

		public Builder withIsAuthority(Boolean isAuthority) {
			attribution.isAuthority = isAuthority;
			return this;
		}

		public Builder withAttributionUrl(String attributionUrl) {
			attribution.attributionUrl = attributionUrl;
			return this;
		}

		public Builder withAttributionEmail(String attributionEmail) {
			attribution.attributionEmail = attributionEmail;
			return this;
		}

		public Builder withAttributionPhone(String attributionPhone) {
			attribution.attributionPhone = attributionPhone;
			return this;
		}

		public GtfsAttribution build() {
			return attribution;
		}
	}
}
