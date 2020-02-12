package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.RouteColorContrastIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsRoute.class)
public class RouteColorsStreamingValidator
		implements StreamingValidator<GtfsRoute> {

	// Default value taken from legacy FeedValidator, rounded a bit
	@ConfigurableOption
	public double minWarningBrightnessDelta = 0.2846;

	// Errors are disabled by default
	@ConfigurableOption
	public double minErrorBrightnessDelta = -1.0;

	@Override
	public void validate(GtfsRoute route, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();

		double colorBrightness = route.getNonNullColor().getBrightness();
		double textBrightness = route.getNonNullTextColor().getBrightness();
		double brightnessDelta = Math.abs(colorBrightness - textBrightness);

		if (brightnessDelta < minWarningBrightnessDelta
				|| brightnessDelta < minErrorBrightnessDelta) {
			reportSink
					.report(new RouteColorContrastIssue(route, brightnessDelta,
							brightnessDelta <= minErrorBrightnessDelta
									? ReportIssueSeverity.ERROR
									: ReportIssueSeverity.WARNING));
		}
	}
}
