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
public class InvalidReferenceError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String value; // Should we use a GtfsId<?,?> ?
	private String refTableName;
	private String refFieldName;

	public InvalidReferenceError(DataObjectSourceInfo sourceInfo,
			String fieldName, String value, String refTableName,
			String refFieldName) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldName);
		this.value = value;
		this.refTableName = refTableName;
		this.refFieldName = refFieldName;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
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
				"Invalid id {0}, it should reference an existing {1} in table {2}",
				fmt.id(value), fmt.pre(refFieldName), fmt.pre(refTableName));
	}
}
