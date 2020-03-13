package com.mecatran.gtfsvtor.cmdline;

import com.beust.jcommander.JCommander;
import com.mecatran.gtfsvtor.lib.GtfsVtor;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultStreamingValidator;
import com.mecatran.gtfsvtor.validation.StreamingValidator;
import com.mecatran.gtfsvtor.validation.dao.ReferencesValidator;
import com.mecatran.gtfsvtor.validation.impl.ValidatorInjector;
import com.mecatran.gtfsvtor.validation.streaming.AgencyStreamingValidator;

public class GtfsVtorMain {

	// TODO This is a minimal dummy example
	public static void main(String[] args) throws Exception {

		System.out.println("GTFSVTOR, development version\n"
				+ "Copyright (c) 2020 Mecatran\n"
				+ "This program comes with absolutely no warranty.\n"
				+ "This is free software, and you are welcome to redistribute it\n"
				+ "under certain conditions; see the license file for details.\n");

		CmdLineArgs cmdLineArgs = new CmdLineArgs();
		JCommander jcmd = JCommander.newBuilder().addObject(cmdLineArgs)
				.build();
		jcmd.setProgramName("GTFSVTOR");
		jcmd.parse(args);
		if (cmdLineArgs.isHelp() || cmdLineArgs.getGtfsFile() == null) {
			jcmd.usage();
			System.exit(1);
		}

		if (cmdLineArgs.isListValidators()) {
			System.out.println(
					"==================== DAO validators ====================");
			ValidatorInjector.listValidatorOptions(DaoValidator.class,
					DefaultDaoValidator.class.getClassLoader(),
					ReferencesValidator.class.getPackage(), System.out);
			System.out.println(
					"================= Streaming validators =================");
			ValidatorInjector.listValidatorOptions(StreamingValidator.class,
					DefaultStreamingValidator.class.getClassLoader(),
					AgencyStreamingValidator.class.getPackage(), System.out);
			System.out.println(
					"========================================================");
		}

		long start = System.currentTimeMillis();
		GtfsVtor gtfsVtor = new GtfsVtor(cmdLineArgs);
		gtfsVtor.validate();
		ReviewReport report = gtfsVtor.getReviewReport();
		long end = System.currentTimeMillis();
		System.out.println(String.format(
				"Validation done in %d ms: %d INFO, %d WARNING, %d ERROR, %d CRITICAL",
				end - start,
				report.issuesCountOfSeverity(ReportIssueSeverity.INFO),
				report.issuesCountOfSeverity(ReportIssueSeverity.WARNING),
				report.issuesCountOfSeverity(ReportIssueSeverity.ERROR),
				report.issuesCountOfSeverity(ReportIssueSeverity.CRITICAL)));
	}
}
