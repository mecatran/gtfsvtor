package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Missing object ID")
public class MissingObjectIdError implements ReportIssue {

	private SourceRefWithFields sourceRef;

	public MissingObjectIdError(DataObjectSourceRef sourceRef,
			String... fieldNames) {
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldNames);
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Missing mandatory object ID");
	}
}
