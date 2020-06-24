package com.mecatran.gtfsvtor.reporting;

import java.util.stream.Stream;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public interface ReviewReport {

	/**
	 * @return A stream of all report items, in the order they were reported.
	 */
	public Stream<ReportIssue> getReportIssues();

	/**
	 * @param reportClass
	 * @return A stream of report items of the given class, in the order they
	 *         were reported.
	 */
	public <T extends ReportIssue> Stream<T> getReportIssues(
			Class<T> reportClass);

	/**
	 * @param severity
	 * @return The total number of issues so far of this severity.
	 */
	public int issuesCountOfSeverity(ReportIssueSeverity severity);

	/**
	 * @param ref The ref to load source info from.
	 * @return The source info containing the source fields.
	 */
	public DataObjectSourceInfo getSourceInfo(DataObjectSourceRef ref);

}
