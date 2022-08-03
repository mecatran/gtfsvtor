package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsArea;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsArea.class, tableName = GtfsArea.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"area_id" })
public class GtfsAreaTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsArea.Builder builder = new GtfsArea.Builder(
				erow.getString("area_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withName(erow.getString("area_name"));
		GtfsArea area = builder.build();
		context.getAppendableDao().addArea(area, context.getSourceContext());
		return area;
	}
}
