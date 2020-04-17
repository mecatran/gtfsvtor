package com.mecatran.gtfsvtor.model;

import java.util.Locale;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class GtfsFeedInfo
		implements GtfsObject<String>, GtfsObjectWithSourceInfo {

	public static final String TABLE_NAME = "feed_info.txt";

	private String feedPublisherName;
	private String feedPublisherUrl;
	private Locale feedLang;
	private Locale defaultLang;
	private GtfsLogicalDate feedStartDate;
	private GtfsLogicalDate feedEndDate;
	private String feedVersion;
	private String feedContactEmail;
	private String feedContactUrl;

	private DataObjectSourceInfo sourceInfo;

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
	}

	public String getFeedPublisherName() {
		return feedPublisherName;
	}

	public String getFeedPublisherUrl() {
		return feedPublisherUrl;
	}

	public Locale getFeedLang() {
		return feedLang;
	}

	public Locale getDefaultLang() {
		return defaultLang;
	}

	public GtfsLogicalDate getFeedStartDate() {
		return feedStartDate;
	}

	public GtfsLogicalDate getFeedEndDate() {
		return feedEndDate;
	}

	public String getFeedVersion() {
		return feedVersion;
	}

	public String getFeedContactEmail() {
		return feedContactEmail;
	}

	public String getFeedContactUrl() {
		return feedContactUrl;
	}

	@Override
	public String toString() {
		return "FeedInfo{feedVersion=" + feedVersion + ",feedStartDate='"
				+ feedStartDate + ",feedEndDate=" + feedEndDate + "}";
	}

	// Note: Feed info does not have an ID

	public static class Builder {
		private GtfsFeedInfo feedInfo;

		public Builder() {
			feedInfo = new GtfsFeedInfo();
		}

		public Builder withSourceInfo(DataObjectSourceInfo sourceInfo) {
			feedInfo.sourceInfo = sourceInfo;
			return this;
		}

		public Builder withFeedPublisherName(String feedPublisherName) {
			feedInfo.feedPublisherName = feedPublisherName;
			return this;
		}

		public Builder withFeedPublisherUrl(String feedPublisherUrl) {
			feedInfo.feedPublisherUrl = feedPublisherUrl;
			return this;
		}

		public Builder withFeedLang(Locale feedLang) {
			feedInfo.feedLang = feedLang;
			return this;
		}

		public Builder withDefaultLang(Locale defaultLang) {
			feedInfo.defaultLang = defaultLang;
			return this;
		}

		public Builder withFeedStartDate(GtfsLogicalDate feedStartDate) {
			feedInfo.feedStartDate = feedStartDate;
			return this;
		}

		public Builder withFeedEndDate(GtfsLogicalDate feedEndDate) {
			feedInfo.feedEndDate = feedEndDate;
			return this;
		}

		public Builder withFeedVersion(String feedVersion) {
			feedInfo.feedVersion = feedVersion;
			return this;
		}

		public Builder withFeedContactEmail(String feedContactEmail) {
			feedInfo.feedContactEmail = feedContactEmail;
			return this;
		}

		public Builder withFeedContactUrl(String feedContactUrl) {
			feedInfo.feedContactUrl = feedContactUrl;
			return this;
		}

		public GtfsFeedInfo build() {
			return feedInfo;
		}
	}
}
