package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsZone;

@TableDescriptorPolicy(objectClass = GtfsStop.class, tableName = GtfsStop.TABLE_NAME, mandatory = true, mandatoryColumns = {
		"stop_id", "stop_name", "stop_lat", "stop_lon" })
public class GtfsStopTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsStop.Builder builder = new GtfsStop.Builder(
				erow.getString("stop_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withType(erow.getStopType("location_type"))
				.withCode(erow.getString("stop_code"))
				.withName(erow.getString("stop_name", true))
				.withCoordinates(
						erow.getDouble("stop_lat", null, Double.NaN, false),
						erow.getDouble("stop_lon", null, Double.NaN, false))
				.withParentId(GtfsStop.id(erow.getString("parent_station")))
				.withDescription(erow.getString("stop_desc"))
				.withZoneId(GtfsZone.id(erow.getString("zone_id")))
				.withUrl(erow.getString("stop_url"))
				.withTimezone(erow.getTimeZone("stop_timezone"))
				.withWheelchairBoarding(
						erow.getWheelchairAccess("wheelchair_boarding"))
				.withLevelId(GtfsLevel.id(erow.getString("level_id")))
				.withPlatformCode(erow.getString("platform_code"));
		GtfsStop stop = builder.build();
		context.getAppendableDao().addStop(stop, context.getSourceContext());
		return stop;
	}
}
