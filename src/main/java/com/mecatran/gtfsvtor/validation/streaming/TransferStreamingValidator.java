package com.mecatran.gtfsvtor.validation.streaming;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTransferType;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidReferenceError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.StreamingValidateType;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

@StreamingValidateType(GtfsTransfer.class)
public class TransferStreamingValidator
		implements StreamingValidator<GtfsTransfer> {

	@Override
	public void validate(Class<? extends GtfsTransfer> clazz,
			GtfsTransfer transfer, StreamingValidator.Context context) {
		ReportSink reportSink = context.getReportSink();
		// stop from / to ID is primary key and tested by DAO
		if (transfer.getMinTransferTime() != null
				&& transfer.getMinTransferTime() < 0)
			reportSink.report(new InvalidFieldFormatError(
					context.getSourceInfo(), "min_transfer_time",
					Integer.toString(transfer.getMinTransferTime()),
					"positive integer"));

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
		// min transfer time should be present for TIMED type
		if (transfer.getNonNullType() == GtfsTransferType.TIMED
				&& transfer.getMinTransferTime() == null) {
			reportSink.report(new MissingMandatoryValueError(
					context.getSourceInfo(), "min_transfer_time"));
		}
		// TODO Should we warn if min time is present for other types?
	}
}
