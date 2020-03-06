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
public class MissingMandatoryColumnError implements ReportIssue {

	private String columnName;
	private SourceInfoWithFields sourceInfo;

	public MissingMandatoryColumnError(DataObjectSourceInfo sourceInfo,
			String columnName) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, columnName);
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Missing mandatory column: {0}", fmt.pre(columnName));
	}
}
