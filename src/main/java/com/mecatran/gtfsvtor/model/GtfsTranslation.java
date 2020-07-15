package com.mecatran.gtfsvtor.model;

import java.util.Locale;
import java.util.Optional;

public interface GtfsTranslation
		extends GtfsObject<GtfsTranslation.Id>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "translations.txt";

	public GtfsTranslationTable getTableName();

	public String getFieldName();

	public Locale getLanguage();

	public String getTranslation();

	public Optional<String> getRecordId();

	public Optional<String> getRecordSubId();

	public Optional<String> getFieldValue();

	public GtfsTranslation.Id getId();

	/**
	 * A translation ID is an opaque object combining all the translation fields
	 * except the translation value (table name, field name, language, record
	 * id, record sub id, field value).
	 * 
	 * There normally cannot be two translations with the same ID.
	 * 
	 * A translation can be queried via two IDs: either using
	 * record_id+record_sub_id, or field_value.
	 */
	public interface Id extends GtfsId<Id, GtfsTranslation> {
	}

	public interface Builder {

		public Builder withSourceLineNumber(long lineNumber);

		public Builder withTableName(GtfsTranslationTable tableName);

		public Builder withFieldName(String fieldName);

		public Builder withLanguage(Locale language);

		public Builder withTranslation(String translation);

		public Builder withRecordId(String recordId);

		public Builder withRecordSubId(String recordSubId);

		public Builder withFieldValue(String fieldValue);

		public GtfsTranslation build();
	}
}
