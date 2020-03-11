package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.INFO, categoryName = "Statistics")
public class StatisticsInfo implements ReportIssue {

	private String message;

	public StatisticsInfo(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("{0}", message);
	}

}
