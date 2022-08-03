package com.mecatran.gtfsvtor.reporting.issues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Trip transfer issues")
public class TripTransferDifferentCalendarError implements ReportIssue {

	private Set<GtfsTransfer> transfers;
	private Set<GtfsTrip> trips;
	private Set<GtfsCalendar.Id> calendarIds;
	private List<SourceRefWithFields> sourceInfos;

	public TripTransferDifferentCalendarError(Set<GtfsTransfer> transfers,
			Set<GtfsTrip> trips, Set<GtfsCalendar.Id> calendarIds,
			String transferField) {
		this.transfers = transfers;
		this.trips = trips;
		this.calendarIds = calendarIds;
		this.sourceInfos = new ArrayList<>();
		for (GtfsTransfer transfer : transfers) {
			this.sourceInfos.add(new SourceRefWithFields(
					transfer.getSourceRef(), transferField));
		}
		for (GtfsTrip trip : trips) {
			this.sourceInfos.add(
					new SourceRefWithFields(trip.getSourceRef(), "service_id"));
		}
		Collections.sort(this.sourceInfos);
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		String serviceIDs = calendarIds.stream().map(id -> fmt.id(id)).sorted()
				.collect(Collectors.joining(", "));
		String tripIDs = trips.stream().map(t -> fmt.id(t.getId())).sorted()
				.collect(Collectors.joining(", "));
		fmt.text(
				"In a 1-to-N, N-to-1 or N-to-N trip transfer continuation, all group of N trips must have the same service ID. Here {0} services ({1}) are used for {2} trips ({3}) in {4} transfers.",
				fmt.var("" + calendarIds.size()), serviceIDs,
				fmt.var("" + trips.size()), tripIDs,
				fmt.var("" + transfers.size()));
	}
}
