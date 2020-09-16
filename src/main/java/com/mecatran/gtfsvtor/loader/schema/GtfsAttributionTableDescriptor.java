package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsAttribution;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsTrip;

@TableDescriptorPolicy(objectClass = GtfsAttribution.class, tableName = GtfsAttribution.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"organization_name" })
public class GtfsAttributionTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsAttribution.Builder builder = new GtfsAttribution.Builder(
				erow.getString("attribution_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withAgencyId(GtfsAgency.id(erow.getString("agency_id", false)))
				.withRouteId(GtfsRoute.id(erow.getString("route_id", false)))
				.withTripId(GtfsTrip.id(erow.getString("trip_id", false)))
				.withOrganizationName(erow.getString("organization_name"))
				.withIsProducer(erow.getBoolean("is_producer", false))
				.withIsOperator(erow.getBoolean("is_operator", false))
				.withIsAuthority(erow.getBoolean("is_authority", false))
				.withAttributionUrl(erow.getString("attribution_url", false))
				.withAttributionEmail(
						erow.getString("attribution_email", false))
				.withAttributionPhone(
						erow.getString("attribution_phone", false));
		GtfsAttribution attribution = builder.build();
		context.getAppendableDao().addAttribution(attribution,
				context.getSourceContext());
		return attribution;
	}
}
