package com.mecatran.gtfsvtor.reporting.issues;

import java.nio.charset.Charset;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class InvalidCharsetError implements ReportIssue {

	private String tableName;
	private Charset charset;

	public InvalidCharsetError(String tableName, Charset charset) {
		this.tableName = tableName;
		this.charset = charset;
	}

	public String getTableName() {
		return tableName;
	}

	public Charset getCharset() {
		return charset;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Invalid charset {0} detected (using BOM) for table {1}, should be UTF-8",
				fmt.pre(charset), fmt.pre(tableName));
	}
}
