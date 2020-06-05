package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsPathway;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Wrong pathway stop type")
public class WrongPathwayStopTypeError implements ReportIssue {

	private GtfsPathway pathway;
	private GtfsStop stop;
	private String fieldname;
	private SourceRefWithFields sourceRef;

	public WrongPathwayStopTypeError(DataObjectSourceRef sourceRef,
			GtfsPathway pathway, GtfsStop stop, String fieldname) {
		this.pathway = pathway;
		this.stop = stop;
		this.fieldname = fieldname;
		this.sourceRef = new SourceRefWithFields(sourceRef, fieldname);
	}

	public GtfsPathway getPathway() {
		return pathway;
	}

	public GtfsStop getStop() {
		return stop;
	}

	public String getFieldname() {
		return fieldname;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Wrong type {0} for {1} {2} {3}, should be type {4}, {5}, {6} or {7}",
				fmt.pre(stop.getType().toString()), fmt.pre(fieldname),
				fmt.id(stop.getId()), fmt.var(stop.getName()),
				fmt.pre(GtfsStopType.STOP), fmt.pre(GtfsStopType.ENTRANCE),
				fmt.pre(GtfsStopType.NODE),
				fmt.pre(GtfsStopType.BOARDING_AREA));
	}
}
