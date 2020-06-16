package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.impl.SourceInfoDataReloader.PostLoadable;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

public class InMemoryReportLog
		implements ReportSink, ReviewReport, PostLoadable {

	private List<ReportIssue> reportItems = new ArrayList<>();
	private ListMultimap<Class<? extends ReportIssue>, ReportIssue> reportIssuesByType = ArrayListMultimap
			.create();
	private ListMultimap<ReportIssueSeverity, ReportIssue> reportIssuesBySeverity = ArrayListMultimap
			.create();
	private Map<String, AtomicInteger> issuesCountPerCategory = new HashMap<>();
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
	private Map<DataObjectSourceRef, DataObjectSourceInfo> loadedSourceInfos = new HashMap<>();
	private Set<DataObjectSourceRef> unloadedSourceRefs = new HashSet<>();

	public InMemoryReportLog() {
	}

	public InMemoryReportLog withPrintIssues(boolean printIssues) {
		this.printIssues = printIssues;
		return this;
	}

	public InMemoryReportLog withMaxIssues(int maxIssues) {
		this.maxIssuesPerCategory = maxIssues;
		return this;
	}

	@Override
	public void report(ReportIssue issue) {
		synchronized (reportItems) {
			int count = issuesCountPerCategory
					.computeIfAbsent(issue.getCategoryName(),
							cat -> new AtomicInteger(0))
					.addAndGet(1);
			boolean skip = count >= maxIssuesPerCategory;
			if (!skip) {
				reportItems.add(issue);
				reportIssuesByType.put(issue.getClass(), issue);
				reportIssuesBySeverity.put(issue.getSeverity(), issue);
				/* Add each new unknown source info ref as unloaded */
				issue.getSourceRefs().stream()
						.map(refwf -> refwf.getSourceRef())
						.filter(ref -> !loadedSourceInfos.containsKey(ref))
						.forEach(unloadedSourceRefs::add);
				if (printIssues) {
					System.err.println(
							PlainTextIssueFormatter.format(null, issue));
				}
			}
		}
	}

	@Override
	public void report(ReportIssue issue, DataObjectSourceInfo... infoList) {
		List<SourceRefWithFields> refs = issue.getSourceRefs();
		if (refs.size() != infoList.length)
			throw new IllegalArgumentException("Trying to report an issue with "
					+ issue.getSourceRefs().size() + " refs, but with "
					+ infoList.length
					+ " source info. Both sizes should match!");
		/*
		 * We have to store the infos first, to prevent the report(issue) to
		 * create a loaded source info.
		 */
		for (int i = 0; i < infoList.length; i++) {
			DataObjectSourceInfo info = infoList[i];
			if (info == null)
				continue; // This is perfectly legal
			DataObjectSourceRef ref = refs.get(i).getSourceRef();
			if (ref.getLineNumber() != info.getLineNumber() || !ref
					.getTableName().equals(info.getTable().getTableName())) {
				throw new IllegalArgumentException("Ref #" + i + " " + ref
						+ " does not match info " + info + "!");
			}
			loadedSourceInfos.put(ref, info);
			// This line should not be necessary,
			// but just in case...
			unloadedSourceRefs.remove(ref);
		}
		this.report(issue);
	}

	@Override
	public Stream<ReportIssue> getReportIssues() {
		return reportItems.stream();
	}

	@Override
	public <T extends ReportIssue> Stream<T> getReportIssues(
			Class<T> issueClass) {
		@SuppressWarnings("unchecked")
		Stream<T> ret = (Stream<T>) reportIssuesByType.get(issueClass).stream();
		return ret;
	}

	@Override
	public int issuesCountOfSeverity(ReportIssueSeverity severity) {
		return reportIssuesBySeverity.asMap()
				.getOrDefault(severity, Collections.emptyList()).size();
	}

	@Override
	public DataObjectSourceInfo getSourceInfo(DataObjectSourceRef ref) {
		// TODO In case the source info is not found, return a dummy info
		DataObjectSourceInfo info = loadedSourceInfos.get(ref);
		if (info == null) {
			throw new IllegalArgumentException(
					"Source info not found for " + ref);
		}
		return info;
	}

	@Override
	public Stream<DataObjectSourceRef> getSourceRefsToPostLoad() {
		return unloadedSourceRefs.stream();
	}

	@Override
	public void addSourceInfo(DataObjectSourceRef ref,
			DataObjectSourceInfo info) {
		loadedSourceInfos.put(ref, info);
		unloadedSourceRefs.remove(ref);
	}
}
