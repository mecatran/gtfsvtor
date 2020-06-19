package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsRouteType;

@TableDescriptorPolicy(objectClass = GtfsRoute.class, tableName = GtfsRoute.TABLE_NAME, mandatory = true, mandatoryColumns = {
		"route_id", "route_type" })
public class GtfsRouteTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsRoute.Builder builder = new GtfsRoute.Builder(
				erow.getString("route_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withAgencyId(
						GtfsAgency.id(erow.getString("agency_id", null, false)))
				.withType(GtfsRouteType
						.fromValue(erow.getInteger("route_type", true)))
				.withShortName(erow.getString("route_short_name"))
				.withLongName(erow.getString("route_long_name"))
				.withDescription(erow.getString("route_desc"))
				.withUrl(erow.getString("route_url"))
				.withColor(erow.getColor("route_color"))
				.withTextColor(erow.getColor("route_text_color"))
				.withSortOrder(erow.getInteger("route_sort_order", false));
		GtfsRoute route = builder.build();
		context.getAppendableDao().addRoute(route, context.getSourceContext());
		return route;
	}
}
