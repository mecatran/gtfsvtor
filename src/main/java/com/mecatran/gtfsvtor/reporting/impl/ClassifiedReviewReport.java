package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;
import com.mecatran.gtfsvtor.utils.MiscUtils;

public class ClassifiedReviewReport {

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
		private Map<List<DataObjectSourceInfo>, IssuesSubCategory> subCatMap = new HashMap<>();
		private List<IssuesSubCategory> subCategories;

		public IssuesCategory(String categoryName) {
			this.categoryName = categoryName;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void addIssue(ReportIssue issue) {
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

		@Override
		public int compareTo(IssuesCategory o) {
			return categoryName.compareTo(o.getCategoryName());
		}
	}

	// List of category of issues
	private List<IssuesCategory> categories;

	public ClassifiedReviewReport(ReviewReport report) {
		Map<String, IssuesCategory> catMap = new HashMap<>();
		for (ReportIssue issue : report.getReportIssues()) {
			String categoryName = getCategoryName(issue);
			catMap.computeIfAbsent(categoryName,
					k -> new IssuesCategory(categoryName)).addIssue(issue);
		}
		categories = catMap.values().stream().collect(Collectors.toList());
		categories.forEach(IssuesCategory::sortAndIndex);
		Collections.sort(categories);
	}

	private String getCategoryName(ReportIssue issue) {
		List<SourceInfoWithFields> sourceInfos = issue.getSourceInfos();
		if (sourceInfos.isEmpty()) {
			// Issues w/o source info: use the issue class as category
			// TODO Use translated issue name
			return issue.getClass().getSimpleName();
		} else {
			// Issues w source info: use the FIRST table name as category
			return sourceInfos.get(0).getSourceInfo().getTable().getTableName();
		}
	}

	public List<IssuesCategory> getCategories() {
		return categories;
	}

}
