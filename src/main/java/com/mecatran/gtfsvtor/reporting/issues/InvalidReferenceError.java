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
public class InvalidReferenceError implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private String fieldName;
	private String value; // Should we use a GtfsId<?,?> ?
	private String refTableName;
	private String refFieldName;

	public InvalidReferenceError(DataObjectSourceRef sourceRef,
			String fieldName, String value, String refTableName,
			String refFieldName) {
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldName);
		this.fieldName = fieldName;
		this.value = value;
		this.refTableName = refTableName;
		this.refFieldName = refFieldName;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + fieldName + " reference";
	}

	public String getValue() {
		return value;
	}

	public String getRefTableName() {
		return refTableName;
	}

	public String getRefFieldName() {
		return refFieldName;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Invalid ID {0}, it should reference an existing {1} in table {2}",
				fmt.id(value), fmt.pre(refFieldName), fmt.pre(refTableName));
	}
}
