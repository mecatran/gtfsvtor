package com.mecatran.gtfsvtor.reporting.issues;

import java.util.List;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Empty calendar")
public class EmptyCalendarWarning implements ReportIssue {

	private GtfsCalendar.Id serviceId;
	private List<SourceInfoWithFields> sourceInfos;

	public EmptyCalendarWarning(GtfsCalendar.Id serviceId,
			List<DataObjectSourceInfo> sourceInfos) {
		this.serviceId = serviceId;
		this.sourceInfos = sourceInfos.stream().map(SourceInfoWithFields::new)
				.collect(Collectors.toList());
	}

	public GtfsCalendar.Id getServiceId() {
		return serviceId;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Calendar {0} is not active on any day", fmt.id(serviceId));
	}
}
