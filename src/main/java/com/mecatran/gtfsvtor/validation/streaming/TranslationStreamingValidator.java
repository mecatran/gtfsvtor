package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.model.GtfsTranslation;
import com.mecatran.gtfsvtor.model.GtfsTranslationTable;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsTranslation.class)
public class TranslationStreamingValidator
		implements StreamingValidator<GtfsTranslation> {

	@Override
	public void validate(Class<? extends GtfsTranslation> clazz,
			GtfsTranslation translation, StreamingValidator.Context context) {

		// Check if we provided coherent references: either field_value, or
		// record_id + eventually sub_id
		if (translation.getFieldValue().isPresent()
				&& translation.getRecordId().isPresent()) {
			context.getReportSink().report(new InvalidFieldValueIssue(
					context.getSourceRef(), translation.getFieldValue().get(),
					"Either provide field_value or record_id but not both.",
					"field_value", "record_id"), context.getSourceInfo());
		}
		if (translation.getRecordSubId().isPresent()
				&& !translation.getRecordId().isPresent()) {
			context.getReportSink().report(new InvalidFieldValueIssue(
					context.getSourceRef(), translation.getRecordSubId().get(),
					"You must provide record_id if record_sub_id is set.",
					"record_id", "record_sub_id"), context.getSourceInfo());
		}
		if (translation.getTableName() == GtfsTranslationTable.FEED_INFO) {
			if (translation.getRecordId().isPresent()) {
				context.getReportSink().report(new InvalidFieldValueIssue(
						context.getSourceRef(), translation.getRecordId().get(),
						"You must not provide record_id for feed_info table.",
						"record_id"), context.getSourceInfo());
			}
			if (translation.getFieldValue().isPresent()) {
				context.getReportSink().report(new InvalidFieldValueIssue(
						context.getSourceRef(),
						translation.getFieldValue().get(),
						"You must not provide field_value for feed_info table.",
						"field_value"), context.getSourceInfo());
			}
		}

		// Check reference to object (in record ID mode)
		if (translation.getTableName() != null
				&& translation.getRecordId().isPresent()) {
			if (context.getPartialDao().getObject(translation.getTableName(),
					translation.getRecordId().get(),
					translation.getRecordSubId()) == null) {
				// TODO Build ref ID from table name
				context.getReportSink().report(new InvalidReferenceError(
						context.getSourceRef(), "record_id",
						translation.getRecordId().get(),
						translation.getTableName().getValue() + ".txt", "id"));
			}
		}

		// Reference to object in field value mode need a DAO validator
	}
}
