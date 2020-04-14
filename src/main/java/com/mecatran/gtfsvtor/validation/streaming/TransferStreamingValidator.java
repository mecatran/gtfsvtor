package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.reporting.issues.UselessValueWarning;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsTransfer.class)
public class TransferStreamingValidator
		implements StreamingValidator<GtfsTransfer> {

	@ConfigurableOption(description = "Maximum transfer time, in seconds, above which we generate a warning")
	private Integer maxTransferTimeSecWarning = 3 * 60 * 60;

	@ConfigurableOption(description = "Maximum transfer time, in seconds, above which we generate an error")
	private Integer maxTransferTimeSecError = 24 * 60 * 60;

	@Override
	public void validate(Class<? extends GtfsTransfer> clazz,
			GtfsTransfer transfer, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		// stop from / to ID is primary key and tested by DAO
		if (transfer.getMinTransferTime() != null) {
			if (transfer.getMinTransferTime() < 0) {
				reportSink.report(new InvalidFieldFormatError(
						context.getSourceInfo(), "min_transfer_time",
						Integer.toString(transfer.getMinTransferTime()),
						"positive integer"));
			}
			if (transfer.getNonNullType() == GtfsTransferType.TIMED) {
				if (maxTransferTimeSecError != null && transfer
						.getMinTransferTime() > maxTransferTimeSecError) {
					reportSink.report(new InvalidFieldValueIssue(
							context.getSourceInfo(),
							Integer.toString(transfer.getMinTransferTime()),
							"Suspiciously large time", "min_transfer_time")
									.withSeverity(ReportIssueSeverity.ERROR));
				} else if (maxTransferTimeSecWarning != null && transfer
						.getMinTransferTime() > maxTransferTimeSecWarning) {
					reportSink.report(new InvalidFieldValueIssue(
							context.getSourceInfo(),
							Integer.toString(transfer.getMinTransferTime()),
							"Suspiciously large time", "min_transfer_time")
									.withSeverity(ReportIssueSeverity.WARNING));
				}
			}
		}

		ReadOnlyDao dao = context.getPartialDao();
		// check from/to stops reference
		if (transfer.getFromStopId() != null
				&& dao.getStop(transfer.getFromStopId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"from_stop_id", transfer.getFromStopId().getInternalId(),
					GtfsTransfer.TABLE_NAME, "from_stop_id"));
		}
		if (transfer.getToStopId() != null
				&& dao.getStop(transfer.getToStopId()) == null) {
			reportSink.report(new InvalidReferenceError(context.getSourceInfo(),
					"to_stop_id", transfer.getToStopId().getInternalId(),
					GtfsTransfer.TABLE_NAME, "to_stop_id"));
		}
		if (transfer.getNonNullType() == GtfsTransferType.TIMED) {
			// min transfer time should be present for TIMED type
			if (transfer.getMinTransferTime() == null) {
				reportSink.report(new MissingMandatoryValueError(
						context.getSourceInfo(), "min_transfer_time"));
			}
		} else {
			// min transfer time should not be present for other types
			if (transfer.getMinTransferTime() != null) {
				reportSink.report(new UselessValueWarning(
						context.getSourceInfo(), "min_transfer_time",
						Integer.toString(transfer.getMinTransferTime()),
						"This field is only useful if transfer_type=2"));
			}
		}
	}
}
