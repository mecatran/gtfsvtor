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
public class MissingMandatoryColumnError implements ReportIssue {

	private String columnName;
	private SourceRefWithFields sourceInfo;

	public MissingMandatoryColumnError(DataObjectSourceRef sourceRef,
			String columnName) {
		this.sourceInfo = new SourceRefWithFields(sourceRef, columnName);
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public String getCategoryName() {
		return "Missing mandatory " + columnName + " column";
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Missing mandatory column: {0}", fmt.pre(columnName));
	}
}
