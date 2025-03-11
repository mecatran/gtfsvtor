package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
import com.mecatran.gtfsvtor.model.GtfsLocationGroup;
import com.mecatran.gtfsvtor.model.GtfsLocationGroupStop;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsStop;

@TableDescriptorPolicy(objectClass = GtfsLocationGroup.class, tableName = GtfsLocationGroupStop.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"location_group_id", "stop_id"})
public class GtfsLocationGroupStopTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsLocationGroupStop.Builder builder = new GtfsLocationGroupStop.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withLocationGroupId(GtfsLocationGroup.id(erow.getString("location_group_id")))
				.withStopId(GtfsStop.id(erow.getString("stop_id")));

		GtfsLocationGroupStop locationGroupStop = builder.build();
		context.getAppendableDao().addLocationGroupStop(locationGroupStop, context.getSourceContext());
		return locationGroupStop;
	}
}
