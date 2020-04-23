package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsLogicalTime;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Too many stops with identical time")
public class TooManyStopWithSameTimeIssue implements ReportIssue {

	private GtfsRoute route;
	private GtfsTrip trip;
	// TODO Include first and last stop sequence in trip?
	private GtfsLogicalTime time;
	private int count;

	public TooManyStopWithSameTimeIssue(GtfsRoute route, GtfsTrip trip,
			GtfsLogicalTime time, int count) {
		this.route = route;
		this.trip = trip;
		this.time = time;
		this.count = count;
	}

	public GtfsRoute getRoute() {
		return route;
	}

	public GtfsTrip getTrip() {
		return trip;
	}

	public GtfsLogicalTime getTime() {
		return time;
	}

	public int getCount() {
		return count;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Too many stops with identical time: {0} × {1}, in trip {2} of route {3} {4}.",
				fmt.var(Integer.toString(count)), fmt.time(time),
				fmt.id(trip.getId()), fmt.id(route.getId()),
				fmt.var(route.getShortName()));
	}
}
