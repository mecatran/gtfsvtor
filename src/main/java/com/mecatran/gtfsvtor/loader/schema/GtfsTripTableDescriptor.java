package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsShape;
import com.mecatran.gtfsvtor.model.GtfsTrip;

@TableDescriptorPolicy(objectClass = GtfsTrip.class, tableName = GtfsTrip.TABLE_NAME, mandatory = true, mandatoryColumns = {
		"route_id", "service_id", "trip_id" })
public class GtfsTripTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsTrip.Builder builder = new GtfsTrip.Builder(
				erow.getString("trip_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withRouteId(GtfsRoute.id(erow.getString("route_id")))
				.withServiceId(GtfsCalendar.id(erow.getString("service_id")))
				.withHeadsign(erow.getString("trip_headsign"))
				.withShortName(erow.getString("trip_short_name"))
				.withBlockId(erow.getBlockId("block_id"))
				.withDirectionId(erow.getDirectionId("direction_id"))
				.withShapeId(GtfsShape.id(erow.getString("shape_id")))
				.withWheelchairAccessible(
						erow.getWheelchairAccess("wheelchair_accessible"))
				.withBikesAllowed(erow.getBikeAccess("bikes_allowed"));
		GtfsTrip trip = builder.build();
		context.getAppendableDao().addTrip(trip, context.getSourceContext());
		return trip;
	}
}
