package com.mecatran.gtfsvtor.lib;

import java.io.File;
import java.io.IOException;

import com.mecatran.gtfsvtor.cmdline.CmdLineArgs;
import com.mecatran.gtfsvtor.dao.impl.InMemoryDao;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.impl.CsvDataSource;
import com.mecatran.gtfsvtor.loader.impl.DefaultDataLoaderContext;
import com.mecatran.gtfsvtor.loader.impl.GtfsDataLoader;
import com.mecatran.gtfsvtor.loader.impl.SourceInfoDataReloader;
import com.mecatran.gtfsvtor.loader.schema.DefaultGtfsTableSchema;
import com.mecatran.gtfsvtor.model.factory.DefaultObjectBuilderFactory;
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

	// TODO Create a dedicated config for the use as a lib
	private CmdLineArgs args;
	private InMemoryReportLog report;

	public GtfsVtor(CmdLineArgs cmdLineArgs) {
		this.args = cmdLineArgs;
	}

	public void validate() throws IOException {

		// TODO Properly configure all this
		// TODO Use the lib in the unit-tests

		DefaultValidatorConfig config = new DefaultValidatorConfig();
		if (args.getConfigFile() != null) {
			File propFile = new File(args.getConfigFile());
			if (propFile.exists() && propFile.canRead()) {
				if (args.isVerbose()) {
					System.out.println(
							"Loading config from " + propFile.getName());
				}
				config.loadProperties(propFile);
			} else {
				System.err.println("Cannot load " + propFile.getName());
			}
		}
		// TODO Add remaining cmd line args to config

		report = new InMemoryReportLog().withPrintIssues(args.isPrintIssues());
		NamedInputStreamSource inputStreamSource = NamedInputStreamSource
				.autoGuess(args.getGtfsFile(), report);
		if (inputStreamSource != null) {
			NamedTabularDataSource dataSource = new CsvDataSource(
					inputStreamSource);
			InMemoryDao dao = new InMemoryDao(args.isDisableStopTimePacking(),
					args.getMaxStopTimeInterleaving(),
					args.isDisableShapePointsPacking(),
					args.getMaxShapePointsInterleaving()).withVerbose(true);

			DefaultStreamingValidator defStreamingValidator = new DefaultStreamingValidator(
					config);
			DefaultGtfsTableSchema tableSchema = new DefaultGtfsTableSchema();
			DefaultObjectBuilderFactory objBldFactory = new DefaultObjectBuilderFactory()
					.withSmallShapePoint(args.isDisableShapePointsPacking()
							|| args.getMaxShapePointsInterleaving() > 1000)
					.withSmallStopTime(args.isDisableStopTimePacking()
							|| args.getMaxStopTimeInterleaving() > 1000);
			GtfsDataLoader loader = new GtfsDataLoader(dataSource, tableSchema,
					objBldFactory);

			long start = System.currentTimeMillis();
			loader.load(new DefaultDataLoaderContext(dao, dao, report,
					defStreamingValidator));
			long end = System.currentTimeMillis();
			System.gc();
			Runtime runtime = Runtime.getRuntime();
			System.out.println("Loaded '" + args.getGtfsFile() + "' in "
					+ (end - start) + "ms. Used memory: ~"
					+ (runtime.totalMemory() - runtime.freeMemory())
							/ (1024 * 1024)
					+ "Mb");

			DaoValidator.Context context = new DefaultDaoValidatorContext(dao,
					report, config);
			DefaultDaoValidator daoValidator = new DefaultDaoValidator(config)
					.withVerbose(args.isVerbose())
					.withNumThreads(args.getNumThreads());
			daoValidator.validate(context);
			DefaultTripTimesValidator tripTimesValidator = new DefaultTripTimesValidator(
					config).withVerbose(args.isVerbose());
			tripTimesValidator.scanValidate(context);

			SourceInfoDataReloader sourceInfoReloader = new SourceInfoDataReloader(
					dataSource).withVerbose(args.isVerbose());
			sourceInfoReloader.loadSourceInfos(report);
		}

		ReportFormatter formatter = new HtmlReportFormatter(
				args.getOutputReportFile(),
				args.getMaxIssuesPerCategoryLimit());
		formatter.format(report);
		System.out.println("Report output to " + args.getOutputReportFile());
	}

	public ReviewReport getReviewReport() {
		return report;
	}
}
