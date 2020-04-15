package com.mecatran.gtfsvtor.validation.dao;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsFareRule;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedFareRuleWarning;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class DuplicatedFareRuleValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		for (GtfsFareAttribute fareAttribute : dao.getFareAttributes()) {
			ListMultimap<List<Object>, GtfsFareRule> rulesPerIds = ArrayListMultimap
					.create();
			for (GtfsFareRule fareRule : dao
					.getRulesOfFare(fareAttribute.getId())) {
				List<Object> key = Arrays.asList(fareRule.getRouteId(),
						fareRule.getOriginId(), fareRule.getDestinationId(),
						fareRule.getContainsId());
				rulesPerIds.put(key, fareRule);
			}
			for (List<GtfsFareRule> fareRules : Multimaps.asMap(rulesPerIds)
					.values()) {
				if (fareRules.size() == 1)
					continue; // OK
				reportSink.report(new DuplicatedFareRuleWarning(fareAttribute,
						fareRules));
			}
		}
	}
}
