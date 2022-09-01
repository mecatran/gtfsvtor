package com.mecatran.gtfsvtor.validation.streaming;

import java.util.Optional;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsFareLegRule;
import com.mecatran.gtfsvtor.model.GtfsFareProduct;
import com.mecatran.gtfsvtor.model.GtfsNetwork;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsFareLegRule.class)
public class FareLegRuleStreamingValidator
		implements StreamingValidator<GtfsFareLegRule> {

	@Override
	public void validate(Class<? extends GtfsFareLegRule> clazz,
			GtfsFareLegRule fareLegRule, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();

		Optional<GtfsNetwork.Id> optNetworkId = fareLegRule.getNetworkId();
		if (optNetworkId.isPresent() && !dao.hasNetworkId(optNetworkId.get())) {
			reportSink.report(
					new InvalidReferenceError(context.getSourceRef(),
							"network_id", optNetworkId.get().getInternalId(),
							GtfsRoute.TABLE_NAME, "network_id"),
					context.getSourceInfo());
		}

		// Check area reference
		checkAreaReference(context, fareLegRule.getFromAreaId(),
				"from_area_id");
		checkAreaReference(context, fareLegRule.getToAreaId(), "to_area_id");
		// Check fare product reference
		if (fareLegRule.getFareProductId() != null
				&& dao.getFareProduct(fareLegRule.getFareProductId()) == null) {
			reportSink.report(
					new InvalidReferenceError(context.getSourceRef(),
							"fare_product_id",
							fareLegRule.getFareProductId().getInternalId(),
							GtfsFareProduct.TABLE_NAME, "fare_product_id"),
					context.getSourceInfo());
		}
	}

	private void checkAreaReference(StreamingValidator.Context context,
			Optional<GtfsArea.Id> optAreaId, String fieldName) {
		if (optAreaId.isPresent()
				&& context.getPartialDao().getArea(optAreaId.get()) == null) {
			context.getReportSink()
					.report(new InvalidReferenceError(context.getSourceRef(),
							fieldName, optAreaId.get().getInternalId(),
							GtfsArea.TABLE_NAME, "area_id"),
							context.getSourceInfo());
		}
	}
}
