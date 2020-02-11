package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.ReviewReport;

public class InMemoryReportLog implements ReportSink, ReviewReport {

	private List<ReportIssue> reportItems = new ArrayList<>();
	private ListMultimap<Class<? extends ReportIssue>, ReportIssue> reportItemsByType = ArrayListMultimap
			.create();

	@Override
	public void report(ReportIssue issue) {
		// TODO Remove
		System.out.println(issue.getSeverity() + ": "
				+ PlainTextIssueFormatter.format(issue));
		reportItems.add(issue);
		reportItemsByType.put(issue.getClass(), issue);
	}

	@Override
	public List<ReportIssue> getReportIssues() {
		return reportItems;
	}

	@Override
	public <T extends ReportIssue> List<T> getReportIssues(
			Class<T> issueClass) {
		@SuppressWarnings("unchecked")
		List<T> ret = (List<T>) reportItemsByType.get(issueClass);
		return ret;
	}

}
