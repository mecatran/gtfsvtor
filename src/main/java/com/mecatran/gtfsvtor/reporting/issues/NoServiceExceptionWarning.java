package com.mecatran.gtfsvtor.reporting.issues;

import com.mecatran.gtfsvtor.model.GtfsCalendarDate;
import com.mecatran.gtfsvtor.model.GtfsLogicalDate;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "No service exception")
public class NoServiceExceptionWarning implements ReportIssue {

	private GtfsLogicalDate firstDate;
	private GtfsLogicalDate lastDate;

	public NoServiceExceptionWarning(GtfsLogicalDate firstDate,
			GtfsLogicalDate lastDate) {
		this.firstDate = firstDate;
		this.lastDate = lastDate;
	}

	public GtfsLogicalDate getFirstDate() {
		return firstDate;
	}

	public GtfsLogicalDate getLastDate() {
		return lastDate;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"All services are defined on a weekly basis, from {0} to {1}, with no single day variations. If there are exceptions, such as holiday service dates, please ensure they are listed in {2}.",
				fmt.date(firstDate), fmt.date(lastDate),
				fmt.pre(GtfsCalendarDate.TABLE_NAME));
	}
}
