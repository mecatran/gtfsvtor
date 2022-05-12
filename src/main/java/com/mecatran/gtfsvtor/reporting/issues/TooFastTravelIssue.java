package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(categoryName = "Too fast travel")
public class TooFastTravelIssue
		implements ReportIssue, Comparable<TooFastTravelIssue> {

	private GtfsRoute route;
	private GtfsTrip trip;
	private GtfsStopTime stopTime1, stopTime2;
	private GtfsStop stop1, stop2;
	private double distanceMeters;
	private double speedMps;
	private double maxSpeedMps;
	private ReportIssueSeverity severity;

	public TooFastTravelIssue(GtfsRoute route, GtfsTrip trip,
			GtfsStopTime stopTime1, GtfsStop stop1, GtfsStopTime stopTime2,
			GtfsStop stop2, double distanceMeters, double speedMps,
			double maxSpeedMps, ReportIssueSeverity severity) {
		this.route = route;
		this.trip = trip;
		this.stopTime1 = stopTime1;
		this.stop1 = stop1;
		this.stopTime2 = stopTime2;
		this.stop2 = stop2;
		this.distanceMeters = distanceMeters;
		this.speedMps = speedMps;
		this.maxSpeedMps = maxSpeedMps;
		this.severity = severity;
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

	public double getDistanceMeters() {
		return distanceMeters;
	}

	public double getSpeedMps() {
		return speedMps;
	}

	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Too fast travel between stop {0} seq {1} @{2} and stop {3} seq {4} @{5} (distance {6}), in trip {7} of route {8} {9}: {10} > {11}",
				fmt.id(stop1.getId()),
				fmt.id(stopTime1.getStopSequence().toString()),
				fmt.time(stopTime1.getDepartureOrArrivalTime()),
				fmt.id(stop2.getId()),
				fmt.id(stopTime2.getStopSequence().toString()),
				fmt.time(stopTime2.getDepartureOrArrivalTime()),
				fmt.var(fmt.distance(distanceMeters)),
				fmt.id(trip.getId()), fmt.id(route.getId()),
				fmt.var(route.getShortName()), fmt.var(fmt.speed(speedMps)),
				fmt.var(fmt.speed(maxSpeedMps)));
	}

	@Override
	public int compareTo(TooFastTravelIssue o) {
		// Compare on computed speed, highest first
		return -Double.compare(speedMps, o.speedMps);
	}
}
