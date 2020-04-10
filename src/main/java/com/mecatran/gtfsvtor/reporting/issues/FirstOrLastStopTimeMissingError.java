package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR)
public class FirstOrLastStopTimeMissingError implements ReportIssue {

	private boolean first;
	private GtfsRoute route;
	private GtfsTrip trip;
	private GtfsStopTime stopTime;

	public FirstOrLastStopTimeMissingError(boolean first, GtfsRoute route,
			GtfsTrip trip, GtfsStopTime stopTime) {
		this.first = first;
		this.route = route;
		this.trip = trip;
		this.stopTime = stopTime;
	}

	public GtfsRoute getRoute() {
		return route;
	}

	public GtfsTrip getTrip() {
		return trip;
	}

	public GtfsStopTime getStopTime() {
		return stopTime;
	}

	@Override
	public String getCategoryName() {
		return first ? "First stop time missing" : "Last stop time missing";
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"{0} stop time at sequence {1} in trip {2} of route {3} {4} is missing.",
				first ? "First" : "Last",
				fmt.pre(stopTime.getStopSequence().toString()),
				fmt.id(trip.getId()), fmt.id(route.getId()),
				fmt.var(route.getShortName()));
	}
}
