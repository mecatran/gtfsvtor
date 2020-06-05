package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Wrong time stop type")
public class WrongStopTimeStopTypeError implements ReportIssue {

	private GtfsStopTime stopTime;
	private GtfsStop stop;
	private SourceRefWithFields sourceRef;

	public WrongStopTimeStopTypeError(DataObjectSourceRef sourceRef,
			GtfsStopTime stopTime, GtfsStop stop) {
		this.stopTime = stopTime;
		this.stop = stop;
		this.sourceRef = new SourceRefWithFields(sourceRef, "stop_id");
	}

	public GtfsStopTime getStopTime() {
		return stopTime;
	}

	public GtfsStop getStop() {
		return stop;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Wrong type {0} for stop {1} {2}, should be type {3}",
				fmt.pre(stop.getType().toString()), fmt.id(stop.getId()),
				fmt.var(stop.getName()), fmt.pre(GtfsStopType.STOP));
	}
}
