package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING)
public class UselessValueWarning implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private String fieldName;
	private String value;
	private String additionalInfo;

	public UselessValueWarning(DataObjectSourceRef sourceRef, String fieldName,
			String value, String additionalInfo) {
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldName);
		this.fieldName = fieldName;
		this.value = value;
		this.additionalInfo = additionalInfo;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public String getCategoryName() {
		return "Useless " + fieldName + " value";
	}

	public String getValue() {
		return value;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Useless value {0}: {1}", fmt.var(value), additionalInfo);
	}
}
