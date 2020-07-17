package com.mecatran.gtfsvtor.validation.streaming.ext;

import java.util.regex.Pattern;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsStop.class)
@DefaultDisabledValidator
public class IFOPTStopIDStreamingValidator
		implements StreamingValidator<GtfsStop> {

	@ConfigurableOption(description = "Pattern the stop_id must conform to for stops of stop_type station")
	public Pattern stationTypeStopIdPattern = Pattern
			.compile("[a-z]{2}:\\d+:\\d+");

	@ConfigurableOption(description = "Pattern the stop_id must conform to for stops of stop_type stop")
	public Pattern stopTypeStopIdPattern = Pattern
			.compile("[a-z]{2}:\\d+:\\d+:\\d*:[A-Za-z0-9]+(:[A-Za-z0-9]+)?");

	@ConfigurableOption(description = "Pattern the stop_id must conform to for stops of stop_type entrance")
	public Pattern entranceTypeStopIdPattern = Pattern
			.compile("[a-z]{2}:\\d+:\\d+:\\d*:[A-Za-z0-9]+(:[A-Za-z0-9]+)?");

	@Override
	public void validate(Class<? extends GtfsStop> clazz, GtfsStop stop,
			Context context) {
		ReportSink reportSink = context.getReportSink();
		GtfsStopType stopType = stop.getType();
		String stopId = stop.getId().getInternalId();
		switch (stopType) {
		case STOP:
			if (!stopTypeStopIdPattern.matcher(stopId).matches()) {
				reportSink.report(
						new InvalidFieldFormatError(context.getSourceRef(),
								"stop_id", stopId,
								stopTypeStopIdPattern.toString()).withSeverity(
										ReportIssueSeverity.WARNING),
						context.getSourceInfo());
			}
			break;
		case STATION:
			if (!stationTypeStopIdPattern.matcher(stopId).matches()) {
				reportSink.report(
						new InvalidFieldFormatError(context.getSourceRef(),
								"stop_id", stopId,
								stationTypeStopIdPattern.toString())
										.withSeverity(
												ReportIssueSeverity.WARNING),
						context.getSourceInfo());
			}
			break;
		case ENTRANCE:
			if (!entranceTypeStopIdPattern.matcher(stopId).matches()) {
				reportSink.report(
						new InvalidFieldFormatError(context.getSourceRef(),
								"stop_id", stopId,
								entranceTypeStopIdPattern.toString())
										.withSeverity(
												ReportIssueSeverity.WARNING),
						context.getSourceInfo());
			}
			break;
		default:
			// Do not check
			break;
		}
	}
}
