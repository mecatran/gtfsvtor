package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Similar colors")
public class SimilarRouteColorWarning implements ReportIssue {

	private GtfsRoute route1, route2;
	private double colorDistance;
	private boolean colorOrText;
	private List<SourceInfoWithFields> sourceInfos;

	public SimilarRouteColorWarning(GtfsRoute route1, GtfsRoute route2,
			double colorDistance, boolean colorOrText) {
		this.route1 = route1;
		this.route2 = route2;
		this.colorDistance = colorDistance;
		this.colorOrText = colorOrText;
		this.sourceInfos = Arrays.asList(
				new SourceInfoWithFields(route1.getSourceInfo(),
						colorOrText ? "route_color" : "route_text_color"),
				new SourceInfoWithFields(route2.getSourceInfo(),
						colorOrText ? "route_color" : "route_text_color"));
		Collections.sort(this.sourceInfos);
	}

	public double getColorDistance() {
		return colorDistance;
	}

	/**
	 * @return True if color, false if text.
	 */
	public boolean getColorOrText() {
		return colorOrText;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		// TODO Format float by the formatter itself
		fmt.text(
				"Route {0} colors are very similar but not identical: {1} vs {2}, distance is {3}%",
				colorOrText ? "background" : "text",
				fmt.colors(route1.getNonNullColor(),
						route1.getNonNullTextColor()),
				fmt.colors(route2.getNonNullColor(),
						route2.getNonNullTextColor()),
				fmt.var(String.format("%.3f", colorDistance * 100)));
	}
}
