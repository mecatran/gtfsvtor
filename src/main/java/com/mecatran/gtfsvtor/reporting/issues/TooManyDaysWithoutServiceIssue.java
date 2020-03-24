package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Too many days w/o service")
public class TooManyDaysWithoutServiceIssue implements ReportIssue {

	private GtfsLogicalDate fromDate, toDate;
	private int nDays;

	public TooManyDaysWithoutServiceIssue(GtfsLogicalDate fromDate,
			GtfsLogicalDate toDate, int nDays) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.nDays = nDays;
	}

	public GtfsLogicalDate getFromDate() {
		return fromDate;
	}

	public GtfsLogicalDate getToDate() {
		return toDate;
	}

	public int getNumberOfDays() {
		return nDays;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("{0} consecutive days without any service, from {1} to {2}",
				nDays, fmt.date(fromDate), fmt.date(toDate));
	}
}
