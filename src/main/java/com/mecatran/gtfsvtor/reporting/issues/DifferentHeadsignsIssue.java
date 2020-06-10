package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Joined trips have different headsigns")
public class DifferentHeadsignsIssue implements ReportIssue {

	private GtfsTrip trip1;
	private GtfsTrip trip2;

	public DifferentHeadsignsIssue(GtfsTrip trip1, GtfsTrip trip2) {
		this.trip1 = trip1;
		this.trip2 = trip2;
	}

	public GtfsTrip getTrip1() {
		return trip1;
	}

	public GtfsTrip getTrip2() {
		return trip2;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"For joined trips, both trips {0} or {1} should have identical trip_headsign, but {2} and {3} are different.",
				fmt.id(trip1.getId()), fmt.id(trip2.getId()),
				trip1.getHeadsign(), trip2.getHeadsign());
	}
}
