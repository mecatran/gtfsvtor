package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkEmail;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkPhone;
import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsAttribution;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsAttribution.class)
public class AttributionStreamingValidator
		implements StreamingValidator<GtfsAttribution> {

	@ConfigurableOption(description = "Maximum route short name char length above which a warning is generated")
	public int maxShortNameLen = 6;

	@Override
	public void validate(Class<? extends GtfsAttribution> clazz,
			GtfsAttribution attribution, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();

		// Check attribution->agency reference
		if (attribution.getAgencyId().isPresent()
				&& dao.getAgency(attribution.getAgencyId().get()) == null) {
			reportSink.report(
					new InvalidReferenceError(context.getSourceRef(),
							"agency_id",
							attribution.getAgencyId().get().getInternalId(),
							GtfsAgency.TABLE_NAME, "agency_id"),
					context.getSourceInfo());
		}
		// Check attribution->route reference
		if (attribution.getRouteId().isPresent()
				&& dao.getRoute(attribution.getRouteId().get()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceRef(),
					"route_id", attribution.getRouteId().get().getInternalId(),
					GtfsRoute.TABLE_NAME, "route_id"), context.getSourceInfo());
		}
		// Check attribution->trip reference
		if (attribution.getTripId().isPresent()
				&& dao.getTrip(attribution.getTripId().get()) == null) {
			reportSink.report(
					new InvalidReferenceError(context.getSourceRef(), "trip_id",
							attribution.getTripId().get().getInternalId(),
							GtfsTrip.TABLE_NAME, "trip_id"),
					context.getSourceInfo());
		}

		// Check that one ID is set only

		// Check attribution attributions
		checkNonNull(attribution::getOrganizationName, "organization_name",
				context);
		if (attribution.getAttributionUrl().isPresent())
			checkUrl(() -> attribution.getAttributionUrl().get(),
					"attribution_url", context);
		if (attribution.getAttributionEmail().isPresent())
			checkEmail(() -> attribution.getAttributionEmail().get(),
					"attribution_email", context);
		if (attribution.getAttributionPhone().isPresent())
			checkPhone(() -> attribution.getAttributionPhone().get(),
					"attribution_phone", context);

		// Check that only one [agency/route/trip]_id is defined
		List<String> idFieldsSet = new ArrayList<>();
		if (attribution.getAgencyId().isPresent())
			idFieldsSet.add("agency_id");
		if (attribution.getRouteId().isPresent())
			idFieldsSet.add("route_id");
		if (attribution.getTripId().isPresent())
			idFieldsSet.add("trip_id");
		if (idFieldsSet.size() >= 2) {
			reportSink.report(
					new InvalidFieldValueIssue(context.getSourceRef(),
							idFieldsSet.stream()
									.collect(Collectors.joining(", ")),
							"Only one ID should be set",
							idFieldsSet.toArray(new String[0]))
									.withSeverity(ReportIssueSeverity.ERROR),
					context.getSourceInfo());
		}

		// Check that at least one is_[producer/operator/authority] is set
		if (!attribution.getNonNullIsProducer()
				&& !attribution.getNonNullIsOperator()
				&& !attribution.getNonNullIsAuthority()) {
			reportSink.report(
					new MissingMandatoryValueError(context.getSourceRef(),
							"is_producer", "is_operator", "is_authority"));
		}
	}
}
