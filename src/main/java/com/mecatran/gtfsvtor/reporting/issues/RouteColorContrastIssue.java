package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(categoryName = "Color contrast")
public class RouteColorContrastIssue implements ReportIssue {

	private GtfsRoute route;
	private double brightnessDeltaPercent;
	private ReportIssueSeverity severity;
	private List<SourceRefWithFields> sourceInfos;

	public RouteColorContrastIssue(GtfsRoute route,
			double brightnessDeltaPercent, ReportIssueSeverity severity) {
		this.route = route;
		this.brightnessDeltaPercent = brightnessDeltaPercent;
		this.severity = severity;
		this.sourceInfos = Arrays.asList(new SourceRefWithFields(
				route.getSourceRef(), "route_color", "route_text_color"));
	}

	public GtfsRoute getRoute() {
		return route;
	}

	public double getBrightnessDeltaPercent() {
		return brightnessDeltaPercent;
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
		fmt.text("Route colors not contrasted enough: {0}, delta is {1}%",
				fmt.colors(route.getNonNullColor(),
						route.getNonNullTextColor()),
				fmt.var(String.format("%.1f", brightnessDeltaPercent)));
	}
}
