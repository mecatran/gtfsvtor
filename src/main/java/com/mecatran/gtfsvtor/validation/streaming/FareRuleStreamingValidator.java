package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsZone;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsFareRule.class)
public class FareRuleStreamingValidator
		implements StreamingValidator<GtfsFareRule> {

	@Override
	public void validate(Class<? extends GtfsFareRule> clazz,
			GtfsFareRule fareRule, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();

		// Check fare reference
		if (fareRule.getFareId() != null
				&& dao.getFareAttribute(fareRule.getFareId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"fare_id", fareRule.getFareId().getInternalId(),
					GtfsFareAttribute.TABLE_NAME, "fare_id"));
		}

		// Check route reference
		if (fareRule.getRouteId() != null
				&& dao.getRoute(fareRule.getRouteId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"route_id", fareRule.getRouteId().getInternalId(),
					GtfsAgency.TABLE_NAME, "route_id"));
		}

		// Check zone reference
		checkZoneReference(context, fareRule.getOriginId(), "origin_id");
		checkZoneReference(context, fareRule.getOriginId(), "destination_id");
		checkZoneReference(context, fareRule.getOriginId(), "contains_id");

		// Check at least one ID is set?
	}

	private void checkZoneReference(StreamingValidator.Context context,
			GtfsZone.Id zoneId, String fieldName) {
		if (zoneId != null && !context.getPartialDao().hasZoneId(zoneId)) {
			context.getReportSink()
					.report(new InvalidReferenceError(context.getSourceInfo(),
							"zone_id", zoneId.getInternalId(),
							GtfsStop.TABLE_NAME, "zone_id"));
		}
	}
}
