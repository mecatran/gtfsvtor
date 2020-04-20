package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Wrong time stop type")
public class WrongStopTimeStopTypeError implements ReportIssue {

	private GtfsStopTime stopTime;
	private GtfsStop stop;
	private SourceInfoWithFields sourceInfo;

	public WrongStopTimeStopTypeError(DataObjectSourceInfo sourceInfo,
			GtfsStopTime stopTime, GtfsStop stop) {
		this.stopTime = stopTime;
		this.stop = stop;
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, "stop_id");
	}

	public GtfsStopTime getStopTime() {
		return stopTime;
	}

	public GtfsStop getStop() {
		return stop;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Wrong type {0} for stop {1} {2}, should be type {3}",
				fmt.pre(stop.getType().toString()), fmt.id(stop.getId()),
				fmt.var(stop.getName()), fmt.pre(GtfsStopType.STOP));
	}
}
