package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(categoryName = "Too fast walking speed")
public class TooFastWalkingSpeed
		implements ReportIssue, Comparable<TooFastWalkingSpeed> {

	private GtfsStop fromStop, toStop;
	private double distanceMeters;
	private double speedMps;
	private double maxSpeedMps;
	private ReportIssueSeverity severity;

	private SourceRefWithFields sourceRef;

	public TooFastWalkingSpeed(DataObjectSourceRef sourceRef, GtfsStop fromStop,
			GtfsStop toStop, double distanceMeters, double speedMps,
			double maxSpeedMps, ReportIssueSeverity severity) {
		this.fromStop = fromStop;
		this.toStop = toStop;
		this.distanceMeters = distanceMeters;
		this.speedMps = speedMps;
		this.maxSpeedMps = maxSpeedMps;
		this.severity = severity;
		this.sourceRef = new SourceRefWithFields(sourceRef, "from_stop_id",
				"to_stop_id");
	}

	public GtfsStop getFromStop() {
		return fromStop;
	}

	public GtfsStop getToStop() {
		return toStop;
	}

	public double getDistanceMeters() {
		return distanceMeters;
	}

	public double getSpeedMps() {
		return speedMps;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return Arrays.asList(sourceRef);
	}

	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Too fast transfer walking speed between stop {0} and stop {1} (distance {2}): {3} > {4}",
				fmt.id(fromStop.getId()), fmt.id(toStop.getId()),
				fmt.var(fmt.distance(distanceMeters)),
				fmt.var(fmt.speed(speedMps)),
				fmt.var(fmt.speed(maxSpeedMps)));
	}

	@Override
	public int compareTo(TooFastWalkingSpeed o) {
		// Compare on computed speed, highest first
		return -Double.compare(speedMps, o.speedMps);
	}
}
