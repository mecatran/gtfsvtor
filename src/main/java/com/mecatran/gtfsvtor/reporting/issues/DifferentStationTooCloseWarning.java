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

@ReportIssuePolicy(categoryName = "Different station too close", severity = ReportIssueSeverity.WARNING)
public class DifferentStationTooCloseWarning implements ReportIssue {

	private GtfsStop stop, station, otherStation;
	private double distanceMeters;
	private List<SourceRefWithFields> sourceInfos;

	public DifferentStationTooCloseWarning(GtfsStop stop, GtfsStop station,
			GtfsStop otherStation, double distanceMeters) {
		this.stop = stop;
		this.station = station;
		this.otherStation = otherStation;
		this.distanceMeters = distanceMeters;
		this.sourceInfos = Arrays.asList(
				new SourceRefWithFields(stop.getSourceRef(), "stop_lat",
						"stop_lon"),
				new SourceRefWithFields(station.getSourceRef(), "stop_lat",
						"stop_lon"),
				new SourceRefWithFields(otherStation.getSourceRef(), "stop_lat",
						"stop_lon"));
		Collections.sort(this.sourceInfos);
	}

	public GtfsStop getStop() {
		return stop;
	}

	public GtfsStop getStation() {
		return station;
	}

	public GtfsStop getOtherStation() {
		return otherStation;
	}

	public double getDistanceMeters() {
		return distanceMeters;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Station {0} is not parent station of {1}, but they are only {2} away",
				fmt.id(otherStation.getId()), fmt.id(stop.getId()),
				fmt.var(fmt.distance(distanceMeters)));
	}
}
