package com.mecatran.gtfsvtor.validation.streaming;

import java.util.Objects;
import java.util.Optional;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsFareProduct;
import com.mecatran.gtfsvtor.model.GtfsFareTransferRule;
import com.mecatran.gtfsvtor.model.GtfsLegGroup;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsFareTransferRule.class)
public class FareTransferRuleStreamingValidator
		implements StreamingValidator<GtfsFareTransferRule> {

	@Override
	public void validate(Class<? extends GtfsFareTransferRule> clazz,
			GtfsFareTransferRule fareTransferRule,
			StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		ReadOnlyDao dao = context.getPartialDao();

		// Check leg group reference
		checkLegGroupReference(context, fareTransferRule.getFromLegGroupId(),
				"from_leg_group_id");
		checkLegGroupReference(context, fareTransferRule.getToLegGroupId(),
				"to_leg_group_id");

		// Check fare product reference
		if (fareTransferRule.getFareProductId().isPresent()
				&& dao.getFareProduct(
						fareTransferRule.getFareProductId().get()) == null) {
			reportSink.report(
					new InvalidReferenceError(context.getSourceRef(),
							"fare_product_id",
							fareTransferRule.getFareProductId().get()
									.getInternalId(),
							GtfsFareProduct.TABLE_NAME, "fare_product_id"),
					context.getSourceInfo());
		}

		// Check transfer count required (from_leg = to_leg)
		if (fareTransferRule.getFromLegGroupId().isPresent()
				&& fareTransferRule.getToLegGroupId().isPresent()
				&& Objects.equals(fareTransferRule.getFromLegGroupId().get(),
						fareTransferRule.getToLegGroupId().get())
				&& !fareTransferRule.getTransferCount().isPresent()) {
			reportSink.report(new MissingMandatoryValueError(
					context.getSourceRef(), "transfer_count"));
		}

		// Check duration limit type required (duration limit present)
		if (fareTransferRule.getDurationLimit().isPresent()
				&& !fareTransferRule.getDurationLimitType().isPresent()) {
			reportSink.report(new MissingMandatoryValueError(
					context.getSourceRef(), "duration_limit_type"));
		}
	}

	private void checkLegGroupReference(StreamingValidator.Context context,
			Optional<GtfsLegGroup.Id> optLegGroupId, String fieldName) {
		if (optLegGroupId.isPresent() && !context.getPartialDao()
				.hasLegGroupId(optLegGroupId.get())) {
			context.getReportSink()
					.report(new InvalidReferenceError(context.getSourceRef(),
							fieldName, optLegGroupId.get().getInternalId(),
							GtfsArea.TABLE_NAME, "leg_group_id"),
							context.getSourceInfo());
		}
	}
}
