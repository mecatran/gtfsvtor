package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Inconsistent number of fields")
public class InconsistentNumberOfFieldsWarning implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private int numberOfFields;
	private int numberOfHeaderColumns;

	public InconsistentNumberOfFieldsWarning(DataObjectSourceInfo sourceInfo,
			int numberOfFields, int numberOfHeaderColumns) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo);
		this.numberOfFields = numberOfFields;
		this.numberOfHeaderColumns = numberOfHeaderColumns;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	public int getNumberOfFields() {
		return numberOfFields;
	}

	public int getNumberOfHeaderColumns() {
		return numberOfHeaderColumns;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Found {0} fields, expected {1} as defined in the header.",
				fmt.var(Integer.toString(numberOfFields)),
				fmt.var(Integer.toString(numberOfHeaderColumns)));
	}
}
