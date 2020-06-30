package com.mecatran.gtfsvtor.reporting.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueCategory;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReviewReport;
import com.mecatran.gtfsvtor.reporting.ReviewReport.IssueCount;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;
import com.mecatran.gtfsvtor.utils.MiscUtils;
import com.mecatran.gtfsvtor.utils.Pair;

/**
 */
public class ClassifiedReviewReport {

	public static class IssuesSubGroup implements Comparable<IssuesSubGroup> {

		private List<DataObjectSourceRef> sourceRefs;
		private List<ReportIssue> issues = new ArrayList<>();
		private Map<String, ReportIssueSeverity> fieldSeverities = new HashMap<>();

		private IssuesSubGroup(List<DataObjectSourceRef> sourceRefs) {
			this.sourceRefs = sourceRefs;
		}

		private void addIssue(ReportIssue issue) {
			issues.add(issue);
		}

		private void sortAndIndex() {
			Collections.sort(issues, ReportIssue.makeComparator());
			for (ReportIssue issue : issues) {
				ReportIssueSeverity severity = issue.getSeverity();
				for (int i = 0; i < issue.getSourceRefs().size(); i++) {
					SourceRefWithFields siwf = issue.getSourceRefs().get(i);
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

		public List<DataObjectSourceRef> getSourceRefs() {
			return sourceRefs;
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
		public int compareTo(IssuesSubGroup o) {
			return MiscUtils.listCompare(this.sourceRefs, o.sourceRefs);
		}
	}

	public static class IssuesGroup implements Comparable<IssuesGroup> {

		private String groupName;
		private boolean displayCategoryCounter;
		private Map<List<DataObjectSourceRef>, IssuesSubGroup> subGroupMap = new HashMap<>();
		private Map<ReportIssueSeverity, AtomicInteger> severityCounter = new HashMap<>();
		private Map<ReportIssueCategory, AtomicInteger> categoryCounter = new HashMap<>();
		private List<IssuesSubGroup> subGroups;

		public IssuesGroup(String groupName, boolean displayCategoryCounters) {
			this.groupName = groupName;
			this.displayCategoryCounter = displayCategoryCounters;
		}

		public String getGroupName() {
			return groupName;
		}

		public void addIssue(ReportIssue issue) {
			ReportIssueSeverity severity = issue.getSeverity();
			ReportIssueCategory category = issue.getCategory();
			severityCounter
					.computeIfAbsent(severity, key -> new AtomicInteger())
					.addAndGet(1);
			categoryCounter
					.computeIfAbsent(category, cat -> new AtomicInteger())
					.addAndGet(1);
			subGroupMap.computeIfAbsent(
					issue.getSourceRefs().stream()
							.map(SourceRefWithFields::getSourceRef)
							.collect(Collectors.toList()),
					k -> new IssuesSubGroup(k)).addIssue(issue);
		}

		private void sortAndIndex() {
			subGroups = subGroupMap.values().stream()
					.collect(Collectors.toList());
			subGroups.forEach(IssuesSubGroup::sortAndIndex);
			Collections.sort(subGroups);
		}

		public List<IssuesSubGroup> getSubGroups() {
			return subGroups;
		}

		public List<Pair<ReportIssueSeverity, Integer>> getSeverityCounters() {
			return severityCounter.entrySet().stream()
					.map(kv -> new Pair<>(kv.getKey(), kv.getValue().get()))
					.sorted(Comparator.comparing(e -> e.getFirst()))
					.collect(Collectors.toList());
		}

		public boolean isDisplayCategoryCounters() {
			return displayCategoryCounter;
		}

		public List<Pair<ReportIssueCategory, Integer>> getCategoryCounters() {
			return categoryCounter.entrySet().stream()
					.map(kv -> new Pair<>(kv.getKey(), kv.getValue().get()))
					.sorted(Comparator.comparing(e -> e.getFirst()))
					.collect(Collectors.toList());
		}

		@Override
		public int compareTo(IssuesGroup o) {
			return groupName.compareTo(o.getGroupName());
		}
	}

	private ReviewReport report;
	private List<IssuesGroup> groups;

	public ClassifiedReviewReport(ReviewReport report) {
		this.report = report;
		Map<String, IssuesGroup> catMap = new HashMap<>();
		report.getReportIssues().forEach(issue -> {
			Pair<String, Boolean> groupInfo = getGroupInfo(issue);
			catMap.computeIfAbsent(groupInfo.getFirst(),
					k -> new IssuesGroup(groupInfo.getFirst(),
							groupInfo.getSecond()))
					.addIssue(issue);
		});
		groups = catMap.values().stream().collect(Collectors.toList());
		groups.forEach(IssuesGroup::sortAndIndex);
		Collections.sort(groups);
	}

	private Pair<String, Boolean> getGroupInfo(ReportIssue issue) {
		List<SourceRefWithFields> sourceInfos = issue.getSourceRefs();
		if (sourceInfos.isEmpty()) {
			// Issues w/o source info: use the issue category as group name
			return new Pair<>(issue.getCategoryName(), false);
		} else {
			// Issues w source info: use the FIRST table name as group name
			return new Pair<>(sourceInfos.get(0).getSourceRef().getTableName(),
					true);
		}
	}

	public Stream<IssuesGroup> getGroups() {
		return groups.stream();
	}

	public Stream<Pair<ReportIssueSeverity, IssueCount>> getSeverityCounters() {
		return Arrays.stream(ReportIssueSeverity.values())
				.map(sev -> new Pair<>(sev, report.issuesCountOfSeverity(sev)));
	}

	public Stream<Pair<ReportIssueCategory, IssueCount>> getCategoryCounters() {
		return report.getCategories().sorted()
				.map(cat -> new Pair<>(cat, report.issuesCountOfCategory(cat)));
	}
}
