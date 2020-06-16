package com.mecatran.gtfsvtor.reporting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mecatran.gtfsvtor.utils.MiscUtils;

public interface ReportIssue {

	/**
	 * @return Sorted list of source references. Empty by default if no source
	 *         info is available. Please do not return null.
	 */
	public default List<SourceRefWithFields> getSourceRefs() {
		return Collections.emptyList();
	}

	/**
	 * Format the issue with the given formatter.
	 * 
	 * @param fmt
	 * @return The formatted issue.
	 */
	public void format(IssueFormatter fmt);

	/**
	 * This method can be safely overriden by specific issue class that want to
	 * return various severities depending on the issue data (for example stop
	 * too far from parent station, according to the distance).
	 */
	public default ReportIssueSeverity getSeverity() {
		Class<? extends ReportIssue> issueClass = this.getClass();
		if (issueClass.isAnnotationPresent(ReportIssuePolicy.class)) {
			ReportIssuePolicy policy = issueClass
					.getAnnotation(ReportIssuePolicy.class);
			return policy.severity();
		}
		// If not annotated, consider issue an error
		return ReportIssueSeverity.ERROR;
	}

	/**
	 * This method can be overriden by specific issue class that want to return
	 * various names for the same issue class. Be careful, as this will split
	 * issues with different names in different categories; so refrain from
	 * using variables in the name which would create dozens of different name /
	 * categories.
	 */
	public default String getCategoryName() {
		Class<? extends ReportIssue> issueClass = this.getClass();
		String name = null;
		if (issueClass.isAnnotationPresent(ReportIssuePolicy.class)) {
			ReportIssuePolicy policy = issueClass
					.getAnnotation(ReportIssuePolicy.class);
			name = policy.categoryName();
		}
		if (name == null || name.isEmpty()) {
			name = issueClass.getSimpleName();
		}
		return name;
	}

	public static Comparator<ReportIssue> makeComparator() {
		return new Comparator<ReportIssue>() {
			@Override
			public int compare(ReportIssue o1, ReportIssue o2) {
				List<SourceRefWithFields> si1 = o1.getSourceRefs();
				List<SourceRefWithFields> si2 = o2.getSourceRefs();
				if (si1.isEmpty() && si2.isEmpty()) {
					if (o1 instanceof Comparable
							&& o1.getClass().equals(o2.getClass())) {
						@SuppressWarnings("unchecked")
						Comparable<ReportIssue> co1 = (Comparable<ReportIssue>) o1;
						return co1.compareTo(o2);
					}
					// TODO: Sort issues by severity?
					return si1.getClass().getSimpleName()
							.compareTo(si2.getClass().getSimpleName());
				}
				return MiscUtils.listCompare(si1, si2);
			}
		};
	}
}
