package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.ReviewReport;

public class InMemoryReportLog implements ReportSink, ReviewReport {

	private List<ReportIssue> reportItems = new ArrayList<>();
	private ListMultimap<Class<? extends ReportIssue>, ReportIssue> reportIssuesByType = ArrayListMultimap
			.create();
	private ListMultimap<ReportIssueSeverity, ReportIssue> reportIssuesBySeverity = ArrayListMultimap
			.create();
	private Map<String, AtomicInteger> issuesCountPerCategory = new HashMap<>();
	private int maxIssuesPerCategory = Integer.MAX_VALUE;
	private boolean printIssues = false;

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
				if (printIssues) {
					System.out.println(PlainTextIssueFormatter.format(issue));
					System.out.flush();
				}
			}
		}
	}

	@Override
	public List<ReportIssue> getReportIssues() {
		return reportItems;
	}

	@Override
	public <T extends ReportIssue> List<T> getReportIssues(
			Class<T> issueClass) {
		@SuppressWarnings("unchecked")
		List<T> ret = (List<T>) reportIssuesByType.get(issueClass);
		return ret;
	}

	@Override
	public int issuesCountOfSeverity(ReportIssueSeverity severity) {
		return reportIssuesBySeverity.asMap()
				.getOrDefault(severity, Collections.emptyList()).size();
	}
}
