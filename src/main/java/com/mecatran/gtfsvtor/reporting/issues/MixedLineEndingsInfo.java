package com.mecatran.gtfsvtor.reporting.issues;

import java.util.List;

import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.INFO, categoryName = "Mixed line endings")
public class MixedLineEndingsInfo implements ReportIssue {

	private final String tableName;
	private final long lfCount;
	private final List<Long> lfLines;
	private final long crlfCount;
	private final List<Long> crlfLines;

	public MixedLineEndingsInfo(String tableName, long lfCount, List<Long> lfLines,
			long crlfCount, List<Long> crlfLines) {
		this.tableName = tableName;
		this.lfCount = lfCount;
		this.lfLines = List.copyOf(lfLines);
		this.crlfCount = crlfCount;
		this.crlfLines = List.copyOf(crlfLines);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("{0} mixes LF and CRLF line endings. LF count: {1} (first lines: {2}). CRLF count: {3} (first lines: {4}).",
				fmt.pre(tableName), lfCount, fmt.pre(lfLines), crlfCount,
				fmt.pre(crlfLines));
	}
}
