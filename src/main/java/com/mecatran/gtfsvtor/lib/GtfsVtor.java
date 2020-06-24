package com.mecatran.gtfsvtor.lib;

import java.io.File;
import java.io.IOException;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.dao.inmemory.InMemoryDao;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.impl.CsvDataSource;
import com.mecatran.gtfsvtor.loader.impl.DefaultDataLoaderContext;
import com.mecatran.gtfsvtor.loader.impl.GtfsDataLoader;
import com.mecatran.gtfsvtor.loader.impl.SourceInfoDataReloader;
import com.mecatran.gtfsvtor.loader.schema.DefaultGtfsTableSchema;
import com.mecatran.gtfsvtor.reporting.ReportFormatter;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.impl.HtmlReportFormatter;
import com.mecatran.gtfsvtor.reporting.impl.InMemoryReportLog;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultStreamingValidator;
import com.mecatran.gtfsvtor.validation.DefaultTripTimesValidator;
import com.mecatran.gtfsvtor.validation.impl.DefaultDaoValidatorContext;
import com.mecatran.gtfsvtor.validation.impl.DefaultValidatorConfig;

public class GtfsVtor {

	private GtfsVtorOptions options;
	private InMemoryReportLog report;
	private IndexedReadOnlyDao dao;

	public GtfsVtor(GtfsVtorOptions options) {
		this.options = options;
	}

	public void validate() throws IOException {

		// TODO Properly configure all this
		// TODO Use the lib in the unit-tests

		DefaultValidatorConfig config = new DefaultValidatorConfig();
		if (options.getConfigFile() != null) {
			File propFile = new File(options.getConfigFile());
			if (propFile.exists() && propFile.canRead()) {
				if (options.isVerbose()) {
					System.out.println(
							"Loading config from " + propFile.getName());
				}
				config.loadProperties(propFile);
			} else {
				System.err.println("Cannot load " + propFile.getName());
			}
		}
		// TODO Add remaining cmd line args to config

		report = new InMemoryReportLog()
				.withPrintIssues(options.isPrintIssues());
		NamedInputStreamSource inputStreamSource = NamedInputStreamSource
				.autoGuess(options.getGtfsFile(), report);
		if (inputStreamSource != null) {
			NamedTabularDataSource dataSource = new CsvDataSource(
					inputStreamSource);
			InMemoryDao dao = new InMemoryDao(options.getStopTimesDaoMode(),
					options.getMaxStopTimeInterleaving(),
					options.getShapePointsDaoMode(),
					options.getMaxShapePointsInterleaving())
							.withVerbose(options.isVerbose());
			this.dao = dao;

			DefaultStreamingValidator defStreamingValidator = new DefaultStreamingValidator(
					config);
			DefaultGtfsTableSchema tableSchema = new DefaultGtfsTableSchema();
			GtfsDataLoader loader = new GtfsDataLoader(dataSource, tableSchema);

			long start = System.currentTimeMillis();
			loader.load(new DefaultDataLoaderContext(dao, dao, report,
					defStreamingValidator));
			long end = System.currentTimeMillis();
			System.gc();
			if (options.isVerbose()) {
				Runtime runtime = Runtime.getRuntime();
				System.out.println("Loaded '" + options.getGtfsFile() + "' in "
						+ (end - start) + "ms. Used memory: ~"
						+ (runtime.totalMemory() - runtime.freeMemory())
								/ (1024 * 1024)
						+ "Mb");
			}

			DaoValidator.Context context = new DefaultDaoValidatorContext(dao,
					report, config);
			DefaultDaoValidator daoValidator = new DefaultDaoValidator(config)
					.withVerbose(options.isVerbose())
					.withNumThreads(options.getNumThreads());
			daoValidator.validate(context);
			DefaultTripTimesValidator tripTimesValidator = new DefaultTripTimesValidator(
					config).withVerbose(options.isVerbose());
			tripTimesValidator.scanValidate(context);

			SourceInfoDataReloader sourceInfoReloader = new SourceInfoDataReloader(
					dataSource).withVerbose(options.isVerbose());
			sourceInfoReloader.loadSourceInfos(report);
		}

		// TODO Auto-guess output format from file?
		// TODO Ability to format to output stream
		if (options.getOutputReportFile() != null) {
			ReportFormatter formatter = new HtmlReportFormatter(
					options.getOutputReportFile(),
					options.getMaxIssuesPerCategoryLimit());
			formatter.format(report);
			System.out.println(
					"Report output to " + options.getOutputReportFile());
		}
	}

	public ReviewReport getReviewReport() {
		return report;
	}

	public IndexedReadOnlyDao getDao() {
		return dao;
	}
}
