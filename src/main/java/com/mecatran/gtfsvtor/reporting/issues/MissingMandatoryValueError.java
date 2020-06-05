package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class MissingMandatoryValueError implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private String fieldName;

	public MissingMandatoryValueError(DataObjectSourceRef sourceRef,
			String fieldName) {
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldName);
		this.fieldName = fieldName;
	}

	// TODO Remove this constructor
	public MissingMandatoryValueError(DataObjectSourceRef sourceRef,
			String... fieldNames) {
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldNames);
		this.fieldName = String.join("/", fieldNames);
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public String getCategoryName() {
		return "Missing mandatory " + fieldName + " value";
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Missing mandatory value");
	}
}
