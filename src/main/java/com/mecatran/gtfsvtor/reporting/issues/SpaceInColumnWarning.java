package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Extra space in column header")
public class SpaceInColumnWarning implements ReportIssue {

	private String columnName;
	private String rawColumnName;
	private SourceRefWithFields sourceInfo;

	public SpaceInColumnWarning(DataObjectSourceRef sourceRef,
			String columnName, String rawColumnName) {
		this.sourceInfo = new SourceRefWithFields(sourceRef, columnName);
		this.columnName = columnName;
		this.rawColumnName = rawColumnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getRawColumnName() {
		return rawColumnName;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Column header {0} should not contain any space: \"{1}\"",
				fmt.pre(columnName), fmt.pre(rawColumnName));
	}
}
