package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Inconsistent number of fields")
public class InconsistentNumberOfFieldsWarning implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private int numberOfFields;
	private int numberOfHeaderColumns;

	public InconsistentNumberOfFieldsWarning(DataObjectSourceRef sourceRef,
			int numberOfFields, int numberOfHeaderColumns) {
		this.sourceRef = new SourceRefWithFields(sourceRef);
		this.numberOfFields = numberOfFields;
		this.numberOfHeaderColumns = numberOfHeaderColumns;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
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
