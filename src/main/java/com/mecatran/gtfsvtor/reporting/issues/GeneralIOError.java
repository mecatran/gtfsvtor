package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "General I/O error")
public class GeneralIOError implements ReportIssue {

	private String message;

	public GeneralIOError(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "IO Error: " + message;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("IO Error: {0}", fmt.var(message));
	}

}
