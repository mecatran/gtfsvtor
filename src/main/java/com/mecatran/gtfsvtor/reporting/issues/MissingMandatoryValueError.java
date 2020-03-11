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
public class MissingMandatoryValueError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String fieldName;

	public MissingMandatoryValueError(DataObjectSourceInfo sourceInfo,
			String fieldName) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldName);
		this.fieldName = fieldName;
	}

	// TODO Remove this constructor
	public MissingMandatoryValueError(DataObjectSourceInfo sourceInfo,
			String... fieldNames) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldNames);
		this.fieldName = String.join("/", fieldNames);
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
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
