package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy
public class InvalidFieldValueIssue implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String[] fieldNames;
	private String value;
	private String errorMessage;
	private ReportIssueSeverity severity = ReportIssueSeverity.ERROR;

	public InvalidFieldValueIssue(DataObjectSourceInfo sourceInfo, String value,
			String errorMessage, String... fieldNames) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldNames);
		this.fieldNames = fieldNames;
		this.value = value;
		this.errorMessage = errorMessage;
	}

	public InvalidFieldValueIssue withSeverity(ReportIssueSeverity severity) {
		this.severity = severity;
		return this;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + String.join("/", fieldNames) + " value";
	}

	public String getValue() {
		return value;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Invalid value {0}: {1}", fmt.var(value), errorMessage);
	}

	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

}
