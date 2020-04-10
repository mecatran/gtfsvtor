package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Future feed")
public class FutureFeedWarning implements ReportIssue {

	private GtfsLogicalDate firstDate;
	private GtfsLogicalDate cutoffDate;

	public FutureFeedWarning(GtfsLogicalDate firstDate,
			GtfsLogicalDate cutoffDate) {
		this.firstDate = firstDate;
		this.cutoffDate = cutoffDate;
	}

	public GtfsLogicalDate getFirstDate() {
		return firstDate;
	}

	public GtfsLogicalDate getCutoffDate() {
		return cutoffDate;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Feed starting too late or in the future. First date with service is {0} > {1}",
				fmt.date(firstDate), fmt.date(cutoffDate));
	}
}
