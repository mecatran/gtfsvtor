package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

public class RouteColorContrastIssue implements ReportIssue {

	private GtfsRoute route;
	private double brightnessDelta;
	private ReportIssueSeverity severity;
	private List<SourceInfoWithFields> sourceInfos;

	public RouteColorContrastIssue(GtfsRoute route, double brightnessDelta,
			ReportIssueSeverity severity) {
		this.route = route;
		this.brightnessDelta = brightnessDelta;
		this.severity = severity;
		this.sourceInfos = Arrays.asList(new SourceInfoWithFields(
				route.getSourceInfo(), "route_color", "route_text_color"));
	}

	public GtfsRoute getRoute() {
		return route;
	}

	public double getBrightnessDelta() {
		return brightnessDelta;
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
		// TODO Format float by the formatter itself
		fmt.text("Route colors not contrasted enough: {0}, delta is {1}%",
				fmt.colors(route.getNonNullColor(),
						route.getNonNullTextColor()),
				fmt.var(String.format("%.1f", brightnessDelta * 100)));
	}
}
