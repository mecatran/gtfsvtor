package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Time travel at stop")
public class TimeTravelAtStopError implements ReportIssue {

	private GtfsStopTime stopTime;
	private SourceInfoWithFields sourceInfo;

	public TimeTravelAtStopError(GtfsStopTime stopTime,
			DataObjectSourceInfo sourceInfo) {
		this.stopTime = stopTime;
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, "arrival_time",
				"departure_time");
	}

	public GtfsStopTime getStopTime() {
		return stopTime;
	}

	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Departure time cannot be before arrival time");
	}
}
