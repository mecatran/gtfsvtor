package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Time travel")
public class TimeTravelError implements ReportIssue {

	private GtfsRoute route;
	private GtfsTrip trip;
	private GtfsStopTime stopTime1, stopTime2;
	// Both stops can be null. Use Optional?
	private GtfsStop stop1, stop2;

	public TimeTravelError(GtfsRoute route, GtfsTrip trip,
			GtfsStopTime stopTime1, GtfsStop stop1, GtfsStopTime stopTime2,
			GtfsStop stop2) {
		this.route = route;
		this.trip = trip;
		this.stopTime1 = stopTime1;
		this.stop1 = stop1;
		this.stopTime2 = stopTime2;
		this.stop2 = stop2;
	}

	public GtfsRoute getRoute() {
		return route;
	}

	public GtfsTrip getTrip() {
		return trip;
	}

	public GtfsStop getStop1() {
		return stop1;
	}

	public GtfsStop getStop2() {
		return stop2;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Time-travel between stop {0} {1} seq {2} @{3} and stop {4} {5} seq {6} @{7}, in trip {8} of route {9} {10}, arrival before departure.",
				fmt.id(stop1 == null ? null : stop1.getId()),
				fmt.var(stop1 == null ? null : stop1.getName()),
				fmt.id(stopTime1.getStopSequence().toString()),
				fmt.time(stopTime1.getDepartureOrArrivalTime()),
				fmt.id(stop2 == null ? null : stop2.getId()),
				fmt.var(stop2 == null ? null : stop2.getName()),
				fmt.id(stopTime2.getStopSequence().toString()),
				fmt.time(stopTime2.getDepartureOrArrivalTime()),
				fmt.id(trip.getId()), fmt.id(route.getId()),
				fmt.var(route.getShortName()));
	}
}
