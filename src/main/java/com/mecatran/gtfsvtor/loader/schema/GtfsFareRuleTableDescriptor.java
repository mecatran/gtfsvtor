package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsZone;

@TableDescriptorPolicy(objectClass = GtfsFareRule.class, tableName = GtfsFareRule.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"fare_id" })
public class GtfsFareRuleTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFareRule.Builder builder = new GtfsFareRule.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withFareId(GtfsFareAttribute.id(erow.getString("fare_id")))
				.withRouteId(GtfsRoute.id(erow.getString("route_id")))
				.withOriginId(GtfsZone.id(erow.getString("origin_id")))
				.withDestinationId(
						GtfsZone.id(erow.getString("destination_id")))
				.withContainsId(GtfsZone.id(erow.getString("contains_id")));
		GtfsFareRule fareRule = builder.build();
		context.getAppendableDao().addFareRule(fareRule,
				context.getSourceContext());
		return fareRule;
	}
}
