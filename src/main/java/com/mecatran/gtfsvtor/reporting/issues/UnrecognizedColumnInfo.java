package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.INFO)
public class UnrecognizedColumnInfo implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private String columnName;

	public UnrecognizedColumnInfo(DataObjectSourceRef sourceRef,
			String columnName) {
		this.sourceRef = new SourceRefWithFields(sourceRef, columnName);
		this.columnName = columnName;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public String getCategoryName() {
		return "Unrecognized column " + columnName;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Unrecognized column");
	}
}
