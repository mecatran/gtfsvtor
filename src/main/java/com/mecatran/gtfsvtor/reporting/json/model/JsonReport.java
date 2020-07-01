package com.mecatran.gtfsvtor.reporting.json.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JsonReport {

	public static final String DATA_VERSION = "1.0";

	public static class JsonSeverityCount {
		public String severity;
		public int totalCount;
		public int reportedCount;
	}

	public static class JsonCategoryCount {
		public String severity;
		public String categoryName;
		// TODO Add error code?
		public int totalCount;
		public int reportedCount;
	}

	public static class JsonSummary {
		public List<JsonSeverityCount> severities = new ArrayList<>();
		public List<JsonCategoryCount> categories = new ArrayList<>();
	}

	public static class JsonValidatorInfo {
		public String name;
		public String version;
		public String buildDate;
		public String buildRev;
		public String copyrights;
	}

	public static class JsonInputDataInfo {
		public String filename;
	}

	public static class JsonValidationRun {

		public Date timestamp;
		public JsonInputDataInfo input;
		public JsonValidatorInfo validator;
		public JsonSummary summary;
	}

	public String dataVersion = DATA_VERSION;
	public List<JsonValidationRun> reports = new ArrayList<>();

}
