package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsTranslation;
import com.mecatran.gtfsvtor.model.impl.InternedGtfsTranslation;

@TableDescriptorPolicy(objectClass = GtfsTranslation.class, tableName = GtfsTranslation.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"table_name", "field_name", "language", "translation" })
public class GtfsTranslationTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsTranslation.Builder builder = new InternedGtfsTranslation.Builder();
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withTableName(erow.getTranslationTable("table_name"))
				.withFieldName(erow.getString("field_name", true))
				.withLanguage(erow.getLocale("language", true))
				.withTranslation(erow.getString("translation", true))
				.withRecordId(erow.getString("record_id", false))
				.withRecordSubId(erow.getString("record_sub_id", false))
				.withFieldValue(erow.getString("field_value", false));
		GtfsTranslation translation = builder.build();
		context.getAppendableDao().addTranslation(translation,
				context.getSourceContext());
		return translation;
	}
}
