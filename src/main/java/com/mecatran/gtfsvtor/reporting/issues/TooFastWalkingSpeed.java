package com.mecatran.gtfsvtor.reporting.issues;

import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(categoryName = "Too fast walking speed")
public class TooFastWalkingSpeed
		implements ReportIssue, Comparable<TooFastWalkingSpeed> {

	private GtfsStop fromStop, toStop;
	private double distanceMeters;
	private double speedMps;
	private double maxSpeedMps;
	private ReportIssueSeverity severity;

	private SourceInfoWithFields sourceInfo;

	public TooFastWalkingSpeed(DataObjectSourceInfo sourceInfo,
			GtfsStop fromStop, GtfsStop toStop, double distanceMeters,
			double speedMps, double maxSpeedMps, ReportIssueSeverity severity) {
		this.fromStop = fromStop;
		this.toStop = toStop;
		this.distanceMeters = distanceMeters;
		this.speedMps = speedMps;
		this.maxSpeedMps = maxSpeedMps;
		this.severity = severity;
		this.sourceInfo = new SourceInfoWithFields(sourceInfo, "from_stop_id",
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
	public List<SourceInfoWithFields> getSourceInfos() {
		return Arrays.asList(sourceInfo);
	}

	@Override
	public ReportIssueSeverity getSeverity() {
		return severity;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Too fast transfer walking speed between stop {0} and stop {1} (distance {2}m): {3} > {4} m/s",
				fmt.id(fromStop.getId()), fmt.id(toStop.getId()),
				fmt.var(String.format("%.2f", distanceMeters)),
				fmt.var(String.format("%.2f", speedMps)),
				fmt.var(String.format("%.2f", maxSpeedMps)));
	}

	@Override
	public int compareTo(TooFastWalkingSpeed o) {
		// Compare on computed speed, highest first
		return -Double.compare(speedMps, o.speedMps);
	}
}
