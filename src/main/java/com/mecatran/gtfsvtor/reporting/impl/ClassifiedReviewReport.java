package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;
import com.mecatran.gtfsvtor.utils.MiscUtils;

public class ClassifiedReviewReport {

	public static class CategoryCounter implements Comparable<CategoryCounter> {

		private String categoryName;
		private ReportIssueSeverity severity;
		private int maxCount;
		private int totalCount;
		private int displayedCount;

		private CategoryCounter() {
		}

		private CategoryCounter(String categoryName,
				ReportIssueSeverity severity, int maxCount) {
			this.categoryName = categoryName;
			this.severity = severity;
			this.maxCount = maxCount;
		}

		private static CategoryCounter merge(CategoryCounter cc1,
				CategoryCounter cc2) {
			if (!Objects.equals(cc1.categoryName, cc2.categoryName)
					|| !Objects.equals(cc1.severity, cc2.severity))
				throw new IllegalArgumentException(
						"Cannot merge CategoryCounter with different names or severity");
			CategoryCounter cc = new CategoryCounter();
			cc.severity = cc1.severity;
			cc.categoryName = cc1.categoryName;
			cc.maxCount = cc1.maxCount + cc2.maxCount;
			cc.totalCount = cc1.totalCount + cc2.totalCount;
			cc.displayedCount = cc1.displayedCount + cc2.displayedCount;
			return cc;
		}

		private boolean inc() {
			this.totalCount++;
			this.displayedCount++;
			if (this.displayedCount > this.maxCount) {
				this.displayedCount = this.maxCount;
				return true;
			} else {
				return false;
			}
		}

		public String getCategoryName() {
			return categoryName;
		}

		public ReportIssueSeverity getSeverity() {
			return severity;
		}

		public int getTotalCount() {
			return totalCount;
		}

		public int getDisplayedCount() {
			return displayedCount;
		}

		public boolean isTruncated() {
			return displayedCount != totalCount;
		}

		@Override
		public int compareTo(CategoryCounter o) {
			// Order by severity, then category name
			String a = severity.ordinal() + ":" + categoryName;
			String b = o.severity.ordinal() + ":" + o.categoryName;
			return a.compareTo(b);
		}
	}

	public static class IssuesSubCategory
			implements Comparable<IssuesSubCategory> {

		private List<DataObjectSourceInfo> sourceInfos;
		private List<ReportIssue> issues = new ArrayList<>();
		private Map<String, ReportIssueSeverity> fieldSeverities = new HashMap<>();

		private IssuesSubCategory(List<DataObjectSourceInfo> sourceInfos) {
			this.sourceInfos = sourceInfos;
		}

		private void addIssue(ReportIssue issue) {
			issues.add(issue);
		}

		private void sortAndIndex() {
			Collections.sort(issues, ReportIssue.makeComparator());
			for (ReportIssue issue : issues) {
				ReportIssueSeverity severity = issue.getSeverity();
				for (int i = 0; i < issue.getSourceInfos().size(); i++) {
					SourceInfoWithFields siwf = issue.getSourceInfos().get(i);
					for (String field : siwf.getFieldNames()) {
						for (String prefix : Arrays.asList(":", i + ":")) {
							ReportIssueSeverity existingSeverity = fieldSeverities
									.get(prefix + field);
							if (existingSeverity == null || severity
									.compareTo(existingSeverity) > 0) {
								fieldSeverities.put(prefix + field, severity);
							}
						}
					}
				}
			}
		}

		public List<DataObjectSourceInfo> getSourceInfos() {
			return sourceInfos;
		}

		public ReportIssueSeverity getFieldSeverity(String headerColumn) {
			return fieldSeverities.get(":" + headerColumn);
		}

		public ReportIssueSeverity getFieldSeverity(int sourceInfoIndex,
				String headerColumn) {
			return fieldSeverities.get(sourceInfoIndex + ":" + headerColumn);
		}

		public List<ReportIssue> getIssues() {
			return Collections.unmodifiableList(issues);
		}

		@Override
		public int compareTo(IssuesSubCategory o) {
			return MiscUtils.listCompare(this.sourceInfos, o.sourceInfos);
		}
	}

	public static class IssuesCategory implements Comparable<IssuesCategory> {

		private String categoryName;
		private int maxIssues;
		private Map<List<DataObjectSourceInfo>, IssuesSubCategory> subCatMap = new HashMap<>();
		private Map<String, CategoryCounter> categoryCounters = new HashMap<>();
		private Map<ReportIssueSeverity, CategoryCounter> severityCounters = new HashMap<>();
		private List<IssuesSubCategory> subCategories;

		public IssuesCategory(String categoryName, int maxIssues) {
			this.categoryName = categoryName;
			this.maxIssues = maxIssues;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void addIssue(ReportIssue issue) {
			ReportIssueSeverity severity = issue.getSeverity();
			severityCounters.computeIfAbsent(severity,
					name -> new CategoryCounter(severity.toString(), severity,
							Integer.MAX_VALUE))
					.inc();
			String categoryName = issue.getCategoryName();
			boolean overflow = categoryCounters.computeIfAbsent(categoryName,
					name -> new CategoryCounter(name, severity, maxIssues))
					.inc();
			if (overflow)
				return;
			subCatMap.computeIfAbsent(
					issue.getSourceInfos().stream()
							.map(SourceInfoWithFields::getSourceInfo)
							.collect(Collectors.toList()),
					k -> new IssuesSubCategory(k)).addIssue(issue);
		}

		private void sortAndIndex() {
			subCategories = subCatMap.values().stream()
					.collect(Collectors.toList());
			subCategories.forEach(IssuesSubCategory::sortAndIndex);
			Collections.sort(subCategories);
		}

		public List<IssuesSubCategory> getSubCategories() {
			return subCategories;
		}

		public List<CategoryCounter> getSeverityCounters() {
			return severityCounters.values().stream().sorted()
					.collect(Collectors.toList());
		}

		public List<CategoryCounter> getCategoryCounters() {
			return categoryCounters.values().stream().sorted()
					.collect(Collectors.toList());
		}

		@Override
		public int compareTo(IssuesCategory o) {
			return categoryName.compareTo(o.getCategoryName());
		}
	}

	// List of category of issues
	private List<IssuesCategory> categories;

	public ClassifiedReviewReport(ReviewReport report,
			int maxIssuePerCategory) {
		Map<String, IssuesCategory> catMap = new HashMap<>();
		for (ReportIssue issue : report.getReportIssues()) {
			String categoryName = getCategoryName(issue);
			catMap.computeIfAbsent(categoryName,
					k -> new IssuesCategory(categoryName, maxIssuePerCategory))
					.addIssue(issue);
		}
		categories = catMap.values().stream().collect(Collectors.toList());
		categories.forEach(IssuesCategory::sortAndIndex);
		Collections.sort(categories);
	}

	private String getCategoryName(ReportIssue issue) {
		List<SourceInfoWithFields> sourceInfos = issue.getSourceInfos();
		if (sourceInfos.isEmpty()) {
			// Issues w/o source info: use the issue class as category
			return issue.getCategoryName();
		} else {
			// Issues w source info: use the FIRST table name as category
			return sourceInfos.get(0).getSourceInfo().getTable().getTableName();
		}
	}

	public List<IssuesCategory> getCategories() {
		return categories;
	}

	public List<CategoryCounter> getSeverityCounters() {
		return getCounters(cat -> cat.getSeverityCounters());
	}

	public List<CategoryCounter> getCategoryCounters() {
		return getCounters(cat -> cat.getCategoryCounters());
	}

	private List<CategoryCounter> getCounters(
			Function<? super IssuesCategory, List<CategoryCounter>> func) {
		// Java 8 streams are cool, but is this very readable?
		return categories.stream()
				// Merge lists
				.flatMap(cat -> func.apply(cat).stream())
				// Collect and merge by category name
				.collect(Collectors.toMap(c -> c.getCategoryName(), c -> c,
						(c1, c2) -> CategoryCounter.merge(c1, c2)))
				// Sort
				.values().stream().sorted().collect(Collectors.toList());
	}
}
