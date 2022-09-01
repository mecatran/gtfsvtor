package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
import com.mecatran.gtfsvtor.model.GtfsFareProduct;
import com.mecatran.gtfsvtor.model.GtfsFareTransferRule;
import com.mecatran.gtfsvtor.model.GtfsLegGroup;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsFareTransferRule.class, tableName = GtfsFareTransferRule.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"fare_transfer_type" })
public class GtfsFareTransferRuleTableDescriptor
		implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFareTransferRule.Builder builder = new GtfsFareTransferRule.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withFromLegGroupId(
						GtfsLegGroup.id(erow.getString("from_leg_group_id")))
				.withToLegGroupId(
						GtfsLegGroup.id(erow.getString("to_leg_group_id")))
				.withTransferCount(erow.getInteger("transfer_count",
						Requiredness.OPTIONAL))
				.withDurationLimit(erow.getInteger("duration_limit",
						Requiredness.OPTIONAL))
				.withDurationLimitType(
						erow.getFareDurationLimit("duration_limit_type"))
				.withFareTransferType(
						erow.getFareTransferType("fare_transfer_type"))
				.withFareProductId(GtfsFareProduct.id(erow
						.getString("fare_product_id", Requiredness.OPTIONAL)));
		GtfsFareTransferRule fareTransferRule = builder.build();
		context.getAppendableDao().addFareTransferRule(fareTransferRule,
				context.getSourceContext());
		return fareTransferRule;
	}
}
