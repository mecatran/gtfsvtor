package com.mecatran.gtfsvtor.reporting;

import java.util.stream.Stream;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public interface ReviewReport {

	/**
	 * @return A stream of all report items, in the order they were added to the
	 *         report sink.
	 */
	public Stream<ReportIssue> getReportIssues();

	/**
	 * @param reportClass
	 * @return The list of report items, in the order they were added to the
	 *         report sink.
	 */
	public <T extends ReportIssue> Stream<T> getReportIssues(
			Class<T> reportClass);

	/**
	 * @param severity
	 * @return The total number of issues so far of this severity.
	 */
	public int issuesCountOfSeverity(ReportIssueSeverity severity);

	/**
	 */
	public DataObjectSourceInfo getSourceInfo(DataObjectSourceRef ref);

}
