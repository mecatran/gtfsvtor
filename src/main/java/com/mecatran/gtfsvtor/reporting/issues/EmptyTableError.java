package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Empty table")
public class EmptyTableError implements ReportIssue {

	private String tableName;

	public EmptyTableError(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Empty table {0}", fmt.pre(tableName));
	}
}
