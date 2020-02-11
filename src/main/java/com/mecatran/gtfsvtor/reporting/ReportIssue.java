package com.mecatran.gtfsvtor.reporting;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mecatran.gtfsvtor.utils.MiscUtils;

public interface ReportIssue {

	/**
	 * @return Sorted list of source infos. Empty by default if no source info
	 *         is available. Please do not return null.
	 */
	public default List<SourceInfoWithFields> getSourceInfos() {
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

	public static Comparator<ReportIssue> makeComparator() {
		return new Comparator<ReportIssue>() {
			@Override
			public int compare(ReportIssue o1, ReportIssue o2) {
				List<SourceInfoWithFields> si1 = o1.getSourceInfos();
				List<SourceInfoWithFields> si2 = o2.getSourceInfos();
				if (si1.isEmpty() && si2.isEmpty()) {
					return si1.getClass().getSimpleName()
							.compareTo(si2.getClass().getSimpleName());
				}
				if (si1.isEmpty())
					return 1;
				if (si2.isEmpty())
					return -1;
				return MiscUtils.listCompare(si1, si2);
			}
		};
	}
}
