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
public class InvalidFieldFormatError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String value;
	private String expectedFormat;

	public InvalidFieldFormatError(DataObjectSourceInfo sourceInfo,
			String fieldName, String value, String expectedFormat) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldName);
		this.value = value;
		this.expectedFormat = expectedFormat;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	public String getValue() {
		return value;
	}

	public String getExpectedFormat() {
		return expectedFormat;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Invalid format for value {0}, expected: {1}", fmt.var(value),
				expectedFormat);
	}
}
