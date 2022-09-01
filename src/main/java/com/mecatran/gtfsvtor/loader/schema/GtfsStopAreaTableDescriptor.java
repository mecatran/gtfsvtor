package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopArea;

@TableDescriptorPolicy(objectClass = GtfsStopArea.class, tableName = GtfsStopArea.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"area_id", "stop_id" })
public class GtfsStopAreaTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsStopArea.Builder builder = new GtfsStopArea.Builder(
				GtfsArea.id(erow.getString("area_id")), 
				GtfsStop.id(erow.getString("stop_id")));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber());
		GtfsStopArea stopArea = builder.build();
		context.getAppendableDao().addStopArea(stopArea,
				context.getSourceContext());
		return stopArea;
	}
}
