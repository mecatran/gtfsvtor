package com.mecatran.gtfsvtor.loader.schema;

import java.util.Set;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsCalendarDate.class, tableName = GtfsCalendarDate.TABLE_NAME, mandatoryColumns = {
		"service_id", "date", "exception_type" })
public class GtfsCalendarDateTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsCalendarDate.Builder builder = new GtfsCalendarDate.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withCalendarId(GtfsCalendar.id(erow.getString("service_id")))
				.withDate(erow.getLogicalDate("date", true)).withExceptionType(
						erow.getCalendarDateExceptionType("exception_type"));
		GtfsCalendarDate calendarDate = builder.build();
		context.getAppendableDao().addCalendarDate(calendarDate,
				context.getSourceContext());
		return calendarDate;
	}

	@Override
	public boolean isTableMandatory(Set<String> loadedTables) {
		// Calendar date table is mandatory only if no calendars table is
		// present
		return !loadedTables.contains(GtfsCalendar.TABLE_NAME);
	}
}
