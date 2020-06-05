package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class DuplicatedObjectIdError implements ReportIssue {

	private GtfsId<?, ?> duplicatedId;
	private List<SourceRefWithFields> sourceRefs;
	private List<String> fieldNames;

	public DuplicatedObjectIdError(DataObjectSourceRef duplicatedSourceRef,
			GtfsId<?, ?> duplicatedId, String... fieldNames) {
		this.duplicatedId = duplicatedId;
		this.sourceRefs = Arrays.asList(
				new SourceRefWithFields(duplicatedSourceRef, fieldNames));
		this.fieldNames = Arrays.asList(fieldNames);
	}

	public DuplicatedObjectIdError(DataObjectSourceRef existingSourceRef,
			DataObjectSourceRef duplicatedSourceRef, GtfsId<?, ?> duplicatedId,
			String... fieldNames) {
		this.duplicatedId = duplicatedId;
		/*
		 * Note: no need to sort source refs: existing is first and duplicated
		 * second, since we are called scanning the tables in line order.
		 */
		this.sourceRefs = Arrays.asList(
				new SourceRefWithFields(existingSourceRef, fieldNames),
				new SourceRefWithFields(duplicatedSourceRef, fieldNames));
		this.fieldNames = Arrays.asList(fieldNames);
	}

	public GtfsId<?, ?> getDuplicatedId() {
		return duplicatedId;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceRefs;
	}

	@Override
	public String getCategoryName() {
		return "Duplicated ID " + String.join(", ", fieldNames);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Duplicated object ID {0}", fmt.id(duplicatedId));
	}
}
