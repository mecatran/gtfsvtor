package com.mecatran.gtfsvtor.lib;

import java.io.IOException;
import java.io.OutputStream;

public interface GtfsVtorOptions {

	public enum StopTimesDaoMode {
		AUTO, PACKED, UNSORTED
	}

	public enum ShapePointsDaoMode {
		AUTO, PACKED, UNSORTED
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

	public default OutputStream getHtmlOutputStream() throws IOException {
		return null;
	}

	public default OutputStream getJsonOutputStream() throws IOException {
		return null;
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
		return ShapePointsDaoMode.AUTO;
	}

	public String getGtfsFile();
}
