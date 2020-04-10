package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Expired feed")
public class ExpiredFeedWarning implements ReportIssue {

	private GtfsLogicalDate lastDate;
	private GtfsLogicalDate cutoffDate;

	public ExpiredFeedWarning(GtfsLogicalDate lastDate,
			GtfsLogicalDate cutoffDate) {
		this.lastDate = lastDate;
		this.cutoffDate = cutoffDate;
	}

	public GtfsLogicalDate getLastDate() {
		return lastDate;
	}

	public GtfsLogicalDate getCutoffDate() {
		return cutoffDate;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Feed ending too soon or expired. Last date with service is {0} < {1}",
				fmt.date(lastDate), fmt.date(cutoffDate));
	}
}
