package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class DuplicatedColumnError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String columnName;

	public DuplicatedColumnError(DataObjectSourceInfo sourceInfo,
			String columnName) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, columnName);
		this.columnName = columnName;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public String getCategoryName() {
		return "Duplicated column " + columnName;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Duplicated column");
	}
}
