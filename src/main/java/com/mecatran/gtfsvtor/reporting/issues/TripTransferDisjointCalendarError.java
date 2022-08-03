package com.mecatran.gtfsvtor.reporting.issues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Trip transfer issues")
public class TripTransferDisjointCalendarError implements ReportIssue {

	private GtfsTransfer transfer;
	private GtfsTrip fromTrip, toTrip;
	private List<SourceRefWithFields> sourceInfos;

	public TripTransferDisjointCalendarError(GtfsTransfer transfer,
			GtfsTrip fromTrip, GtfsTrip toTrip, GtfsCalendar fromCalendar,
			GtfsCalendar toCalendar) {
		this.transfer = transfer;
		this.fromTrip = fromTrip;
		this.toTrip = toTrip;
		this.sourceInfos = new ArrayList<>();
		this.sourceInfos.add(new SourceRefWithFields(transfer.getSourceRef(),
				"from_trip_id", "to_trip_id"));
		if (fromCalendar != null) {
			this.sourceInfos
					.add(new SourceRefWithFields(fromCalendar.getSourceRef()));
		}
		if (toCalendar != null) {
			this.sourceInfos
					.add(new SourceRefWithFields(toCalendar.getSourceRef()));
		}
		Collections.sort(this.sourceInfos);
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
				"Transfer between trip {0} and {1}, both trips must operate at least on a common day, but service {2} and {3} are disjoint.",
				fmt.id(fromTrip.getId()), fmt.id(toTrip.getId()),
				fmt.id(fromTrip.getServiceId()), fmt.id(toTrip.getServiceId()));
	}
}
