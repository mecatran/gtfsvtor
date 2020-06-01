package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.geospatial.GeoBounds;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.reporting.*;

import java.util.Arrays;
import java.util.List;

@ReportIssuePolicy
public class InvalidCoordinateError implements ReportIssue {

	private SourceInfoWithFields sourceInfo;
	private String latFieldName;
	private String lonFieldName;
	private GeoCoordinates value;
	private GeoBounds expectedBounds;
	private ReportIssueSeverity severity = ReportIssueSeverity.ERROR;

	public InvalidCoordinateError(DataObjectSourceInfo sourceInfo,
			String latFieldName, String lonFieldName, GeoCoordinates value, GeoBounds expectedBounds) {
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, latFieldName, lonFieldName);
		this.latFieldName = latFieldName;
		this.lonFieldName = lonFieldName;
		this.value = value;
		this.expectedBounds = expectedBounds;
	}

	public InvalidCoordinateError withSeverity(ReportIssueSeverity severity) {
		this.severity = severity;
		return this;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + latFieldName + "/"+ lonFieldName+" coordinate";
	}

	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	public GeoCoordinates getValue() {
		return value;
	}

	public GeoBounds getExpectedBounds() {
		return expectedBounds;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Invalid coordinate value {0}, should be inside: {1}", fmt.coordinates(value),
				fmt.bounds(expectedBounds));
	}
}
