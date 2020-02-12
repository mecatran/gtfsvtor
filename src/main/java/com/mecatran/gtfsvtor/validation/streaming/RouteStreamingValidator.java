package com.mecatran.gtfsvtor.validation.streaming;

import static com.mecatran.gtfsvtor.validation.impl.StreamingValidationUtils.checkNonNull;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsRoute.class)
public class RouteStreamingValidator implements StreamingValidator<GtfsRoute> {

	@Override
	public void validate(Class<? extends GtfsRoute> clazz, GtfsRoute route,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();
		checkNonNull(route::getAgencyId, "agency_id", context);
		checkNonNull(route::getType, "route_type", context);
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
	}
}
