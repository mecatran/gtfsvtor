package com.mecatran.gtfsvtor.test;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.impl.InMemoryDao;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.impl.CsvDataSource;
import com.mecatran.gtfsvtor.loader.impl.DefaultDataLoaderContext;
import com.mecatran.gtfsvtor.loader.impl.GtfsDataLoader;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.impl.InMemoryReportLog;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultStreamingValidator;
import com.mecatran.gtfsvtor.validation.impl.DefaultDaoValidatorContext;
import com.mecatran.gtfsvtor.validation.impl.DefaultValidatorConfig;

public class TestUtils {

	public static class TestBundle {
		public ReviewReport report;
		public IndexedReadOnlyDao dao;
	}

	public static TestBundle loadAndValidate(String gtfsFileOrDirectory) {
		return loadAndValidate(gtfsFileOrDirectory, "src/test/resources/data/");
	}

	public static TestBundle loadAndValidate(String gtfsFileOrDirectory,
			String base) {
		InMemoryReportLog report = new InMemoryReportLog()
				.withPrintIssues(true);
		TestBundle ret = new TestBundle();
		ret.report = report;
		NamedInputStreamSource inputStreamSource = NamedInputStreamSource
				.autoGuess(base + gtfsFileOrDirectory, report);
		if (inputStreamSource == null) {
			return ret;
		}
		NamedTabularDataSource dataSource = new CsvDataSource(
				inputStreamSource);
		InMemoryDao dao = new InMemoryDao().withVerbose(true);

		DefaultValidatorConfig config = new DefaultValidatorConfig();
		DefaultStreamingValidator streamingValidator = new DefaultStreamingValidator(
				config);
		GtfsDataLoader loader = new GtfsDataLoader(dataSource);
		loader.load(new DefaultDataLoaderContext(dao, dao, report,
				streamingValidator));

		DaoValidator.Context context = new DefaultDaoValidatorContext(dao,
				report, config);
		DefaultDaoValidator daoValidator = new DefaultDaoValidator(config)
				.withVerbose(true);
		daoValidator.validate(context);
		System.out.println(String.format(
				"Validation result for '%s': %d INFO, %d WARNING, %d ERROR, %d CRITICAL",
				gtfsFileOrDirectory,
				report.issuesCountOfSeverity(ReportIssueSeverity.INFO),
				report.issuesCountOfSeverity(ReportIssueSeverity.WARNING),
				report.issuesCountOfSeverity(ReportIssueSeverity.ERROR),
				report.issuesCountOfSeverity(ReportIssueSeverity.CRITICAL)));

		ret.dao = dao;
		return ret;
	}
}
