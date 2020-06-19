package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsFrequency;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsTrip;

@TableDescriptorPolicy(objectClass = GtfsFrequency.class, tableName = GtfsFrequency.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"trip_id", "start_time", "end_time", "headway_secs" })
public class GtfsFrequencyTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFrequency.Builder builder = new GtfsFrequency.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withTripId(GtfsTrip.id(erow.getString("trip_id")))
				.withStartTime(erow.getLogicalTime("start_time", true))
				.withEndTime(erow.getLogicalTime("end_time", true))
				.withHeadwaySeconds(erow.getInteger("headway_secs", true))
				.withExactTimes(erow.getExactTimes("exact_times"));
		GtfsFrequency frequency = builder.build();
		context.getAppendableDao().addFrequency(frequency,
				context.getSourceContext());
		return frequency;
	}
}
