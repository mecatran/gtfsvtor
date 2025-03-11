package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
import com.mecatran.gtfsvtor.model.GtfsLocationGroup;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsLocationGroup.class, tableName = GtfsLocationGroup.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"location_group_id"})
public class GtfsLocationGroupTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsLocationGroup.Builder builder = new GtfsLocationGroup.Builder(
				erow.getString("location_group_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withName(erow.getString("location_group_name", Requiredness.OPTIONAL));

		GtfsLocationGroup locationGroup = builder.build();
		context.getAppendableDao().addLocationGroup(locationGroup, context.getSourceContext());
		return locationGroup;
	}
}
