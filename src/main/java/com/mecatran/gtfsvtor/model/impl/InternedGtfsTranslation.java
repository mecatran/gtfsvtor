package com.mecatran.gtfsvtor.model.impl;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsCompositeId;
import com.mecatran.gtfsvtor.model.GtfsTranslation;
import com.mecatran.gtfsvtor.model.GtfsTranslationTable;

/**
 * The default and single implementation of GtfsTranslation, interning all
 * commonly shared values and optimizing storage of object references (we use
 * only record_id or field_value, not both).
 */
public class InternedGtfsTranslation implements GtfsTranslation {

	private GtfsTranslationColRef colRef;
	private GtfsTranslationValRef valRef;
	private String translation;

	private long sourceLineNumber;

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	@Override
	public GtfsTranslationTable getTableName() {
		return colRef.getTableName();
	}

	@Override
	public String getFieldName() {
		return colRef.getFieldName();
	}

	@Override
	public Locale getLanguage() {
		return colRef.getLanguage();
	}

	@Override
	public String getTranslation() {
		return translation;
	}

	@Override
	public Optional<String> getRecordId() {
		return valRef.getRecordId();
	}

	@Override
	public Optional<String> getRecordSubId() {
		return valRef.getRecordSubId();
	}

	@Override
	public Optional<String> getFieldValue() {
		return valRef.getFieldValue();
	}

	@Override
	public InternedId getId() {
		return id(colRef, valRef);
	}

	GtfsTranslationColRef getColRef() {
		return colRef;
	}

	GtfsTranslationValRef getValRef() {
		return valRef;
	}

	@Override
	public String toString() {
		return "Translation{tablename=" + getTableName() + ",fieldName="
				+ getFieldName() + ",language=" + getLanguage()
				+ ",translation='" + getTranslation() + "'}";
	}

	public static class Builder implements GtfsTranslation.Builder {
		private InternedGtfsTranslation translation;
		private GtfsTranslationColRef.Builder colRefBuilder;
		private GtfsTranslationValRef.Builder valRefBuilder;

		public Builder() {
			translation = new InternedGtfsTranslation();
			colRefBuilder = new GtfsTranslationColRef.Builder();
			valRefBuilder = new GtfsTranslationValRef.Builder();
		}

		@Override
		public Builder withSourceLineNumber(long lineNumber) {
			translation.sourceLineNumber = lineNumber;
			return this;
		}

		@Override
		public Builder withTableName(GtfsTranslationTable tableName) {
			colRefBuilder.withTableName(tableName);
			return this;
		}

		@Override
		public Builder withFieldName(String fieldName) {
			colRefBuilder.withFieldName(fieldName);
			return this;
		}

		@Override
		public Builder withLanguage(Locale language) {
			colRefBuilder.withLanguage(language);
			return this;
		}

		@Override
		public Builder withTranslation(String translation) {
			this.translation.translation = translation;
			return this;
		}

		@Override
		public Builder withRecordId(String recordId) {
			valRefBuilder.withRecordId(recordId);
			return this;
		}

		@Override
		public Builder withRecordSubId(String recordSubId) {
			valRefBuilder.withRecordSubId(recordSubId);
			return this;
		}

		@Override
		public Builder withFieldValue(String fieldValue) {
			valRefBuilder.withFieldValue(fieldValue);
			return this;
		}

		@Override
		public InternedGtfsTranslation build() {
			translation.colRef = colRefBuilder.build();
			translation.valRef = valRefBuilder.build();
			return translation;
		}
	}

	public static InternedId id(GtfsTranslationTable tableName,
			String fieldName, Locale language, String fieldValue) {
		return new InternedId(tableName, fieldName, language, fieldValue, null,
				null);
	}

	public static InternedId id(GtfsTranslationTable tableName,
			String fieldName, Locale language, String recordId,
			String recordSubId) {
		return new InternedId(tableName, fieldName, language, null, recordId,
				recordSubId);
	}

	public static InternedId id(GtfsTranslationColRef colRef,
			GtfsTranslationValRef valRef) {
		if (colRef == null || valRef == null) {
			return null;
		}
		GtfsTranslationTable tableName = colRef.getTableName();
		String fieldName = colRef.getFieldName();
		Locale lang = colRef.getLanguage();
		String fieldValue = valRef.getFieldValue().orElse(null);
		String recordId = valRef.getRecordId().orElse(null);
		String recordSubId = valRef.getRecordSubId().orElse(null);
		if (tableName == null || fieldName == null || lang == null
				|| (fieldValue == null && recordId == null)) {
			return null;
		}
		return new InternedId(tableName, fieldName, lang, fieldValue, recordId,
				recordSubId);
	}

	public static class InternedId
			extends GtfsCompositeId<String, GtfsTranslation>
			implements GtfsTranslation.Id {

		private InternedId(GtfsTranslationTable tableName, String fieldName,
				Locale language, String fieldValue, String recordId,
				String recordSubId) {
			super(tableName.getValue(), fieldName, language.getLanguage(),
					fieldValue, recordId, recordSubId);
		}
	}

	/**
	 * Since most of the time those 3 fields will be re-used, we keep them
	 * together and interned.
	 */
	private static class GtfsTranslationColRef {

		private GtfsTranslationTable tableName;
		private String fieldName;
		private Locale language;

		private static ConcurrentMap<GtfsTranslationColRef, GtfsTranslationColRef> CACHE = new ConcurrentHashMap<>();

		private GtfsTranslationColRef() {
		}

		private GtfsTranslationColRef intern() {
			return CACHE.computeIfAbsent(this, r -> r);
		}

		private GtfsTranslationTable getTableName() {
			return tableName;
		}

		private String getFieldName() {
			return fieldName;
		}

		private Locale getLanguage() {
			return language;
		}

		private static class Builder {
			private GtfsTranslationColRef translationRef;

			private Builder() {
				this.translationRef = new GtfsTranslationColRef();
			}

			private Builder withTableName(GtfsTranslationTable tableName) {
				this.translationRef.tableName = tableName;
				return this;
			}

			private Builder withFieldName(String fieldName) {
				this.translationRef.fieldName = fieldName;
				return this;
			}

			private Builder withLanguage(Locale language) {
				this.translationRef.language = language;
				return this;
			}

			private GtfsTranslationColRef build() {
				return this.translationRef.intern();
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(tableName, fieldName, language);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof GtfsTranslationColRef)) {
				return false;
			}
			GtfsTranslationColRef other = (GtfsTranslationColRef) obj;
			return Objects.equals(tableName, other.tableName)
					&& Objects.equals(fieldName, other.fieldName)
					&& Objects.equals(language, other.language);
		}
	}

	/**
	 * A translation will either define a record ID OR a field value; normally
	 * never both (except in case of errors). Sub-ID is never used in the
	 * current norm, only for GTFS with extended translations. We thus create
	 * two dedicated sub-classes keeping only the needed single field.
	 */
	private interface GtfsTranslationValRef {

		default Optional<String> getRecordId() {
			return Optional.empty();
		}

		default Optional<String> getRecordSubId() {
			return Optional.empty();
		}

		default Optional<String> getFieldValue() {
			return Optional.empty();
		}

		public static class Builder {
			private String recordId;
			private String recordSubId;
			private String fieldValue;

			private Builder() {
			}

			private Builder withRecordId(String recordId) {
				this.recordId = recordId == null ? null : recordId.intern();
				return this;
			}

			private Builder withRecordSubId(String recordSubId) {
				this.recordSubId = recordSubId == null ? null
						: recordSubId.intern();
				return this;
			}

			private Builder withFieldValue(String fieldValue) {
				this.fieldValue = fieldValue == null ? null
						: fieldValue.intern();
				return this;
			}

			private GtfsTranslationValRef build() {
				if (fieldValue != null && recordId == null
						&& recordSubId == null)
					return GtfsTranslationValRefFieldOnly
							.fromFieldValue(fieldValue);
				else if (recordId != null && recordSubId == null
						&& fieldValue == null)
					return GtfsTranslationValRefRecordIdOnly
							.fromRecordId(recordId);
				else
					return new GtfsTranslationValRefAllFields(recordId,
							recordSubId, fieldValue);
			}
		}
	}

	private abstract static class GtfsTranslationValRefDef
			implements GtfsTranslationValRef {

		@Override
		public int hashCode() {
			return Objects.hash(getRecordId().orElse(null),
					getRecordSubId().orElse(null),
					getFieldValue().orElse(null));
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof GtfsTranslationValRef)) {
				return false;
			}
			GtfsTranslationValRef other = (GtfsTranslationValRef) obj;
			return Objects.equals(getRecordId().orElse(null),
					other.getRecordId().orElse(null))
					&& Objects.equals(getRecordSubId().orElse(null),
							other.getRecordSubId().orElse(null))
					&& Objects.equals(getFieldValue().orElse(null),
							other.getFieldValue().orElse(null));
		}
	}

	private static class GtfsTranslationValRefFieldOnly
			extends GtfsTranslationValRefDef {
		private String fieldValue;
		private static ConcurrentMap<String, GtfsTranslationValRefFieldOnly> CACHE = new ConcurrentHashMap<>();

		private GtfsTranslationValRefFieldOnly(String fieldValue) {
			this.fieldValue = fieldValue;
		}

		private static GtfsTranslationValRefFieldOnly fromFieldValue(
				String fieldValue) {
			return CACHE.computeIfAbsent(fieldValue,
					GtfsTranslationValRefFieldOnly::new);
		}

		@Override
		public Optional<String> getFieldValue() {
			return Optional.ofNullable(fieldValue);
		}
	}

	private static class GtfsTranslationValRefRecordIdOnly
			extends GtfsTranslationValRefDef {
		private String recordId;
		private static ConcurrentMap<String, GtfsTranslationValRefRecordIdOnly> CACHE = new ConcurrentHashMap<>();

		private GtfsTranslationValRefRecordIdOnly(String recordId) {
			this.recordId = recordId;
		}

		private static GtfsTranslationValRefRecordIdOnly fromRecordId(
				String recordId) {
			return CACHE.computeIfAbsent(recordId,
					GtfsTranslationValRefRecordIdOnly::new);
		}

		@Override
		public Optional<String> getRecordId() {
			return Optional.ofNullable(recordId);
		}
	}

	/* TODO Add a GtfsTranslationValRefRecordIdAndSubId class? */

	private static class GtfsTranslationValRefAllFields
			extends GtfsTranslationValRefDef {
		private String recordId;
		private String recordSubId;
		private String fieldValue;
		// TODO Intern values?

		public GtfsTranslationValRefAllFields(String recordId,
				String recordSubId, String fieldValue) {
			this.recordId = recordId;
			this.recordSubId = recordSubId;
			this.fieldValue = fieldValue;
		}

		@Override
		public Optional<String> getRecordId() {
			return Optional.ofNullable(recordId);
		}

		@Override
		public Optional<String> getRecordSubId() {
			return Optional.ofNullable(recordSubId);
		}

		@Override
		public Optional<String> getFieldValue() {
			return Optional.ofNullable(fieldValue);
		}
	}
}
