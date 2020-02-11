package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsObjectWithSourceInfo;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class DuplicatedObjectIdError implements ReportIssue {

	private GtfsObjectWithSourceInfo existingObject;
	private GtfsObjectWithSourceInfo duplicatedObject;
	private GtfsId<?, ?> duplicatedId;
	private List<SourceInfoWithFields> sourceInfos;

	public DuplicatedObjectIdError(GtfsObjectWithSourceInfo existingObject,
			GtfsObjectWithSourceInfo duplicatedObject,
			GtfsId<?, ?> duplicatedId, String fieldName) {
		this.existingObject = existingObject;
		this.duplicatedObject = duplicatedObject;
		this.duplicatedId = duplicatedId;
		/*
		 * Note: no need to sort source info: existing is first and duplicated
		 * second, since we are called scanning the tables in line order.
		 */
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(existingObject.getSourceInfo(),
						fieldName),
				new SourceInfoWithFields(duplicatedObject.getSourceInfo(),
						fieldName));
	}

	public GtfsId<?, ?> getDuplicatedId() {
		return duplicatedId;
	}

	public GtfsObjectWithSourceInfo getExistingObject() {
		return existingObject;
	}

	public GtfsObjectWithSourceInfo getDuplicatedObject() {
		return duplicatedObject;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Duplicated object ID {0}", fmt.id(duplicatedId));
	}
}
