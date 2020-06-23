package com.mecatran.gtfsvtor.cmdline;

import com.beust.jcommander.Parameter;
import com.mecatran.gtfsvtor.lib.GtfsVtorOptions;

public class CmdLineArgs implements GtfsVtorOptions {

	@Parameter(names = { "-h",
			"--help" }, description = "Display this help and exit")
	private boolean help = false;

	@Parameter(names = {
			"--listValidators" }, description = "List validators and their parameters, and exit")
	private boolean listValidators = false;

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
	private String outputReportFile = "validation-results.html";

	@Parameter(names = { "-l",
			"--limit" }, description = "Limit number of issues per category")
	private int maxIssuesPerCategoryLimit = 100;

	@Parameter(names = {
			"--numThreads" }, description = "Number of threads for running DAO validators in parallel")
	private int numThreads = 1;

	@Parameter(names = { "--maxStopTimesInterleaving" }, description = ""
			+ "Max number of interleaved trips in stop_times.txt "
			+ "(number of concurrent 'opened' trips) in PACKED stop time mode. "
			+ "Increase this option if you have some data not ordered by trip ID, "
			+ "but still want to use PACKED mode, to improve loading performances. "
			+ "Otherwise, use --stopTimesMode UNSORTED or AUTO option.")
	private int maxStopTimesInterleaving = 100;

	@Parameter(names = { "--maxShapePointsInterleaving" }, description = ""
			+ "Max number of interleaved shapes in shapes.txt "
			+ "(number of concurrent 'opened' shapes). "
			+ "Use/increase this option if you have lots of unordered shape points in shapes.txt, "
			+ "to improve loading performances.")
	private int maxShapePointsInterleaving = 100;

	@Parameter(names = { "--stopTimesMode" }, description = ""
			+ "Stop times DAO implementation to use. "
			+ "PACKED: Optimized for memory, but can be slower if stop_times.txt are not sorted by trip ID. "
			+ "UNSORTED: Work best for stop_times.txt unsorted by trip ID, but uses more memory. "
			+ "AUTO: Start in PACKED mode, then switch to UNSORTED mode if required. ")
	private StopTimesDaoMode stopTimesDaoMode = StopTimesDaoMode.AUTO;

	@Parameter(names = { "--shapePointsMode" }, description = ""
			+ "Shape points DAO implementation to use. "
			+ "PACKED: Optimized for memory, but can be slower if shapes.txt are not sorted by shape ID. "
			+ "SIMPLE: Work for all situations, but uses more memory. ")
	private ShapePointsDaoMode shapePointsDaoMode = ShapePointsDaoMode.PACKED;

	@Parameter(description = "<GTFS file to validate>")
	private String gtfsFile;

	public boolean isHelp() {
		return help;
	}

	public boolean isListValidators() {
		return listValidators;
	}

	@Override
	public boolean isVerbose() {
		return verbose;
	}

	@Override
	public boolean isPrintIssues() {
		return printIssues;
	}

	@Override
	public String getConfigFile() {
		return configFile;
	}

	@Override
	public String getOutputReportFile() {
		return outputReportFile;
	}

	@Override
	public int getMaxIssuesPerCategoryLimit() {
		return maxIssuesPerCategoryLimit;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
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
	public String getGtfsFile() {
		return gtfsFile;
	}
}
