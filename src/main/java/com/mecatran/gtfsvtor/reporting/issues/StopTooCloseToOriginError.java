package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Stop too close to origin")
public class StopTooCloseToOriginError implements ReportIssue {

	private GtfsStop stop;
	private List<SourceInfoWithFields> sourceInfos;

	public StopTooCloseToOriginError(GtfsStop stop) {
		this.stop = stop;
		this.sourceInfos = Arrays.asList(new SourceInfoWithFields(
				stop.getSourceInfo(), "stop_lat", "stop_lon"));
	}

	public GtfsStop getStop() {
		return stop;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		// Note: do not use stop.getCoordinates, if 0,0 it will return null
		fmt.text("Stop too close to origin (0,0): {0}", fmt
				.coordinates(new GeoCoordinates(stop.getLat(), stop.getLon())));
	}
}
