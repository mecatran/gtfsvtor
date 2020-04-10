package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "No service")
public class NoServiceError implements ReportIssue {

	public NoServiceError() {
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("No service on any day.");
	}
}
