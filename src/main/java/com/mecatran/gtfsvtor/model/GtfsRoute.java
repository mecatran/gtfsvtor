package com.mecatran.gtfsvtor.model;

import java.util.Optional;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsRoute implements GtfsObject<String>, GtfsObjectWithSourceInfo {

	public static final String TABLE_NAME = "routes.txt";

	private GtfsRoute.Id id;
	private GtfsAgency.Id agencyId;
	private GtfsRouteType type;
	private String shortName;
	private String longName;
	private String description;
	private String url;
	private GtfsColor color;
	private GtfsColor textColor;
	private Integer sortOrder;

	private DataObjectSourceInfo sourceInfo;

	public GtfsRoute.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
	}

	public GtfsAgency.Id getAgencyId() {
		return agencyId;
	}

	public GtfsRouteType getType() {
		return type;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public String getDescription() {
		return description;
	}

	public String getUrl() {
		return url;
	}

	public Optional<GtfsColor> getColor() {
		return Optional.ofNullable(color);
	}

	public GtfsColor getNonNullColor() {
		return color == null ? GtfsColor.WHITE : color;
	}

	public Optional<GtfsColor> getTextColor() {
		return Optional.ofNullable(textColor);
	}

	public GtfsColor getNonNullTextColor() {
		return textColor == null ? GtfsColor.BLACK : textColor;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	@Override
	public String toString() {
		return "Route{id=" + id + ",type=" + type + ",shortName='" + shortName
				+ "',longName='" + longName + "'}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : new Id(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsRoute> {

		private Id(String id) {
			super(id);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsRoute.Id.class);
		}
	}

	public static class Builder {
		private GtfsRoute route;

		public Builder(String id) {
			route = new GtfsRoute();
			route.id = id(id);
		}

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			route.sourceInfo = sourceInfo;
			return this;
		}

		public Builder withAgencyId(GtfsAgency.Id agencyId) {
			route.agencyId = agencyId;
			return this;
		}

		public Builder withType(GtfsRouteType type) {
			route.type = type;
			return this;
		}

		public Builder withShortName(String shortName) {
			route.shortName = shortName;
			return this;
		}

		public Builder withLongName(String longName) {
			route.longName = longName;
			return this;
		}

		public Builder withDescription(String description) {
			route.description = description;
			return this;
		}

		public Builder withUrl(String url) {
			route.url = url;
			return this;
		}

		public Builder withColor(GtfsColor color) {
			route.color = color;
			return this;
		}

		public Builder withTextColor(GtfsColor textColor) {
			route.textColor = textColor;
			return this;
		}

		public Builder withSortOrder(Integer sortOrder) {
			route.sortOrder = sortOrder;
			return this;
		}

		public GtfsRoute build() {
			return route;
		}
	}
}
