package com.mecatran.gtfsvtor.lib;

public interface GtfsVtorOptions {

	public enum StopTimesDaoMode {
		AUTO, PACKED, UNSORTED
	}

	public enum ShapePointsDaoMode {
		SIMPLE, PACKED
	}

	public default boolean isVerbose() {
		return false;
	}

	public default boolean isPrintIssues() {
		return false;
	}

	public default String getConfigFile() {
		return null;
	}

	public default String getOutputReportFile() {
		return "validation-results.html";
	}

	public default int getMaxIssuesPerCategoryLimit() {
		return 100;
	}

	public default int getNumThreads() {
		return 1;
	}

	public default int getMaxStopTimeInterleaving() {
		return 100;
	}

	public default int getMaxShapePointsInterleaving() {
		return 100;
	}

	public default StopTimesDaoMode getStopTimesDaoMode() {
		return StopTimesDaoMode.AUTO;
	}

	public default ShapePointsDaoMode getShapePointsDaoMode() {
		return ShapePointsDaoMode.PACKED;
	}

	public String getGtfsFile();
}
