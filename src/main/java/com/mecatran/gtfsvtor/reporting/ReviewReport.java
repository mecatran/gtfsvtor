package com.mecatran.gtfsvtor.reporting;

import java.util.stream.Stream;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public interface ReviewReport {

	public interface IssueCount {

		/**
		 * @return The total number of issues.
		 */
		public int totalCount();

		/**
		 * @return The number of issues that are reported (kept in the report).
		 *         The number of skipped issues is (total - reported).
		 */
		public int reportedCount();
	}

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
	 * @return The issue count for this severity.
	 */
	public IssueCount issuesCountOfSeverity(ReportIssueSeverity severity);

	/**
	 * @param category
	 * @return The issue count for this category.
	 */
	public IssueCount issuesCountOfCategory(ReportIssueCategory category);

	/**
	 * @return The list of categories of all issues.
	 */
	public Stream<ReportIssueCategory> getCategories();

	/**
	 * @param ref The ref to load source info from.
	 * @return The source info containing the source fields.
	 */
	public DataObjectSourceInfo getSourceInfo(DataObjectSourceRef ref);

}
