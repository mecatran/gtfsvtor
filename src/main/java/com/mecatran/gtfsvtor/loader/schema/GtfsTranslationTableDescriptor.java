package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
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
				.withFieldName(
						erow.getString("field_name", Requiredness.MANDATORY))
				.withLanguage(
						erow.getLocale("language", Requiredness.MANDATORY))
				.withTranslation(
						erow.getString("translation", Requiredness.MANDATORY))
				.withRecordId(
						erow.getString("record_id", Requiredness.OPTIONAL))
				.withRecordSubId(
						erow.getString("record_sub_id", Requiredness.OPTIONAL))
				.withFieldValue(
						erow.getString("field_value", Requiredness.OPTIONAL));
		GtfsTranslation translation = builder.build();
		context.getAppendableDao().addTranslation(translation,
				context.getSourceContext());
		return translation;
	}
}
