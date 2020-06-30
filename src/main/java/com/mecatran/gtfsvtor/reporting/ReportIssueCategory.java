package com.mecatran.gtfsvtor.reporting;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReportIssueCategory implements Comparable<ReportIssueCategory> {

	private ReportIssueSeverity severity;
	private String categoryName;
	// TODO Add a numeric code?

	private static ConcurrentMap<ReportIssueCategory, ReportIssueCategory> CACHE = new ConcurrentHashMap<>();

	public static ReportIssueCategory create(ReportIssueSeverity severity,
			String categoryName) {
		ReportIssueCategory cat = new ReportIssueCategory(severity,
				categoryName);
		return CACHE.computeIfAbsent(cat, ct -> ct);
	}

	private ReportIssueCategory(ReportIssueSeverity severity,
			String categoryName) {
		this.severity = severity;
		this.categoryName = categoryName;
	}

	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(severity, categoryName);
	}

	@Override
	public boolean equals(Object another) {
		if (another == null)
			return false;
		if (another == this)
			return true;
		if (!(another instanceof ReportIssueCategory))
			return false;
		ReportIssueCategory other = (ReportIssueCategory) another;
		return severity == other.severity
				&& Objects.equals(categoryName, other.categoryName);
	}

	@Override
	public int compareTo(ReportIssueCategory cat) {
		int cmp = severity.compareTo(cat.severity);
		if (cmp == 0)
			cmp = categoryName.compareTo(cat.categoryName);
		return cmp;
	}
}
