package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Invalid encoding")
public class InvalidEncodingError implements ReportIssue {

	private SourceRefWithFields sourceInfo;
	private String value;

	public InvalidEncodingError(DataObjectSourceRef sourceRef,
			String fieldName, String value) {
		this.sourceInfo = new SourceRefWithFields(sourceRef, fieldName);
		this.value = value;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceInfo);
	}

	public String getValue() {
		return value;
	}

	@Override
	public void format(IssueFormatter fmt) {
		// Replace null char to invalid encoding char to see it
		fmt.text("Invalid UTF-8 encoding (or NULL char) in value {0}",
				fmt.var(value.replace('\0', '\uFFFD')));
	}
}
