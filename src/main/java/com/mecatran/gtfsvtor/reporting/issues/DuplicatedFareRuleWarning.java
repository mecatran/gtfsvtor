package com.mecatran.gtfsvtor.reporting.issues;

import java.util.List;
import java.util.stream.Collectors;

import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.reporting.IssueFormatter;
import com.mecatran.gtfsvtor.reporting.ReportIssue;
import com.mecatran.gtfsvtor.reporting.ReportIssuePolicy;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.SourceInfoWithFields;

/*
 * Note: we set the severity to warning, as the GTFS specs do not clearly state this to be forbidden. But anyway, this is useless.
 */
@ReportIssuePolicy(severity = ReportIssueSeverity.WARNING, categoryName = "Duplicated rules in fare")
public class DuplicatedFareRuleWarning implements ReportIssue {

	private GtfsFareAttribute fareAttribute;
	private List<GtfsFareRule> fareRules;

	private List<SourceInfoWithFields> sourceInfos;

	public DuplicatedFareRuleWarning(GtfsFareAttribute fareAttribute,
			List<GtfsFareRule> fareRules) {
		this.fareAttribute = fareAttribute;
		this.fareRules = fareRules;
		this.sourceInfos = fareRules.stream()
				.map(rule -> new SourceInfoWithFields(rule.getSourceInfo(),
						"route_id", "origin_id", "destination_id",
						"contains_id"))
				.collect(Collectors.toList());
	}

	public GtfsFareAttribute getFareAttribute() {
		return fareAttribute;
	}

	public List<GtfsFareRule> getFareRules() {
		return fareRules;
	}

	@Override
	public List<SourceInfoWithFields> getSourceInfos() {
		return sourceInfos;
	}

	@Override
	public void format(IssueFormatter fmt) {
		fmt.text(
				"Fare rules are duplicated (same set of route and zones IDs for the same fare)");
	}
}
