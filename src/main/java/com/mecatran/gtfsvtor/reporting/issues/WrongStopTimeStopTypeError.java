package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Wrong time stop type")
public class WrongStopTimeStopTypeError implements ReportIssue {

	private GtfsRoute route;
	private GtfsTrip trip;
	private GtfsStopTime stopTime;
	private GtfsStop stop;

	public WrongStopTimeStopTypeError(GtfsRoute route, GtfsTrip trip,
			GtfsStopTime stopTime, GtfsStop stop) {
		this.route = route;
		this.trip = trip;
		this.stopTime = stopTime;
		this.stop = stop;
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

	public GtfsStop getStop() {
		return stop;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Wrong type {0} for stop {1} at sequence {2} @ {3} in trip {4} of route {5} {6}, should be type STOP (0)",
				fmt.pre(stop.getType().toString()), fmt.id(stop.getId()),
				fmt.pre(stopTime.getStopSequence().toString()),
				fmt.time(stopTime.getDepartureOrArrivalTime()),
				fmt.id(trip.getId()), fmt.id(route.getId()),
				fmt.var(route.getShortName()));
	}
}
