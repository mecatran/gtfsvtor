package com.mecatran.gtfsvtor.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public interface GtfsVtorOptions {

	public interface NamedDataIO {

		public OutputStream getOutputStream() throws IOException;

		public Optional<InputStream> getInputStream() throws IOException;

		public String getName();
	}

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

	public default NamedDataIO getHtmlDataIO() throws IOException {
		return null;
	}

	public default NamedDataIO getJsonDataIO() throws IOException {
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
