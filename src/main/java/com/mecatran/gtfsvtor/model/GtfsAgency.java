package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsAgency
		implements GtfsObject<String>, GtfsObjectWithSourceInfo {

	public static final String TABLE_NAME = "agency.txt";

	private GtfsAgency.Id id;
	private String name;
	private String url;
	private TimeZone timezone;
	private Locale lang;
	private String phone;
	private String fareUrl;
	private String email;

	private DataObjectSourceInfo sourceInfo;

	/* Note: For a null ID, we store a non-null ID. */
	public GtfsAgency.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public TimeZone getTimezone() {
		return timezone;
	}

	public Locale getLang() {
		return lang;
	}

	public String getPhone() {
		return phone;
	}

	public String getFareUrl() {
		return fareUrl;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "Agency{id=" + id + ",name='" + name + "'}";
	}

	public static Id id(String id) {
		/*
		 * Warning: we allow the creation of an agency ID with an empty string
		 * (undefined ID), this is allowed. Note that this is different than a
		 * null Agency.Id, which means no or a missing ID.
		 */
		return id == null ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsAgency> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, Id.class);
		}
	}

	public static class Builder {
		private GtfsAgency agency;

		public Builder(String id) {
			agency = new GtfsAgency();
			agency.id = id(id);
		}

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			agency.sourceInfo = sourceInfo;
			return this;
		}

		public Builder withName(String name) {
			agency.name = name;
			return this;
		}

		public Builder withUrl(String url) {
			agency.url = url;
			return this;
		}

		public Builder withTimezone(TimeZone timezone) {
			agency.timezone = timezone;
			return this;
		}

		public Builder withLang(Locale lang) {
			agency.lang = lang;
			return this;
		}

		public Builder withPhone(String phone) {
			agency.phone = phone;
			return this;
		}

		public Builder withFareUrl(String fareUrl) {
			agency.fareUrl = fareUrl;
			return this;
		}

		public Builder withEmail(String email) {
			agency.email = email;
			return this;
		}

		public GtfsAgency build() {
			return agency;
		}
	}
}
