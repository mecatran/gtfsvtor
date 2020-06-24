package com.mecatran.gtfsvtor.cmdline;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.beust.jcommander.JCommander;
import com.mecatran.gtfsvtor.lib.GtfsVtor;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.validation.impl.ValidatorInjector;

public class GtfsVtorMain {

	public static void main(String[] args) throws Exception {

		Calendar cal = GregorianCalendar.getInstance();
		ManifestReader mfr = new ManifestReader(GtfsVtorMain.class);
		System.out.println(String.format("GTFSVTOR version %s\n"
				+ "Copyright (c) %d Mecatran\n"
				+ "This program comes with absolutely no warranty.\n"
				+ "This is free software, and you are welcome to redistribute it\n"
				+ "under certain conditions; see the license file for details.\n",
				mfr.getApplicationVersion(), cal.get(Calendar.YEAR)));

		CmdLineArgs cmdLineArgs = new CmdLineArgs();
		JCommander jcmd = JCommander.newBuilder().addObject(cmdLineArgs)
				.build();
		jcmd.setProgramName("GTFSVTOR");
		jcmd.parse(args);

		if (cmdLineArgs.isListValidators()) {
			System.out.println(
					"==================== DAO validators ====================");
			ValidatorInjector.getDaoValidatorInjector()
					.listValidatorOptions(System.out);
			System.out.println(
					"================= Streaming validators =================");
			ValidatorInjector.getStreamingValidatorInjector()
					.listValidatorOptions(System.out);
			System.out.println(
					"========================================================");
			System.exit(1);
		}

		if (cmdLineArgs.isHelp() || cmdLineArgs.getGtfsFile() == null) {
			jcmd.usage();
			System.out.println(String.format(
					"Version %s build at %s from commit %s",
					mfr.getApplicationVersion(), mfr.getApplicationBuildDate(),
					mfr.getApplicationBuildRevision()));
			System.exit(1);
		}

		long start = System.currentTimeMillis();
		GtfsVtor gtfsvtor = new GtfsVtor(cmdLineArgs);
		gtfsvtor.validate();
		ReviewReport report = gtfsvtor.getReviewReport();
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
