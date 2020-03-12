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
public class TooFastTravelIssue implements ReportIssue, Comparable<TooFastTravelIssue> {

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
				"Too fast travel between {0} @{1} and {2} @{3} (distance {4}m), in trip {5} of route {6} {7}: {8} > {9} m/s",
				fmt.id(stop1.getId()),
				fmt.time(stopTime1.getDepartureOrArrivalTime()),
				fmt.id(stop2.getId()),
				fmt.time(stopTime2.getDepartureOrArrivalTime()),
				fmt.var(String.format("%.2f", distanceMeters)),
				fmt.id(trip.getId()), fmt.id(route.getId()),
				fmt.var(route.getShortName()),
				fmt.var(String.format("%.2f", speedMps)),
				fmt.var(String.format("%.2f", maxSpeedMps)));
	}

	@Override
	public int compareTo(TooFastTravelIssue o) {
		// Compare on computed speed, highest first
		return -Double.compare(speedMps, o.speedMps);
	}
}
