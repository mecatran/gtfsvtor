package com.mecatran.gtfsvtor.cmdline;

import java.io.File;

import com.mecatran.gtfsvtor.dao.impl.InMemoryDao;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.impl.CsvDataSource;
import com.mecatran.gtfsvtor.loader.impl.DefaultDataLoaderContext;
import com.mecatran.gtfsvtor.loader.impl.GtfsDataLoader;
import com.mecatran.gtfsvtor.reporting.ReportFormatter;
import com.mecatran.gtfsvtor.reporting.impl.HtmlReportFormatter;
import com.mecatran.gtfsvtor.reporting.impl.InMemoryReportLog;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultStreamingValidator;
import com.mecatran.gtfsvtor.validation.impl.DefaultDaoValidatorContext;
import com.mecatran.gtfsvtor.validation.impl.DefaultValidatorConfig;

public class GtfsVtorMain {

	// TODO This is a minimal dummy example
	public static void main(String[] args) throws Exception {

		System.out.println("GTFSVTOR, development version\n"
				+ "Copyright (c) 2020 Mecatran\n"
				+ "This program comes with absolutely no warranty.\n"
				+ "This is free software, and you are welcome to redistribute it\n"
				+ "under certain conditions; see the license file for details.\n");
		if (args.length != 1) {
			System.err.println("Usage: gtfsvtor <GTFS>");
			System.exit(1);
		}

		long start = System.currentTimeMillis();

		InMemoryReportLog report = new InMemoryReportLog();

		NamedInputStreamSource inputStreamSource = NamedInputStreamSource
				.autoGuess(args[0], report);
		NamedTabularDataSource dataSource = new CsvDataSource(
				inputStreamSource);
		InMemoryDao dao = new InMemoryDao();
		DefaultValidatorConfig config = new DefaultValidatorConfig();

		DefaultStreamingValidator defStreamingValidator = new DefaultStreamingValidator(
				config);
		GtfsDataLoader loader = new GtfsDataLoader(dataSource);
		loader.load(new DefaultDataLoaderContext(dao, dao, report,
				defStreamingValidator));

		File propFile = new File("config.properties");
		if (propFile.exists() && propFile.canRead()) {
			System.out.println("Loading config from " + propFile.getName());
			config.loadProperties(propFile);
		}

		DaoValidator.Context context = new DefaultDaoValidatorContext(dao,
				report, config);
		DefaultDaoValidator daoValidator = new DefaultDaoValidator(config);
		daoValidator.validate(context);

		ReportFormatter formatter = new HtmlReportFormatter(
				"validation-report.html");
		formatter.format(report);

		long end = System.currentTimeMillis();
		System.out.println("Validation done in " + (end - start) + "ms.");
	}
}
