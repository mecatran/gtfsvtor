package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsFareLegRule;
import com.mecatran.gtfsvtor.model.GtfsFareProduct;
import com.mecatran.gtfsvtor.model.GtfsLegGroup;
import com.mecatran.gtfsvtor.model.GtfsNetwork;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsFareLegRule.class, tableName = GtfsFareLegRule.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"fare_product_id" })
public class GtfsFareLegRuleTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFareLegRule.Builder builder = new GtfsFareLegRule.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withLegGroupId(GtfsLegGroup.id(erow.getString("leg_group_id")))
				.withNetworkId(GtfsNetwork.id(erow.getString("network_id")))
				.withFromAreaId(GtfsArea.id(erow.getString("from_area_id")))
				.withToAreaId(GtfsArea.id(erow.getString("to_area_id")))
				.withFareProductId(GtfsFareProduct.id(erow
						.getString("fare_product_id", Requiredness.MANDATORY)));
		GtfsFareLegRule fareLegRule = builder.build();
		context.getAppendableDao().addFareLegRule(fareLegRule,
				context.getSourceContext());
		return fareLegRule;
	}
}
