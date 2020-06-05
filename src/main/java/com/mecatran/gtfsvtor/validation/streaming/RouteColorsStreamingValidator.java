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
	@ConfigurableOption(description = "Color brightness perceived contrast threshold, in %, below which a warning is generated")
	public double minBrightnessDeltaPercentWarning = 28.46;

	// Errors are disabled by default
	@ConfigurableOption(description = "Color brightness perceived contrast threshold, in %, below which an error is generated")
	public double minBrightnessDeltaPercentError = 0.0;

	@Override
	public void validate(Class<? extends GtfsRoute> clazz, GtfsRoute route,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();

		double colorBrightness = route.getNonNullColor().getBrightness();
		double textBrightness = route.getNonNullTextColor().getBrightness();
		double brightnessDeltaPercent = Math
				.abs(colorBrightness - textBrightness) * 100.;

		if (brightnessDeltaPercent < minBrightnessDeltaPercentWarning
				|| brightnessDeltaPercent < minBrightnessDeltaPercentError) {
			reportSink.report(new RouteColorContrastIssue(route,
					brightnessDeltaPercent,
					brightnessDeltaPercent < minBrightnessDeltaPercentError
							? ReportIssueSeverity.ERROR
							: ReportIssueSeverity.WARNING),
					context.getSourceInfo());
		}
	}
}
