package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Missing object ID")
public class MissingObjectIdError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;

	public MissingObjectIdError(DataObjectSourceInfo sourceInfo,
			String... fieldNames) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, fieldNames);
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Missing mandatory object ID");
	}
}
