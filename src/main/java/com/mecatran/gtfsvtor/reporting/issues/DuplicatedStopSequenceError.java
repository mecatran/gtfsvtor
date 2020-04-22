package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Duplicated stop sequence")
public class DuplicatedStopSequenceError implements ReportIssue {

	private GtfsRoute route;
	private GtfsTrip trip;
	private GtfsStopTime stopTime1, stopTime2;
	private GtfsTripStopSequence stopSequence;

	public DuplicatedStopSequenceError(GtfsRoute route, GtfsTrip trip,
			GtfsStopTime stopTime1, GtfsStopTime stopTime2,
			GtfsTripStopSequence stopSequence) {
		this.route = route;
		this.trip = trip;
		this.stopTime1 = stopTime1;
		this.stopTime2 = stopTime2;
		this.stopSequence = stopSequence;
	}

	public GtfsRoute getRoute() {
		return route;
	}

	public GtfsTrip getTrip() {
		return trip;
	}

	public GtfsTripStopSequence getStopSequence() {
		return stopSequence;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Duplicated stop sequence {0} at stop {1} and {2} in trip {3} of route {4} {5}",
				fmt.var(stopSequence.toString()), fmt.id(stopTime1.getStopId()),
				fmt.id(stopTime2.getStopId()), fmt.id(trip.getId()),
				fmt.id(route.getId()), fmt.var(route.getShortName()));
	}
}
