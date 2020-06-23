package com.mecatran.gtfsvtor.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.lib.GtfsVtor;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;

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
					.mapToInt(s -> report.issuesCountOfSeverity(s)).sum();
		}

		public <T extends ReportIssue> List<T> issuesOfCategory(
				Class<T> reportClass) {
			return report.getReportIssues(reportClass)
					.collect(Collectors.toList());
		}
	}

	public static class TestScenario implements GtfsVtorOptions {
		public String gtfsFileOrDirectory;
		public int maxStopTimesInterleaving = 3;
		public StopTimesDaoMode stopTimesDaoMode = StopTimesDaoMode.AUTO;
		public ShapePointsDaoMode shapePointsDaoMode = ShapePointsDaoMode.PACKED;

		public TestScenario() {
		}

		// Is this really what we want?
		public TestScenario(String localGtfsFileOrDirectory) {
			this.gtfsFileOrDirectory = "src/test/resources/data/"
					+ localGtfsFileOrDirectory;
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
		public StopTimesDaoMode getStopTimesDaoMode() {
			return stopTimesDaoMode;
		}

		@Override
		public ShapePointsDaoMode getShapePointsDaoMode() {
			return shapePointsDaoMode;
		}

		@Override
		public String getGtfsFile() {
			return gtfsFileOrDirectory;
		}
	}

	public static TestBundle loadAndValidate(String localGtfsFileOrDirectory) {
		return new TestScenario(localGtfsFileOrDirectory).run();
	}
}
