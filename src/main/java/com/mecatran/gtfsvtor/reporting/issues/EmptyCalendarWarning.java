package com.mecatran.gtfsvtor.reporting.issues;

import java.util.List;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsCalendar;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceRefWithFields;

@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Empty calendar")
public class EmptyCalendarWarning implements ReportIssue {

	private GtfsCalendar.Id serviceId;
	private List<SourceRefWithFields> sourceRefs;

	public EmptyCalendarWarning(GtfsCalendar.Id serviceId,
			List<DataObjectSourceRef> sourceRefs) {
		this.serviceId = serviceId;
		this.sourceRefs = sourceRefs.stream().map(SourceRefWithFields::new)
				.collect(Collectors.toList());
	}

	public GtfsCalendar.Id getServiceId() {
		return serviceId;
	}

	@Override
	public List<SourceRefWithFields> getSourceRefs() {
		return sourceRefs;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text("Calendar {0} is not active on any day", fmt.id(serviceId));
	}
}
