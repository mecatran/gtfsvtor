package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Time travel at stop")
public class TimeTravelAtStopError implements ReportIssue {

	private GtfsStopTime stopTime;
	private SourceRefWithFields sourceRef;

	public TimeTravelAtStopError(GtfsStopTime stopTime,
			DataObjectSourceRef sourceRef) {
		this.stopTime = stopTime;
		this.sourceRef = new SourceRefWithFields(sourceRef, "arrival_time",
				"departure_time");
	}

	public GtfsStopTime getStopTime() {
		return stopTime;
	}

	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Departure time cannot be before arrival time");
	}
}
