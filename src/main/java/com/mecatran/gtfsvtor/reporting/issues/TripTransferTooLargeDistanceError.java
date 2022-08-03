package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsTransfer;
import com.mecatran.gtfsvtor.model.GtfsTrip;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.ERROR, categoryName = "Trip transfer issues")
public class TripTransferTooLargeDistanceError implements ReportIssue {

	private GtfsTransfer transfer;
	private GtfsTrip fromTrip, toTrip;
	private GtfsStop fromStop, toStop;
	private double distanceMeters, maxDistanceMeters;
	private List<SourceRefWithFields> sourceInfos;

	public TripTransferTooLargeDistanceError(GtfsTransfer transfer,
			GtfsTrip fromTrip, GtfsTrip toTrip, GtfsStop fromStop,
			GtfsStop toStop, double distanceMeters, double maxDistanceMeters) {
		this.transfer = transfer;
		this.fromTrip = fromTrip;
		this.toTrip = toTrip;
		this.fromStop = fromStop;
		this.toStop = toStop;
		this.distanceMeters = distanceMeters;
		this.maxDistanceMeters = maxDistanceMeters;
		this.sourceInfos = Arrays.asList(
				new SourceRefWithFields(transfer.getSourceRef(), "from_trip_id",
						"to_trip_id"),
				new SourceRefWithFields(fromStop.getSourceRef(), "stop_lat",
						"stop_lon"),
				new SourceRefWithFields(toStop.getSourceRef(), "stop_lat",
						"stop_lon"));
	}

	public GtfsTransfer getTransfer() {
		return transfer;
	}

	public GtfsTrip getFromTrip() {
		return fromTrip;
	}

	public GtfsTrip getToTrip() {
		return toTrip;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Transfer between trip {0} (arriving at stop {1}) and {2} (departing from stop {3}), distance between stops {4} is too large (max {5})",
				fmt.id(fromTrip.getId()), fmt.id(fromStop.getId()),
				fmt.id(toTrip.getId()), fmt.id(toStop.getId()),
				fmt.distance(distanceMeters), fmt.distance(maxDistanceMeters));
	}
}
