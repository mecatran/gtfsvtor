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

@ReportIssuePolicy(categoryName = "Stop too far from parent station")
public class StopTooFarFromParentStationIssue implements ReportIssue {

	private GtfsStop stop, station;
	private double distanceMeters;
	private ReportIssueSeverity severity;
	private List<SourceInfoWithFields> sourceInfos;

	public StopTooFarFromParentStationIssue(GtfsStop stop, GtfsStop station,
			double distanceMeters, ReportIssueSeverity severity) {
		this.stop = stop;
		this.station = station;
		this.distanceMeters = distanceMeters;
		this.severity = severity;
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(stop.getSourceInfo(), "stop_lat",
						"stop_lon"),
				new SourceInfoWithFields(station.getSourceInfo(), "stop_lat",
						"stop_lon"));
		Collections.sort(this.sourceInfos);
	}

	public GtfsStop getStop() {
		return stop;
	}

	public GtfsStop getStation() {
		return station;
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
		fmt.text("Stop too far from parent station, they are {0}m away",
				fmt.var(String.format("%.2f", distanceMeters)));
	}
}
