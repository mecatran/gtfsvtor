package com.mecatran.gtfsvtor.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander;
import com.mecatran.gtfsvtor.cmdline.CmdLineArgs;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.impl.InMemoryDao;
import com.mecatran.gtfsvtor.dao.impl.PackingShapePointsDao;
import com.mecatran.gtfsvtor.dao.impl.PackingStopTimesDao;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.impl.CsvDataSource;
import com.mecatran.gtfsvtor.loader.impl.DefaultDataLoaderContext;
import com.mecatran.gtfsvtor.loader.impl.GtfsDataLoader;
import com.mecatran.gtfsvtor.loader.impl.SourceInfoDataReloader;
import com.mecatran.gtfsvtor.loader.schema.DefaultGtfsTableSchema;
import com.mecatran.gtfsvtor.model.factory.DefaultObjectBuilderFactory;
import com.mecatran.gtfsvtor.model.impl.TestPackedShapePoints;
import com.mecatran.gtfsvtor.model.impl.TestPackedStopTimes;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.impl.InMemoryReportLog;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultStreamingValidator;
import com.mecatran.gtfsvtor.validation.DefaultTripTimesValidator;
import com.mecatran.gtfsvtor.validation.impl.DefaultDaoValidatorContext;
import com.mecatran.gtfsvtor.validation.impl.DefaultValidatorConfig;

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

	public static class TestScenario {
		public String gtfsFileOrDirectory;
		public int maxStopTimesInterleaving = 3;

		public TestScenario() {
		}

		// Is this really what we want?
		public TestScenario(String localGtfsFileOrDirectory) {
			this.gtfsFileOrDirectory = "src/test/resources/data/"
					+ localGtfsFileOrDirectory;
		}

		public CmdLineArgs getCmdLineArgs() {
			// TODO This is a ugly hack. Make this better
			CmdLineArgs cmdLineArgs = new CmdLineArgs();
			JCommander jcmd = JCommander.newBuilder().addObject(cmdLineArgs)
					.build();
			jcmd.parse(new String[] { "--maxStopTimesInterleaving",
					"" + maxStopTimesInterleaving });
			return cmdLineArgs;
		}
	}

	public static TestBundle loadAndValidate(String localGtfsFileOrDirectory) {
		return runScenario(new TestScenario(localGtfsFileOrDirectory));
	}

	@Deprecated
	public static TestBundle loadAndValidate(String gtfsFileOrDirectory,
			String base) {
		TestScenario scenario = new TestScenario();
		scenario.gtfsFileOrDirectory = base + gtfsFileOrDirectory;
		return runScenario(scenario);
	}

	// TODO Make a test runner class, not a static method
	// Or even better, re-use the lib
	public static TestBundle runScenario(TestScenario scenario) {
		InMemoryReportLog report = new InMemoryReportLog()
				.withPrintIssues(true);
		TestBundle ret = new TestBundle();
		ret.report = report;
		CmdLineArgs args = scenario.getCmdLineArgs();
		NamedInputStreamSource inputStreamSource = NamedInputStreamSource
				.autoGuess(scenario.gtfsFileOrDirectory, report);
		if (inputStreamSource == null) {
			return ret;
		}
		// TODO Add config configuration from args
		DefaultValidatorConfig config = new DefaultValidatorConfig();
		NamedTabularDataSource dataSource = new CsvDataSource(
				inputStreamSource);
		InMemoryDao dao = new InMemoryDao(args.isDisableStopTimePacking(),
				args.getMaxStopTimeInterleaving(),
				args.isDisableShapePointsPacking(),
				args.getMaxShapePointsInterleaving()).withVerbose(true);
		PackingStopTimesDao
				.setAssertListener(TestPackedStopTimes::assertStopTimes);
		PackingShapePointsDao
				.setAssertListener(TestPackedShapePoints::assertShapePoints);

		DefaultStreamingValidator streamingValidator = new DefaultStreamingValidator(
				config);
		DefaultGtfsTableSchema tableSchema = new DefaultGtfsTableSchema();
		DefaultObjectBuilderFactory objBldFactory = new DefaultObjectBuilderFactory()
				.withSmallShapePoint(args.isDisableShapePointsPacking()
						|| args.getMaxShapePointsInterleaving() > 1000)
				.withSmallStopTime(args.isDisableStopTimePacking()
						|| args.getMaxStopTimeInterleaving() > 1000);
		GtfsDataLoader loader = new GtfsDataLoader(dataSource, tableSchema,
				objBldFactory);
		loader.load(new DefaultDataLoaderContext(dao, dao, report,
				streamingValidator));

		DaoValidator.Context context = new DefaultDaoValidatorContext(dao,
				report, config);
		DefaultDaoValidator daoValidator = new DefaultDaoValidator(config)
				.withVerbose(true);
		daoValidator.validate(context);
		DefaultTripTimesValidator tripTimesValidator = new DefaultTripTimesValidator(
				config).withVerbose(true);
		tripTimesValidator.scanValidate(context);

		SourceInfoDataReloader sourceInfoReloader = new SourceInfoDataReloader(
				dataSource).withVerbose(true);
		sourceInfoReloader.loadSourceInfos(report);

		System.out.println(String.format(
				"Validation result for '%s': %d INFO, %d WARNING, %d ERROR, %d CRITICAL",
				scenario.gtfsFileOrDirectory,
				report.issuesCountOfSeverity(ReportIssueSeverity.INFO),
				report.issuesCountOfSeverity(ReportIssueSeverity.WARNING),
				report.issuesCountOfSeverity(ReportIssueSeverity.ERROR),
				report.issuesCountOfSeverity(ReportIssueSeverity.CRITICAL)));

		ret.dao = dao;
		return ret;
	}
}
