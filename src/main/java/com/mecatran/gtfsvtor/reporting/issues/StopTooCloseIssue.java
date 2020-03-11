package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(categoryName = "Stops too close")
public class StopTooCloseIssue implements ReportIssue {

	private GtfsStop stop1, stop2;
	private double distanceMeters;
	private ReportIssueSeverity severity;
	private List<SourceInfoWithFields> sourceInfos;

	public StopTooCloseIssue(GtfsStop stop1, GtfsStop stop2,
			double distanceMeters, ReportIssueSeverity severity) {
		this.stop1 = stop1;
		this.stop2 = stop2;
		this.distanceMeters = distanceMeters;
		this.severity = severity;
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(stop1.getSourceInfo(), "stop_lat",
						"stop_lon"),
				new SourceInfoWithFields(stop2.getSourceInfo(), "stop_lat",
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
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		// TODO Format distance by the formatter itself
		fmt.text("Stops too close, they are {0}m away",
				fmt.var(String.format("%.2f", distanceMeters)));
	}
}
