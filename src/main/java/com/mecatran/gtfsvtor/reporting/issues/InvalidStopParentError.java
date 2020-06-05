package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class InvalidStopParentError implements ReportIssue {

	private GtfsStop child;
	private GtfsStop parent;
	private GtfsStopType expectedParentType;
	private List<SourceRefWithFields> sourceInfos;

	public InvalidStopParentError(GtfsStop child, GtfsStop parent,
			GtfsStopType expectedParentType) {
		this.child = child;
		this.parent = parent;
		this.expectedParentType = expectedParentType;
		this.sourceInfos = Arrays.asList(
				new SourceRefWithFields(child.getSourceRef(), "location_type",
						"parent_station"),
				new SourceRefWithFields(parent.getSourceRef(),
						"location_type"));
		Collections.sort(this.sourceInfos);
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + expectedParentType.toString().toLowerCase()
				+ " parent type";
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Invalid type {0} for parent ID {1}, a child of type {2} should {3}",
				fmt.pre(parent.getType()), fmt.id(parent.getId()),
				fmt.pre(child.getType()),
				(expectedParentType == null ? "not have any parent"
						: "have a parent type " + fmt.pre(expectedParentType)));
	}
}
