package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Trip transfer issues")
public class TripTransferTooLongDurationError implements ReportIssue {

	private GtfsTransfer transfer;
	private GtfsTrip fromTrip, toTrip;
	private GtfsLogicalTime lastFromArrTime, firstToDepTime;
	private int maxDurationSec;
	private List<SourceRefWithFields> sourceInfos;

	public TripTransferTooLongDurationError(GtfsTransfer transfer,
			GtfsTrip fromTrip, GtfsTrip toTrip, GtfsLogicalTime lastFromArrTime,
			GtfsLogicalTime firstToDepTime, int maxDurationSec) {
		this.transfer = transfer;
		this.fromTrip = fromTrip;
		this.toTrip = toTrip;
		this.lastFromArrTime = lastFromArrTime;
		this.firstToDepTime = firstToDepTime;
		this.maxDurationSec = maxDurationSec;
		this.sourceInfos = Arrays.asList(new SourceRefWithFields(
				transfer.getSourceRef(), "from_trip_id", "to_trip_id"));
	}

	public GtfsTransfer getTransfer() {
		return transfer;
	}

	public GtfsTrip getFromTrip() {
		return fromTrip;
	}

	public GtfsTrip getToTrip() {
		return toTrip;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Transfer between trip {0} (arriving at {1}) and {2} (departing at {3}), duration of {4} is too long (max {5})",
				fmt.id(fromTrip.getId()), fmt.time(lastFromArrTime),
				fmt.id(toTrip.getId()), fmt.time(firstToDepTime),
				fmt.durationSec(firstToDepTime.getSecondSinceMidnight()
						- lastFromArrTime.getSecondSinceMidnight()),
				fmt.durationSec(maxDurationSec));
	}
}
