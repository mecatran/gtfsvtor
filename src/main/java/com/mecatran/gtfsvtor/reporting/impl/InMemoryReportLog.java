package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private boolean printIssues = false;

	public InMemoryReportLog() {
	}

	public InMemoryReportLog withPrintIssues(boolean printIssues) {
		this.printIssues = printIssues;
		return this;
	}

	@Override
	public void report(ReportIssue issue) {
		synchronized (reportItems) {
			if (printIssues) {
				System.out.println(issue.getSeverity() + ": "
						+ PlainTextIssueFormatter.format(issue));
			}
			reportItems.add(issue);
			reportIssuesByType.put(issue.getClass(), issue);
			reportIssuesBySeverity.put(issue.getSeverity(), issue);
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
