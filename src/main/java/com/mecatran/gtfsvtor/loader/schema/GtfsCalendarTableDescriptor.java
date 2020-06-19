package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsCalendar.class, tableName = GtfsCalendar.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"service_id", "monday", "tuesday", "wednesday", "thursday", "friday",
		"saturday", "sunday", "start_date", "end_date" })
public class GtfsCalendarTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsCalendar.Builder builder = new GtfsCalendar.Builder(
				erow.getString("service_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withDow(erow.getBoolean("monday", true),
						erow.getBoolean("tuesday", true),
						erow.getBoolean("wednesday", true),
						erow.getBoolean("thursday", true),
						erow.getBoolean("friday", true),
						erow.getBoolean("saturday", true),
						erow.getBoolean("sunday", true))
				.withStartDate(erow.getLogicalDate("start_date", true))
				.withEndDate(erow.getLogicalDate("end_date", true));
		GtfsCalendar calendar = builder.build();
		context.getAppendableDao().addCalendar(calendar,
				context.getSourceContext());
		return calendar;
	}
}
