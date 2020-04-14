package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class DuplicatedObjectIdError implements ReportIssue {

	private GtfsId<?, ?> duplicatedId;
	private List<SourceInfoWithFields> sourceInfos;
	private List<String> fieldNames;

	public DuplicatedObjectIdError(DataObjectSourceInfo duplicatedSourceInfo,
			GtfsId<?, ?> duplicatedId, String... fieldNames) {
		this.duplicatedId = duplicatedId;
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(duplicatedSourceInfo, fieldNames));
		this.fieldNames = Arrays.asList(fieldNames);
	}

	public DuplicatedObjectIdError(DataObjectSourceInfo existingSourceInfo,
			DataObjectSourceInfo duplicatedSourceInfo,
			GtfsId<?, ?> duplicatedId, String... fieldNames) {
		this.duplicatedId = duplicatedId;
		/*
		 * Note: no need to sort source info: existing is first and duplicated
		 * second, since we are called scanning the tables in line order.
		 */
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(existingSourceInfo, fieldNames),
				new SourceInfoWithFields(duplicatedSourceInfo, fieldNames));
		this.fieldNames = Arrays.asList(fieldNames);
	}

	public GtfsId<?, ?> getDuplicatedId() {
		return duplicatedId;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
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
