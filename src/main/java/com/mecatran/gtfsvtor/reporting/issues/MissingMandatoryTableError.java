package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class MissingMandatoryTableError implements ReportIssue {

	private String tableName;

	public MissingMandatoryTableError(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public String getCategoryName() {
		return "Missing mandatory table " + tableName;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Missing mandatory table {0}", fmt.pre(tableName));
	}
}
