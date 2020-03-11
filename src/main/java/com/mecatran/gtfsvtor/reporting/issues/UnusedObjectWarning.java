package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING)
public class UnusedObjectWarning implements ReportIssue {

	private String typeName;
	private GtfsId<?, ?> id;
	private List<SourceInfoWithFields> sourceInfos;

	public UnusedObjectWarning(String typeName, GtfsId<?, ?> id,
			DataObjectSourceInfo sourceInfo, String fieldName) {
		this.typeName = typeName;
		this.id = id;
		if (sourceInfo != null) {
			this.sourceInfos = Arrays
					.asList(new SourceInfoWithFields(sourceInfo, fieldName));
		} else {
			this.sourceInfos = Collections.emptyList();
		}
	}

	public GtfsId<?, ?> getId() {
		return id;
	}

	@Override
	public String getCategoryName() {
		return "Unused " + typeName;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Unused {0} ID {1}", fmt.var(typeName), fmt.id(id));
	}
}
