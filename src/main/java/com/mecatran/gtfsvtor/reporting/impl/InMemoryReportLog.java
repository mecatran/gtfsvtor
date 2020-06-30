package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueCategory;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.SourceInfoFactory;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

public class InMemoryReportLog implements ReportSink, ReviewReport {

	private static class IssueCountImpl implements IssueCount {
		private int total;
		private int reported;

		@Override
		public int totalCount() {
			return total;
		}

		@Override
		public int reportedCount() {
			return reported;
		}
	}

	private List<ReportIssue> reportIssues = new ArrayList<>();
	// TODO Remove the two maps below.
	// Report issue indexing should be done afterwards, in a wrapper
	private ListMultimap<Class<? extends ReportIssue>, ReportIssue> reportIssuesByType = ArrayListMultimap
			.create();
	private ListMultimap<ReportIssueSeverity, ReportIssue> reportIssuesBySeverity = ArrayListMultimap
			.create();
	private Map<ReportIssueSeverity, IssueCountImpl> issuesCountPerSeverity = new HashMap<>();
	private Map<ReportIssueCategory, IssueCountImpl> issuesCountPerCategory = new HashMap<>();
	/*
	 * Note: we do not use this threshold for now, as this breaks the
	 * computation of total number of report issue per category AND per source
	 * info in the report formatter. In order to have correct computation we
	 * would need to store the total number of issues per categories AND per
	 * source info here. TODO: either remove this capability in the report
	 * formatter (or disable it), or count total issues per category and source
	 * info.
	 */
	private int maxIssuesPerCategory = Integer.MAX_VALUE;
	private boolean printIssues = false;
	private SourceInfoFactory sourceInfoFactory;

	public InMemoryReportLog() {
	}

	public InMemoryReportLog withSourceInfoFactory(
			SourceInfoFactory sourceInfoFactory) {
		this.sourceInfoFactory = sourceInfoFactory;
		return this;
	}

	public InMemoryReportLog withPrintIssues(boolean printIssues) {
		this.printIssues = printIssues;
		return this;
	}

	public InMemoryReportLog withMaxIssuesPerCategory(int maxIssues) {
		this.maxIssuesPerCategory = maxIssues;
		return this;
	}

	@Override
	public void report(ReportIssue issue) {
		doReport(issue);
	}

	@Override
	public void report(ReportIssue issue, DataObjectSourceInfo... infoList) {
		List<SourceRefWithFields> refs = issue.getSourceRefs();
		if (refs.size() != infoList.length)
			throw new IllegalArgumentException("Trying to report an issue with "
					+ issue.getSourceRefs().size() + " refs, but with "
					+ infoList.length
					+ " source info. Both sizes should match!");
		if (doReport(issue)) {
			/*
			 * Only register source info if we stored the issue, to prevent
			 * unncessary memory use. Note that the source info factory is
			 * responsible of removing the reference to post-load we just added
			 * in the doReport() call, in case we register the corresponding
			 * source info here.
			 */
			for (int i = 0; i < infoList.length; i++) {
				DataObjectSourceInfo info = infoList[i];
				if (info == null)
					continue; // This is perfectly legal
				DataObjectSourceRef ref = refs.get(i).getSourceRef();
				if (ref.getLineNumber() != info.getLineNumber()
						|| !ref.getTableName()
								.equals(info.getTable().getTableName())) {
					throw new IllegalArgumentException("Ref #" + i + " " + ref
							+ " does not match info " + info + "!");
				}
				sourceInfoFactory.registerSourceInfo(ref, info);
			}
		}
	}

	private boolean doReport(ReportIssue issue) {
		synchronized (reportIssues) {
			IssueCountImpl countPerSeverity = issuesCountPerSeverity
					.computeIfAbsent(issue.getSeverity(),
							cat -> new IssueCountImpl());
			IssueCountImpl countPerCategory = issuesCountPerCategory
					.computeIfAbsent(issue.getCategory(),
							cat -> new IssueCountImpl());
			countPerSeverity.total++;
			countPerCategory.total++;
			boolean keep = countPerCategory.total <= maxIssuesPerCategory;
			if (keep) {
				countPerSeverity.reported++;
				countPerCategory.reported++;
				reportIssues.add(issue);
				reportIssuesByType.put(issue.getClass(), issue);
				reportIssuesBySeverity.put(issue.getSeverity(), issue);
				/* Register each source ref to load */
				issue.getSourceRefs().stream()
						.map(refwf -> refwf.getSourceRef())
						.forEach(ref -> sourceInfoFactory
								.registerSourceRef(ref));
				if (printIssues) {
					/*
					 * Do *not* provide the sourceInfoFactory to the formatter.
					 * With lazy-loading, this will work, but will be highly
					 * inefficient.
					 */
					System.err.println(PlainTextIssueFormatter
							.format(Optional.empty(), issue));
				}
			}
			return keep;
		}
	}

	@Override
	public Stream<ReportIssue> getReportIssues() {
		return reportIssues.stream();
	}

	@Override
	public <T extends ReportIssue> Stream<T> getReportIssues(
			Class<T> issueClass) {
		@SuppressWarnings("unchecked")
		Stream<T> ret = (Stream<T>) reportIssuesByType.get(issueClass).stream();
		return ret;
	}

	@Override
	public IssueCount issuesCountOfSeverity(ReportIssueSeverity severity) {
		return issuesCountPerSeverity.getOrDefault(severity,
				new IssueCountImpl());
	}

	@Override
	public IssueCount issuesCountOfCategory(ReportIssueCategory category) {
		return issuesCountPerCategory.getOrDefault(category,
				new IssueCountImpl());
	}

	@Override
	public Stream<ReportIssueCategory> getCategories() {
		return issuesCountPerCategory.keySet().stream().sorted();
	}

	@Override
	public DataObjectSourceInfo getSourceInfo(DataObjectSourceRef ref) {
		// TODO Accept this and return dummy source info?
		return sourceInfoFactory.getSourceInfo(ref)
				.orElseThrow(() -> new IllegalArgumentException(
						"Source info not found for " + ref));
	}
}
