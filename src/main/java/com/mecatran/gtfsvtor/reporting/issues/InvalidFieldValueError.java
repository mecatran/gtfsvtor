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
public class InvalidFieldValueError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String fieldName;
	private String value;
	private String errorMessage;

	public InvalidFieldValueError(DataObjectSourceInfo sourceInfo,
			String fieldName, String value, String errorMessage) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldName);
		this.fieldName = fieldName;
		this.value = value;
		this.errorMessage = errorMessage;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + fieldName + " value";
	}

	public String getValue() {
		return value;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Invalid value {0}: {1}", fmt.var(value), errorMessage);
	}
}
