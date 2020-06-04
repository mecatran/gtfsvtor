package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Wrong drop off/pickup type for split/joined trips")
public class WrongDropOffPickUpTypeForSplitTripsIssue implements ReportIssue {

	private GtfsTrip.Id trip1;
	private GtfsTrip.Id trip2;
	private boolean isSplit;

	public WrongDropOffPickUpTypeForSplitTripsIssue(GtfsTrip.Id trip1,
			GtfsTrip.Id trip2, boolean isSplit) {
		this.trip1 = trip1;
		this.trip2 = trip2;
		this.isSplit = isSplit;
	}

	public GtfsTrip.Id getTrip1() {
		return trip1;
	}

	public GtfsTrip.Id getTrip2() {
		return trip2;
	}

	public boolean isSplit() {
		return isSplit;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"For {0} trips, either trip {1} or {2} should have NONE as {3} type for common stops.",
				isSplit ? "split" : "joined", fmt.id(trip1), fmt.id(trip2),
				isSplit ? "drop off" : "pickup");
	}
}
