package com.mecatran.gtfsvtor.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.cmdline.FileDataIO;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.lib.GtfsVtor;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.test.stubs.TestDataIO;

public class TestUtils {

	public static class TestBundle {
		public ReviewReport report;
		public IndexedReadOnlyDao dao;

		public <T extends ReportIssue> long issuesCountOfCategory(
				Class<T> reportClass) {
			// Maybe we could delegate this method to the report?
			return report.getReportIssues(reportClass).count();
		}

		public long issuesCountOfSeverities(ReportIssueSeverity... severities) {
			return Arrays.asList(severities).stream().distinct()
					.mapToInt(s -> report.issuesCountOfSeverity(s).totalCount())
					.sum();
		}

		// TODO Rename this method to "issuesOfType"
		public <T extends ReportIssue> List<T> issuesOfCategory(
				Class<T> reportClass) {
			return report.getReportIssues(reportClass)
					.collect(Collectors.toList());
		}
	}

	public static class TestScenario implements GtfsVtorOptions {
		public String gtfsFileOrDirectory;
		public int maxStopTimesInterleaving = 3;
		public int maxShapePointsInterleaving = 3;
		public StopTimesDaoMode stopTimesDaoMode = StopTimesDaoMode.AUTO;
		public ShapePointsDaoMode shapePointsDaoMode = ShapePointsDaoMode.PACKED;
		public String htmlOutputFile = null;
		public TestDataIO htmlDataIO = null;
		public TestDataIO jsonDataIO = null;
		public String configFile = "src/test/resources/configs/def.properties";

		public TestScenario() {
		}

		// Is this really what we want?
		public TestScenario(String localGtfsFileOrDirectory) {
			this.gtfsFileOrDirectory = "src/test/resources/data/"
					+ localGtfsFileOrDirectory;
		}

		public TestScenario(String localGtfsFileOrDirectory,
				String localConfigFile) {
			this.gtfsFileOrDirectory = "src/test/resources/data/"
					+ localGtfsFileOrDirectory;
			this.configFile = "src/test/resources/configs/" + localConfigFile;
		}

		public TestBundle run() {
			TestBundle ret = new TestBundle();
			GtfsVtorOptions options = getOptions();
			GtfsVtor gtfsvtor = new GtfsVtor(options);
			try {
				gtfsvtor.validate();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			ret.report = gtfsvtor.getReviewReport();
			ret.dao = gtfsvtor.getDao();
			return ret;
		}

		public GtfsVtorOptions getOptions() {
			return this;
		}

		@Override
		public int getMaxStopTimeInterleaving() {
			return maxStopTimesInterleaving;
		}

		@Override
		public int getMaxShapePointsInterleaving() {
			return maxShapePointsInterleaving;
		}

		@Override
		public StopTimesDaoMode getStopTimesDaoMode() {
			return stopTimesDaoMode;
		}

		@Override
		public ShapePointsDaoMode getShapePointsDaoMode() {
			return shapePointsDaoMode;
		}

		@Override
		public Optional<String> getConfigFile() {
			return Optional.ofNullable(configFile);
		}

		@Override
		public Optional<NamedDataIO> getHtmlDataIO() throws IOException {
			if (htmlOutputFile != null)
				return Optional.of(new FileDataIO(htmlOutputFile, false));
			else
				return Optional.ofNullable(htmlDataIO);
		}

		@Override
		public Optional<NamedDataIO> getJsonDataIO() throws IOException {
			return Optional.ofNullable(jsonDataIO);
		}

		@Override
		public String getGtfsFile() {
			return gtfsFileOrDirectory;
		}
	}

	public static TestBundle loadAndValidate(String localGtfsFileOrDirectory) {
		return new TestScenario(localGtfsFileOrDirectory).run();
	}

	public static TestBundle loadAndValidate(String localGtfsFileOrDirectory,
			String localConfigFile) {
		return new TestScenario(localGtfsFileOrDirectory, localConfigFile)
				.run();
	}
}
