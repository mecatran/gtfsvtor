package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class TableIOError implements ReportIssue {

	private String tableName;
	private String message;

	public TableIOError(String tableName, String message) {
		this.tableName = tableName;
		this.message = message;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("I/O Error reading table {0}: {1}", fmt.pre(tableName),
				fmt.var(message));
	}
}
