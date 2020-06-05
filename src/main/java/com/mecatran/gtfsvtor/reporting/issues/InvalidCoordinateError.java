package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.geospatial.GeoBounds;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy
public class InvalidCoordinateError implements ReportIssue {

	private SourceRefWithFields sourceRef;
	private String latFieldName;
	private String lonFieldName;
	private GeoCoordinates value;
	private GeoBounds expectedBounds;
	private ReportIssueSeverity severity = ReportIssueSeverity.ERROR;

	public InvalidCoordinateError(DataObjectSourceRef sourceRef,
			String latFieldName, String lonFieldName, GeoCoordinates value,
			GeoBounds expectedBounds) {
		this.sourceRef = new SourceRefWithFields(sourceRef, latFieldName,
				lonFieldName);
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
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public String getCategoryName() {
		return "Invalid " + latFieldName + "/" + lonFieldName + " coordinate";
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
		fmt.text("Invalid coordinate value {0}, should be inside: {1}",
				fmt.coordinates(value), fmt.bounds(expectedBounds));
	}
}
