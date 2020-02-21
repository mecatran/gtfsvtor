package com.mecatran.gtfsvtor.test.transitfeeds;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransitFeedsResponse {

	static public class TransitFeedUrls {
		@JsonProperty("d")
		public String downloadUrl;
	}

	static public class TransitFeed {
		@JsonProperty
		public String id;
		@JsonProperty("ty")
		public String type;
		@JsonProperty("t")
		public String title;
		@JsonProperty("u")
		public TransitFeedUrls urls;
	}

	static public class TransitFeedsResult {
		@JsonProperty
		public int total;
		@JsonProperty
		public int limit;
		@JsonProperty
		public int page;
		@JsonProperty
		public int numPages;
		@JsonProperty
		public List<TransitFeed> feeds;
	}

	@JsonProperty
	public String status;
	@JsonProperty("ts")
	public long timestamp;
	@JsonProperty
	public TransitFeedsResult results;
}
