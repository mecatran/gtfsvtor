package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsLevel;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsLevel.class, tableName = GtfsLevel.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"level_id", "level_index" })
public class GtfsLevelTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsLevel.Builder builder = new GtfsLevel.Builder(
				erow.getString("level_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withIndex(erow.getDouble("level_index", true))
				.withName(erow.getString("level_name"));
		GtfsLevel level = builder.build();
		context.getAppendableDao().addLevel(level, context.getSourceContext());
		return level;
	}
}
