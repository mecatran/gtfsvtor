package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(categoryName = "Stop too far from shape")
public class StopTooFarFromShapeIssue implements ReportIssue {

	private GtfsStop stop;
	private GtfsTripStopSequence stopSequence;
	private GtfsShape.Id shapeId;
	private GtfsTrip.Id examplarTripId;
	private double distanceMeters;
	private double arcLengthMeters;
	private GeoCoordinates projectedPoint;
	private ReportIssueSeverity severity;
	private SourceInfoWithFields sourceInfo;

	public StopTooFarFromShapeIssue(GtfsStop stop,
			GtfsTripStopSequence stopSequence, GtfsShape.Id shapeId,
			GtfsTrip.Id examplarTripId, double distanceMeters,
			double linearDistanceMeters, GeoCoordinates projectedPoint,
			ReportIssueSeverity severity) {
		this.stop = stop;
		this.stopSequence = stopSequence;
		this.shapeId = shapeId;
		this.examplarTripId = examplarTripId;
		this.distanceMeters = distanceMeters;
		this.arcLengthMeters = linearDistanceMeters;
		this.projectedPoint = projectedPoint;
		this.severity = severity;
		this.sourceInfo = new SourceInfoWithFields(stop.getSourceInfo(),
				"stop_lat", "stop_lon");
	}

	public GtfsStop getStop() {
		return stop;
	}

	public GtfsTripStopSequence getStopSequence() {
		return stopSequence;
	}

	public GtfsShape.Id getShapeId() {
		return shapeId;
	}

	public GtfsTrip.Id getExamplarTripId() {
		return examplarTripId;
	}

	public double getDistanceMeters() {
		return distanceMeters;
	}

	public double getArcLengthMeters() {
		return arcLengthMeters;
	}

	public GeoCoordinates getProjectedPoint() {
		return projectedPoint;
	}

	// This issue can have several severity depending on the distance
	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public void format(IssueFormatter fmt) {
		// TODO Format distance by the formatter itself
		fmt.text(
				"Stop at sequence {0} on examplar trip {1} is too far from computed projection point {2} on shape {3} (at arc-length {4}m): {5}m away",
				fmt.var(stopSequence.toString()), fmt.id(examplarTripId),
				fmt.var(fmt.coordinates(projectedPoint)), fmt.id(shapeId),
				fmt.var(String.format("%.2f", arcLengthMeters)),
				fmt.var(String.format("%.2f", distanceMeters)));
	}
}
