package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkUrl;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsRoute.class)
public class RouteStreamingValidator implements StreamingValidator<GtfsRoute> {

	@ConfigurableOption(description = "Maximum route short name char length above which a warning is generated")
	public int maxShortNameLen = 6;

	@Override
	public void validate(Class<? extends GtfsRoute> clazz, GtfsRoute route,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();
		checkNonNull(route::getAgencyId, "agency_id", context);
		checkUrl(route::getUrl, "route_url", context);
		// Check route->agency reference
		if (route.getAgencyId() != null
				&& dao.getAgency(route.getAgencyId()) == null) {
			reportSink.report(new InvalidReferenceError(route.getSourceInfo(),
					"agency_id", route.getAgencyId().getInternalId(),
					GtfsAgency.TABLE_NAME, "agency_id"));
		}
		if (route.getShortName() == null && route.getLongName() == null) {
			// TODO specific report for this for better explanation
			reportSink.report(
					new MissingMandatoryValueError(context.getSourceInfo(),
							"route_short_name", "route_long_name"));
		}
		if (route.getShortName() != null
				&& route.getShortName().length() > maxShortNameLen) {
			// TODO specific report for this for better explanation
			reportSink
					.report(new InvalidFieldFormatError(context.getSourceInfo(),
							"route_short_name", route.getShortName(),
							"Max " + maxShortNameLen + " chars long")
									.withSeverity(ReportIssueSeverity.WARNING));
		}
		if (route.getShortName() != null && route.getLongName() != null
				&& route.getLongName().startsWith(route.getShortName())) {
			// TODO Are we sure about this test?
			// TODO specific report for this for better explanation
			reportSink.report(new InvalidFieldValueIssue(
					context.getSourceInfo(), route.getShortName(),
					"Long name should not start or be equals with short name",
					"route_short_name", "route_long_name")
							.withSeverity(ReportIssueSeverity.WARNING));
		}
		if (route.getLongName() != null && route.getDescription() != null
				&& route.getLongName()
						.equalsIgnoreCase(route.getDescription())) {
			reportSink
					.report(new InvalidFieldValueIssue(context.getSourceInfo(),
							route.getDescription(),
							"Description should not be the same as long name",
							"route_desc", "route_long_name")
									.withSeverity(ReportIssueSeverity.WARNING));
		}
	}
}
