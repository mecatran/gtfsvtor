package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy
public class InvalidFieldFormatError implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private String fieldName;
	private String value;
	private String expectedFormat;
	private String additionalInfo;
	private ReportIssueSeverity severity = ReportIssueSeverity.ERROR;

	public InvalidFieldFormatError(DataObjectSourceRef sourceRef,
			String fieldName, String value, String expectedFormat) {
		this(sourceRef, fieldName, value, expectedFormat, null);
	}

	public InvalidFieldFormatError(DataObjectSourceRef sourceRef,
			String fieldName, String value, String expectedFormat,
			String additionalInfo) {
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldName);
		this.fieldName = fieldName;
		this.value = value;
		this.expectedFormat = expectedFormat;
		this.additionalInfo = additionalInfo;
	}

	public InvalidFieldFormatError withSeverity(ReportIssueSeverity severity) {
		this.severity = severity;
		return this;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + fieldName + " format";
	}

	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	public String getValue() {
		return value;
	}

	public String getExpectedFormat() {
		return expectedFormat;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Invalid format for value {0}, expected: {1}{2}",
				fmt.var(value), expectedFormat,
				additionalInfo == null ? "" : " - " + additionalInfo);
	}
}
