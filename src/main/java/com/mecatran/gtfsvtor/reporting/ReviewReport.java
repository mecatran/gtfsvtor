package com.mecatran.gtfsvtor.reporting;

import java.util.List;

public interface ReviewReport {

	/**
	 * @return The list of all report items, in the order they were added to the
	 *         report sink.
	 */
	public List<ReportIssue> getReportIssues();

	/**
	 * @param reportClass
	 * @return The list of report items, in the order they were added to the
	 *         report sink.
	 */
	public <T extends ReportIssue> List<T> getReportIssues(
			Class<T> reportClass);

	/**
	 * @param severity
	 * @return The total number of issues so far of this severity.
	 */
	public int issuesCountOfSeverity(ReportIssueSeverity severity);

}
