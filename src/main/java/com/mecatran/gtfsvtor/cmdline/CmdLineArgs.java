package com.mecatran.gtfsvtor.cmdline;

import com.beust.jcommander.Parameter;

public class CmdLineArgs {

	@Parameter(names = { "-h",
			"--help" }, description = "Display this help and exit")
	private boolean help = false;

	@Parameter(names = { "-v",
			"--verbose" }, description = "Enable verbose mode")
	private boolean verbose = false;

	@Parameter(names = { "-p",
			"--printIssues" }, description = "Print issues log to standard output")
	private boolean printIssues = false;

	@Parameter(names = { "-c",
			"--config" }, description = "Configuration file to load (properties file)")
	private String configFile = null;

	@Parameter(names = { "-o",
			"--output" }, description = "Validation report output file")
	private String outputReportFile = "validation-report.html";

	@Parameter(names = { "-l",
			"--limit" }, description = "Limit number of issues per category")
	private int maxIssuesPerCategoryLimit = 100;

	@Parameter(description = "<GTFS file to validate>")
	private String gtfsFile;

	public boolean isHelp() {
		return help;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isPrintIssues() {
		return printIssues;
	}

	public String getConfigFile() {
		return configFile;
	}

	public String getOutputReportFile() {
		return outputReportFile;
	}

	public String getGtfsFile() {
		return gtfsFile;
	}

	public int getMaxIssuesPerCategoryLimit() {
		return maxIssuesPerCategoryLimit;
	}
}
