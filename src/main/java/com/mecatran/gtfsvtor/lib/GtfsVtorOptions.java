package com.mecatran.gtfsvtor.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.mecatran.gtfsvtor.reporting.FormattingOptions;
import com.mecatran.gtfsvtor.reporting.FormattingOptions.SpeedUnit;

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

	public default Optional<String> getConfigFile() {
		return Optional.empty();
	}

	public default Optional<NamedDataIO> getHtmlDataIO() throws IOException {
		return Optional.empty();
	}

	public default Optional<NamedDataIO> getJsonDataIO() throws IOException {
		return Optional.empty();
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

	public default FormattingOptions getFormattingOptions() {
		return new FormattingOptions(SpeedUnit.MPS);
	}

	public String getGtfsFile();
}
