package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.INFO, categoryName = "Unknown file")
public class UnknownFileInfo implements ReportIssue {

	private String fileName;

	public UnknownFileInfo(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Unknown file {0}", fmt.pre(fileName));
	}

}
