package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(categoryName = "Stops too close")
public class StopTooCloseIssue implements ReportIssue {

	private GtfsStop stop1, stop2;
	private double distanceMeters;
	private ReportIssueSeverity severity;
	private List<SourceRefWithFields> sourceInfos;

	public StopTooCloseIssue(GtfsStop stop1, GtfsStop stop2,
			double distanceMeters, ReportIssueSeverity severity) {
		this.stop1 = stop1;
		this.stop2 = stop2;
		this.distanceMeters = distanceMeters;
		this.severity = severity;
		this.sourceInfos = Arrays.asList(
				new SourceRefWithFields(stop1.getSourceRef(), "stop_lat",
						"stop_lon"),
				new SourceRefWithFields(stop2.getSourceRef(), "stop_lat",
						"stop_lon"));
		Collections.sort(this.sourceInfos);
	}

	public GtfsStop getStop1() {
		return stop1;
	}

	public GtfsStop getStop2() {
		return stop2;
	}

	public double getDistanceMeters() {
		return distanceMeters;
	}

	// This issue can have several severity depending on the distance
	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Stops too close, they are {0} away",
				fmt.var(fmt.distance(distanceMeters)));
	}
}
